package cn.nukkit.raknet.server;

import java.nio.charset.StandardCharsets;

import cn.nukkit.raknet.RakNet;
import cn.nukkit.raknet.protocol.EncapsulatedPacket;
import cn.nukkit.utils.Binary;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class ServerHandler {

    protected final RakNetServer server;

    protected final ServerInstance instance;

    public ServerHandler(final RakNetServer server, final ServerInstance instance) {
        this.server = server;
        this.instance = instance;
    }

    public void sendEncapsulated(final String identifier, final EncapsulatedPacket packet) {
        this.sendEncapsulated(identifier, packet, RakNet.PRIORITY_NORMAL);
    }

    public void sendEncapsulated(final String identifier, final EncapsulatedPacket packet, final int flags) {
        final byte[] buffer = Binary.appendBytes(
                RakNet.PACKET_ENCAPSULATED,
                new byte[]{(byte) (identifier.length() & 0xff)},
                identifier.getBytes(StandardCharsets.UTF_8),
                new byte[]{(byte) (flags & 0xff)},
                packet.toBinary(true)
        );
        this.server.pushMainToThreadPacket(buffer);
    }

    public void sendRaw(final String address, final int port, final byte[] payload) {
        final byte[] buffer = Binary.appendBytes(
                RakNet.PACKET_RAW,
                new byte[]{(byte) (address.length() & 0xff)},
                address.getBytes(StandardCharsets.UTF_8),
                Binary.writeShort(port),
                payload
        );
        this.server.pushMainToThreadPacket(buffer);
    }

    public void closeSession(final String identifier, final String reason) {
        final byte[] buffer = Binary.appendBytes(
                RakNet.PACKET_CLOSE_SESSION,
                new byte[]{(byte) (identifier.length() & 0xff)},
                identifier.getBytes(StandardCharsets.UTF_8),
                new byte[]{(byte) (reason.length() & 0xff)},
                reason.getBytes(StandardCharsets.UTF_8)
        );
        this.server.pushMainToThreadPacket(buffer);
    }

    public void sendOption(final String name, final String value) {
        final byte[] buffer = Binary.appendBytes(
                RakNet.PACKET_SET_OPTION,
                new byte[]{(byte) (name.length() & 0xff)},
                name.getBytes(StandardCharsets.UTF_8),
                value.getBytes(StandardCharsets.UTF_8)
        );
        this.server.pushMainToThreadPacket(buffer);
    }

    public void blockAddress(final String address, final int timeout) {
        final byte[] buffer = Binary.appendBytes(
                RakNet.PACKET_BLOCK_ADDRESS,
                new byte[]{(byte) (address.length() & 0xff)},
                address.getBytes(StandardCharsets.UTF_8),
                Binary.writeInt(timeout)
        );
        this.server.pushMainToThreadPacket(buffer);
    }

    public void unblockAddress(final String address) {
        final byte[] buffer = Binary.appendBytes(
                RakNet.PACKET_UNBLOCK_ADDRESS,
                new byte[]{(byte) (address.length() & 0xff)},
                address.getBytes(StandardCharsets.UTF_8)
        );
        this.server.pushMainToThreadPacket(buffer);
    }

    public void shutdown() {
        this.server.shutdown();
        synchronized (this) {
            try {
                this.wait(20);
            } catch (final InterruptedException e) {
                //ignore
            }
        }
    }

    public void emergencyShutdown() {
        this.server.shutdown();
        this.server.pushMainToThreadPacket(new byte[]{RakNet.PACKET_EMERGENCY_SHUTDOWN});
    }

    protected void invalidSession(final String identifier) {
        final byte[] buffer = Binary.appendBytes(
                RakNet.PACKET_INVALID_SESSION,
                new byte[]{(byte) (identifier.length() & 0xff)},
                identifier.getBytes(StandardCharsets.UTF_8)
        );
        this.server.pushMainToThreadPacket(buffer);
    }

    public boolean handlePacket() {
        final byte[] packet = this.server.readThreadToMainPacket();
        if (packet != null && packet.length > 0) {
            final byte id = packet[0];
            int offset = 1;
            if (id == RakNet.PACKET_ENCAPSULATED) {
                final int len = packet[offset++];
                final String identifier = new String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8);
                offset += len;
                final int flags = packet[offset++];
                final byte[] buffer = Binary.subBytes(packet, offset);
                this.instance.handleEncapsulated(identifier, EncapsulatedPacket.fromBinary(buffer, true), flags);
            } else if (id == RakNet.PACKET_RAW) {
                final int len = packet[offset++];
                final String address = new String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8);
                offset += len;
                final int port = Binary.readShort(Binary.subBytes(packet, offset, 2)) & 0xffff;
                offset += 2;
                final byte[] payload = Binary.subBytes(packet, offset);
                this.instance.handleRaw(address, port, payload);
            } else if (id == RakNet.PACKET_SET_OPTION) {
                final int len = packet[offset++];
                final String name = new String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8);
                offset += len;
                final String value = new String(Binary.subBytes(packet, offset), StandardCharsets.UTF_8);
                this.instance.handleOption(name, value);
            } else if (id == RakNet.PACKET_OPEN_SESSION) {
                int len = packet[offset++];
                final String identifier = new String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8);
                offset += len;
                len = packet[offset++];
                final String address = new String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8);
                offset += len;
                final int port = Binary.readShort(Binary.subBytes(packet, offset, 2)) & 0xffff;
                offset += 2;
                final long clientID = Binary.readLong(Binary.subBytes(packet, offset, 8));
                this.instance.openSession(identifier, address, port, clientID);
            } else if (id == RakNet.PACKET_CLOSE_SESSION) {
                int len = packet[offset++];
                final String identifier = new String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8);
                offset += len;
                len = packet[offset++];
                final String reason = new String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8);
                this.instance.closeSession(identifier, reason);
            } else if (id == RakNet.PACKET_INVALID_SESSION) {
                final int len = packet[offset++];
                final String identifier = new String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8);
                this.instance.closeSession(identifier, "Invalid session");
            } else if (id == RakNet.PACKET_ACK_NOTIFICATION) {
                final int len = packet[offset++];
                final String identifier = new String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8);
                offset += len;
                final int identifierACK = Binary.readInt(Binary.subBytes(packet, offset, 4));
                this.instance.notifyACK(identifier, identifierACK);
            }
            return true;
        }

        return false;
    }

}
