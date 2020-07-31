package net.stzups.tanks.server;

public interface PacketListener {
    void onTextPacket(Connection connection, String payload);
    void onBinaryPacket(Connection connection, byte[] payload);
    void removeConnection(Connection connection);
    void addConnection(Connection connection);
}
