package net.stzups.tanks.server;

import net.stzups.tanks.FileManager;
import net.stzups.tanks.Tanks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Queue;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection implements Runnable {

    private static final Logger logger = Logger.getLogger(Tanks.class.getName());

    private FileManager fileManager;

    private Server server;
    private UUID uuid;
    private Socket socket;
    private long lastPing = System.currentTimeMillis();
    private int ping = 0;
    private Queue<byte[]> queue = Collections.asLifoQueue(new ArrayDeque<>());
    private boolean connected = true;

    Connection(Server server, Socket socket, FileManager fileManager) {
        this.fileManager = fileManager;

        this.server = server;
        this.socket = socket;
        this.uuid = UUID.randomUUID();

        InetAddress inetAddress = this.socket.getInetAddress();
        if (server.getClientsMap().containsKey(inetAddress))
            server.getClientsMap().get(inetAddress).close();
        server.getClientsMap().put(this.socket.getInetAddress(), this);

        new Thread(this).start();
    }

    public UUID getUUID() {
        return uuid;
    }

    public Socket getSocket() {
        return socket;
    }

    public void run() {
        Thread manager = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted() && connected) {
                try {
                    Thread.sleep(1000);
                    if (ping == -1) {
                        close();
                    }
                    lastPing = System.currentTimeMillis();
                    sendPacket((byte) 0x9, "");
                } catch (InterruptedException e) {
                    close();
                }
            }
        });
        manager.start();

        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()){

            try (Scanner scanner = new Scanner(inputStream, "UTF-8")){
                String data = scanner.useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);

                if (get.find()) {
                    Matcher webSocket = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);

                    if (webSocket.find()) {
                        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                + "Connection: Upgrade\r\n"
                                + "Upgrade: websocket\r\n"
                                + "Sec-WebSocket-Accept: "
                                + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((webSocket.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                                + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
                        outputStream.write(response, 0, response.length);

                        logger.info("Client connected from IP address " + socket.getInetAddress().getHostAddress());

                        connection:
                        while(connected) {
                            if (inputStream.available() > 0) {
                                byte[] head = new byte[2];

                                if (inputStream.read(head) != 2)
                                    throw new IOException();

                                byte[] decoded = decodePayload(head, inputStream);

                                if (((head[0] >> 7) & 1 ) != 1) {
                                    throw new RuntimeException("Client sent non fin bit indicating fragmented message, server can't handle");
                                }

                                switch (head[0] & 0x0F) { // Opcode bits
                                    case 0x0: // continuation frame
                                        throw new RuntimeException("Client sent continuation frame, server can't handle");
                                    case 0x1: // text frame
                                        server.onTextPacket(this, new String(decoded));
                                        break;
                                    case 0x2: // binary frame
                                        logger.info("binary frame"); //todo handle
                                        break;
                                    case 0x8: // connection close
                                        logger.info("Client disconnected from IP address " + socket.getInetAddress().getHostAddress());
                                        close(true);
                                        break connection;
                                    case 0x9: // ping, shouldn't ever receive one
                                        throw new RuntimeException("Client sent ping, server can't handle");
                                    case 0xA: // pong from client
                                        long time = System.currentTimeMillis();
                                        ping = (int) (time - lastPing);
                                        lastPing = -1;
                                        sendText("ping:" + ping);
                                        break;
                                    default: // error
                                        byte[] packet = new byte[inputStream.available()];
                                        if (inputStream.read(packet) == -1)
                                            throw new IOException("Could not read malformed packet");
                                        throw new RuntimeException("Unrecognized opcode ( "+ readBytesToString((byte) (head[0] & 0x0F)).substring(4) + " ) from client " + getUUID() + ", full head packet: " + readBytesToString(head) + " ");
                                }
                            }

                            if (connected) {
                                while (queue.peek() != null) {
                                    outputStream.write(queue.poll());
                                }
                            }
                        }

                    } else {
                        Matcher path = Pattern.compile("(?<=GET /)\\S+").matcher(data);
                        String foundPath;

                        if (path.find()) {
                            foundPath = path.group();
                        } else {
                            foundPath = "index.html";
                        }
                        
                        byte[] fileContents = fileManager.getFileContents("resources/client/" + foundPath);

                        if(fileContents.length > 0) {
                            outputStream.write(("HTTP/1.1 200 OK\r\n"
                                    + "Server: Tanks\r\n"
                                    + "Date: " + new Date() + "\r\n"
                                    + "Content-type: "
                                    + Files.probeContentType(Paths.get(fileManager.getFile("resources/client/" + foundPath).getCanonicalPath()))
                                    + "\r\n"
                                    + "Content-length: " + fileContents.length + "\r\n"
                                    + "\r\n").getBytes(StandardCharsets.UTF_8));
                            outputStream.write(fileContents);
                        } else {
                            outputStream.write(("HTTP/1.1 404 Not Found").getBytes(StandardCharsets.UTF_8));
                        }
                    }
                } else {
                    outputStream.write(("HTTP/1.1 400 Bad Request").getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (connected) {
            close(true);
        }
    }

    private static int readLength(byte[] head, InputStream inputStream) throws IOException {
        byte[] lengthBits;
        int length;
        switch (head[1] + 0x80) {
            case 126:
                lengthBits = new byte[2];
                if (inputStream.read(lengthBits) != 2)
                    throw new IOException();
                length = (((lengthBits[0] & 0xFF) << 8)
                        | (lengthBits[1] & 0xFF));
                break;
            case 127:
                throw new RuntimeException("Client sent message too long, server can't handle");
                /*
                lengthBits = new byte[8];
                if (inputStream.read(lengthBits) != 8)
                    throw new IOException();
                length = (((long)(lengthBits[0] & 0xFF) << 56)
                        | ((long)(lengthBits[1] & 0xFF) << 48)
                        | ((long)(lengthBits[2] & 0xFF) << 40)
                        | ((long)(lengthBits[3] & 0xFF) << 32)
                        | ((long)(lengthBits[4] & 0xFF) << 24)
                        | ((long)(lengthBits[5] & 0xFF) << 16)
                        | ((long)(lengthBits[6] & 0xFF) << 8)
                        | ((long)lengthBits[7] & 0xFF));
                break;
                 */
            default:
                length = head[1] + 0x80;
        }

        return length;
    }

    private static byte[] decodePayload(byte[] head, InputStream inputStream) throws IOException {
        byte[] decoded;

        if (((head[1] >> 7) & 1) == 1) { // Mask bit
            long length = readLength(head, inputStream);

            byte[] key = new byte[4];
            if (inputStream.read(key) != 4)
                throw new IOException("Couldn't read masking bits");
            byte[] encoded = new byte[inputStream.available()];
            decoded = new byte[encoded.length];

            if (inputStream.read(encoded) == -1)
                throw new IOException("Couldn't read encoded payload");
            for (int i = 0; i < encoded.length; i++) {
                decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
            }

            if (length != decoded.length) {
                throw new RuntimeException("Mismatching payload lengths, (packet length: " + length + ", actual length " + decoded.length + ")");
            }
        } else {
            byte[] packet = new byte[inputStream.available()];
            if (inputStream.read(packet) == -1)
                throw new IOException();
            throw new RuntimeException("Received unmasked data from client, full HEAD packet: " + readBytesToString(head));
        }

        return decoded;
    }

    private static byte[] getFramedPacket(byte opcode, String payload) {
        byte[] data;
        int offset;

        if (payload.length() <= 125) {
             offset = 2;
             data = new byte[offset + payload.length()];
             data[1] = (byte) (payload.length());
        } else if (payload.length() > 126 && payload.length() < 65535) {
            offset = 4;
            data = new byte[offset + payload.length()];
            data[1] = 126;
        } else {
            offset = 6;
            data = new byte[offset + payload.length()];
        }

        data[0] = (byte) (0x80 ^ opcode);

        for (int i = 0; i < payload.length(); i++) {
            data[offset + i] = (byte) payload.charAt(i);
        }

        return data;
    }

    private static String readBytesToString(byte[] bytes) {
        StringBuilder string = new StringBuilder();

        for (byte b : bytes) {
            string.append((b >> 7) & 1);
            string.append((b >> 6) & 1);
            string.append((b >> 5) & 1);
            string.append((b >> 4) & 1);
            string.append((b >> 3) & 1);
            string.append((b >> 2) & 1);
            string.append((b >> 1) & 1);
            string.append(b & 1);
            string.append(" ");
            string.append(" (");
            string.append(b);
            string.append(") ");
        }

        if (string.length() > 0) {
            return string.substring(0, string.length() - 1);
        } else {
            return string.toString();
        }
    }

    private static String readBytesToString(byte b) {
        return readBytesToString(new byte[]{b});
    }

    void sendPacket(byte opcode, String payload) {
        queue.add(getFramedPacket(opcode, payload));
    }

    void sendText(String payload) {
        sendPacket((byte) 0x1, payload);
    }

    boolean isConnected() {
        return connected;
    }

    public void close() {
        close(false);
    }

    public void close(boolean quiet) {
        sendPacket((byte) 0x8, "");
        connected = false;
        if (!quiet)
            logger.info("Closed connection for client from " + socket.getInetAddress().getHostAddress());
        server.getClientsMap().remove(socket.getInetAddress());
    }
}
