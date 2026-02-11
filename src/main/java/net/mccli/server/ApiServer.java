package net.mccli.server;

import net.mccli.McCliMod;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ApiServer {
    private static final int PORT = 25566;
    private static boolean running = false;
    private static ServerSocket serverSocket;

    public static void start() {
        if (running) return;
        running = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT, 50, java.net.InetAddress.getLoopbackAddress());
                McCliMod.LOGGER.info("API Server started on port " + PORT);
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        new Thread(() -> handleClient(clientSocket)).start();
                    } catch (IOException e) {
                        if (running) {
                            McCliMod.LOGGER.error("Error accepting connection", e);
                        }
                    }
                }
            } catch (IOException e) {
                McCliMod.LOGGER.error("Could not listen on port " + PORT, e);
            }
        }).start();
    }

    public static void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            McCliMod.LOGGER.error("Error closing server socket", e);
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String response = CommandHandler.handle(inputLine);
                out.println(response);
            }
        } catch (IOException e) {
            McCliMod.LOGGER.error("Error handling client connection", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
