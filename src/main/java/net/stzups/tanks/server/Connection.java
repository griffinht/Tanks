package net.stzups.tanks.server;

import net.stzups.tanks.ConfigManager;
import net.stzups.tanks.FileManager;
import net.stzups.tanks.Tanks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
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
    private static final boolean ALLOW_MULTIPLE_CONNECTIONS_FROM_SAME_IP_ADDRESS = true;
    public static final int MAXIMUM_PING = 1000;

    private final FileManager fileManager;

    private final Server server;
    private final Socket socket;
    private final Queue<byte[]> queue = Collections.asLifoQueue(new ArrayDeque<>());

    private final UUID uuid = UUID.randomUUID();

    private long lastPing = System.currentTimeMillis();
    private int ping = 0;
    private boolean connected = false;

    private static byte[] SERVER_ADDRESS;

    static {
        try {//todo make sure this doesnt block
            String addressStr;
            String ip = ConfigManager.getConfigProperty("server_ip");

            if (ip.equalsIgnoreCase("auto")) {
                URL checkip = new URL("http://checkip.amazonaws.com");
                try (BufferedReader in = new BufferedReader(new InputStreamReader(checkip.openStream()))) {//todo this breaks when server isnt connected to internet
                    ip = in.readLine();
                }
            }

            InetAddress inetAddress = InetAddress.getByName(ip);
            if (inetAddress instanceof Inet6Address) {
                addressStr = "ws://[" + ip + "]";
            } else {
                addressStr = "ws://" + ip;
            }

            String port = ConfigManager.getConfigProperty("port");
            if (port.length() > 0) {
                addressStr += ":" + ConfigManager.getConfigProperty("port");
            }

            SERVER_ADDRESS = addressStr.getBytes();
        } catch (MalformedURLException e) {
            logger.warning("Auto IP address API failed");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            logger.warning("Couldn't resolve IP address set in config");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Connection(Server server, Socket socket, FileManager fileManager) {
        this.fileManager = fileManager;

        this.server = server;
        this.socket = socket;

        new Thread(this).start();
    }

    private final Thread heartbeat = new Thread(() -> {
        while(!Thread.currentThread().isInterrupted() && connected) {
            if (ping == -1 || ping > MAXIMUM_PING) {
                logger.warning("Closing unresponsive connection...");
                close(true);
            }
            ping = -1;
            lastPing = System.currentTimeMillis();
            sendPacket((byte) 0x9);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
        }

        close(false);
    });

    public int getPing() {
        return ping;
    }

    public Socket getSocket() {
        return socket;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()){//todo try catch all for java error to return 500 error

            try (Scanner scanner = new Scanner(inputStream, "UTF-8")) {
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

                        if (!ALLOW_MULTIPLE_CONNECTIONS_FROM_SAME_IP_ADDRESS && server.containsInetAddress(socket.getInetAddress())) {
                            sendPacket((byte) 0x8);
                            return;
                        }

                        server.addConnection(this);
                        connected = true;
                        heartbeat.start();
                        logger.info("Client connected from IP address " + socket.getInetAddress().getHostAddress());

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
                                        close(false);
                                        return;
                                    case 0x9: // ping, shouldn't ever receive one
                                        throw new RuntimeException("Client sent ping, server can't handle");
                                    case 0xA: // pong from client
                                        long time = System.currentTimeMillis();
                                        ping = (int) (time - lastPing);
                                        break;
                                    default: // error
                                        byte[] packet = new byte[inputStream.available()];
                                        if (inputStream.read(packet) == -1)
                                            throw new IOException("Could not read malformed packet");
                                        throw new RuntimeException("Unrecognized opcode ( "+ readBytesToString((byte) (head[0] & 0x0F)).substring(4) + " ) from client " + getSocket().getInetAddress().getHostAddress() + ", full head packet: " + readBytesToString(head) + " ");
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
                            String fPath = path.group();
                            if (fPath.contains(".")) {
                                foundPath = fPath;
                            } else {//todo get file without file ending
                                if (fPath.endsWith("/")) {
                                    foundPath = fPath + "index.html";
                                } else {
                                    outputStream.write(("HTTP/1.1 308 Permanent Redirect\r\n"
                                            + "Location: " + fPath + "/\r\n"
                                            + "\r\n").getBytes(StandardCharsets.UTF_8));
                                    return;
                                }
                            }
                        } else {
                            foundPath = "index.html";
                        }

                        byte[] fileContents = fileManager.getFileContents("client/" + foundPath);

                        if (fileContents.length > 0) {//todo cache this bad boy with some filemanager thing
                            for (int i = 0; i < fileContents.length; i++)
                            {
                                if (fileContents[i] == '[' && fileContents[i + 1] == '[' && fileContents[i + 2] == '[')
                                {
                                    for (int x = i + 3; x < fileContents.length; x++)
                                    {
                                        if (fileContents[x] == ']' && fileContents[x + 1] == ']' && fileContents[x + 2] == ']')
                                        {

                                            String str = new String(fileContents, i + 3, x - i - 3);
                                            String match = "SERVER_IP";
                                            if (str.equalsIgnoreCase(match))
                                            {
                                                byte[] first = new byte[fileContents.length + SERVER_ADDRESS.length - (match.length() + 6)];
                                                System.arraycopy(fileContents, 0, first, 0, x - 3);
                                                System.arraycopy(SERVER_ADDRESS, 0, first, i, SERVER_ADDRESS.length);
                                                System.arraycopy(fileContents, x + 3, first, i + SERVER_ADDRESS.length, first.length - (x + SERVER_ADDRESS.length));
                                                fileContents = first;
                                            }
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            outputStream.write(("HTTP/1.1 200 OK\r\n"
                                    + "Server: Tanks\r\n"
                                    + "Date: " + new Date() + "\r\n"
                                    + "Content-type: "
                                    + Files.probeContentType(Paths.get(fileManager.getFile("client/" + foundPath).getCanonicalPath())) //todo strip file ending?
                                    + "\r\n"
                                    + "Content-length: " + fileContents.length + "\r\n"
                                    + "\r\n").getBytes(StandardCharsets.UTF_8));
                            outputStream.write(fileContents);
                        } else {//TODO SANITIZE ALL INPUTS
                            outputStream.write(("HTTP/1.1 404 Not Found\r\n\r\n404 " + foundPath + " not found").getBytes(StandardCharsets.UTF_8));//todo add better 404
                        }
                    }
                } else {
                    logger.warning("Bad request from " + socket.getInetAddress().getHostAddress() + ", data: " + System.lineSeparator() + data);
                    outputStream.write(("HTTP/1.1 400 Bad Request").getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.warning("Closing connection for " + socket.getInetAddress().getHostAddress());
            close(true);
        }

        if (connected) {
            logger.warning("Client from " + socket.getInetAddress().getHostAddress() + " is still connected (it shouldn't be)");//todo make sure this doesn't happen
            //close(false, true);
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
            int length = readLength(head, inputStream);

            byte[] key = new byte[4];
            if (inputStream.read(key) != 4)
                throw new IOException("Couldn't read masking bits");
            byte[] encoded = new byte[length];
            decoded = new byte[encoded.length];

            if (inputStream.read(encoded) == -1)
                throw new IOException("Couldn't read encoded payload");
            for (int i = 0; i < encoded.length; i++) {
                decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
            }
        } else {
            byte[] packet = new byte[inputStream.available()];
            if (inputStream.read(packet) == -1)
                throw new IOException();
            throw new RuntimeException("Received unmasked data from client, full head packet: " + readBytesToString(head));
        }

        return decoded;
    }

    private static byte[] getFramedPacket(byte opcode, byte[] payload) {
        byte[] data;
        int offset;

        if (payload.length <= 125) {
             offset = 2;
             data = new byte[offset + payload.length];
             data[1] = (byte) (payload.length);
        } else {
            offset = 4;
            data = new byte[offset + payload.length];
            data[1] = 126;
            data[2] = (byte) (payload.length >> 8);
            data[3] = (byte) payload.length;
        }

        data[0] = (byte) (0x80 ^ opcode);

        System.arraycopy(payload, 0, data, offset, payload.length);

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

    private void sendPacket(byte opcode) {
        sendPacket(opcode, new byte[0]);
    }

    private void sendPacket(byte opcode, byte[] payload) {
        queue.add(getFramedPacket(opcode, payload));
    }

    public void sendText(String payload) {
        sendPacket((byte) 0x1, payload.getBytes());//todo explicitly specify charset?.
    }

    public void sendBinary(byte[] payload) {
        sendPacket((byte) 0x2, payload);
    }

    public void close(boolean kick) {
        close(kick, true);
    }

    void close(boolean kick, boolean remove) {
        if (connected) {
            connected = false;
            heartbeat.interrupt();
            if (kick)
                sendPacket((byte) 0x8);

            try {
                socket.close();
            }
            catch(IOException e) {
                logger.warning("Tried to close socket for client from " + socket.getInetAddress().getHostAddress());
                e.printStackTrace();
            }

            logger.info("Closed connection for client from " + socket.getInetAddress().getHostAddress());

            if (remove)
                server.removeConnection(this);
        } else {
            logger.warning("Tried to close connection for client from " + socket.getInetAddress().getHostAddress() + " but the connection was already closed (it shouldn't be)");
        }
    }
}
