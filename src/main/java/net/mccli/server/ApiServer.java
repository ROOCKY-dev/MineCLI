package net.mccli.server;

import com.google.gson.JsonObject;
import net.mccli.McCliMod;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ApiServer {
    private static final int PORT = 25566;
    private static boolean running = false;
    private static ServerSocket serverSocket;
    private static final Set<PrintWriter> clients = Collections.synchronizedSet(new HashSet<>());

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

    public static void broadcast(JsonObject json) {
        String message = CommandHandler.GSON.toJson(json);
        synchronized (clients) {
            for (PrintWriter client : clients) {
                client.println(message);
            }
        }
    }

    private static void handleClient(Socket clientSocket) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            clients.add(out);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String response = CommandHandler.handle(inputLine);
                out.println(response);
            }
        } catch (IOException e) {
            McCliMod.LOGGER.error("Error handling client connection", e);
        } finally {
            if (out != null) {
                clients.remove(out);
                out.close();
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
