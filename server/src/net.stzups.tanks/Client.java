package net.stzups.tanks;

import com.sun.deploy.trace.LoggerTraceListener;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Client implements Runnable {
    private UUID uuid;
    private Socket client;
    private boolean running;

    Client(Socket socket, UUID uuid) {
        this.client = socket;
        this.uuid = uuid;
        running = true;
        new Thread(this).start();
    }

    UUID getUUID() {
        return uuid;
    }

    public void run() {
        try {
            InputStream inputStream = client.getInputStream();
            OutputStream outputStream = client.getOutputStream();

            try (Scanner scanner = new Scanner(inputStream, "UTF-8")){
                String data = scanner.useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);

                if (get.find()) {
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    if (match.find()) {
                        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                + "Connection: Upgrade\r\n"
                                + "Upgrade: websocket\r\n"
                                + "Sec-WebSocket-Accept: "
                                + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                                + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
                        outputStream.write(response, 0, response.length);

                        while(running) {
                            if (inputStream.available() > 0) {
                                byte[] head = new byte[2];
                                inputStream.read(head, 0, 2);
                                Logger.log("first byte: "+
                                        ((head[0] >> 7) & 1)
                                        +((head[0] >> 6) & 1)
                                        +((head[0] >> 5) & 1)
                                        +((head[0] >> 4) & 1)
                                        +((head[0] >> 3) & 1)
                                        +((head[0] >> 2) & 1)
                                        +((head[0] >> 1) & 1)
                                        +(head[0] & 1));
                                if (((head[0] >> 7) & 1 ) == 1) { // First bit - FIN
                                    Logger.log("FIN");
                                } else {
                                    Logger.log("not FIN");
                                }
                                switch (head[0] & 0x0F) { // Last 4 bits - opcode
                                    case 0:
                                        Logger.log("continuation frame");
                                        break;
                                    case 1:
                                        Logger.log("text frame");
                                        switch (head[1] + 0x80) {
                                            case 126:
                                                inputStream.skip(2); //todo read these?
                                                break;
                                            case 127:
                                                inputStream.skip(4);
                                                break;
                                        }
                                        byte[] key = new byte[4];
                                        inputStream.read(key, 0, 4);
                                        byte[] encoded = new byte[inputStream.available()];
                                        byte[] decoded = new byte[encoded.length];
                                        inputStream.read(encoded);
                                        for (int i = 0; i < encoded.length; i++) {
                                            decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
                                        }
                                        Logger.log(new String(decoded));
                                        break;
                                    case 2:
                                        Logger.log("binary frame");
                                        break;
                                    case 8:
                                        Logger.log("Connection close");
                                        break;
                                    case 9:
                                        Logger.log("ping");
                                        break;
                                    case 10:
                                        Logger.log("pong");
                                        break;
                                    default:
                                        throw new RuntimeException("Unrecognized opcode " + (head[0] & 0x0F));
                                }
                            }
                        }
                    } else {
                        Logger.log("No matches for " + match.pattern() + " in " + data, LoggerType.WARNING);
                    }
                } else {
                    Logger.log("No matches for " + get.pattern() + " in " + data, LoggerType.WARNING);
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
