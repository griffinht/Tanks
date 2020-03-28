package net.stzups.tanks.server;

public interface PacketListener {
    void onTextPacket(Connection connection, String payload);
}
