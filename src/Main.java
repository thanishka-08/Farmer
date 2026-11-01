import ui.DashboardFrame;

import javax.swing.*;
import java.awt.Desktop;
import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;

/**
 * Main application entry point for Agricultural Supply Chain Management System.
 * Initializes the Swing GUI on the Event Dispatch Thread with a modern Look and Feel,
 * and starts an embedded Localhost HTTP Server for viewing the web dashboard & reports.
 */
public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        // Support direct terminal execution mode
        if (args.length > 0 && (args[0].equalsIgnoreCase("--terminal") || args[0].equalsIgnoreCase("--cli") || args[0].equalsIgnoreCase("-t") || args[0].equalsIgnoreCase("console"))) {
            test.SystemDemonstration.main(args);
            return;
        }
        if (args.length > 0 && (args[0].equalsIgnoreCase("--test") || args[0].equalsIgnoreCase("-test"))) {
            test.SystemValidationTest.main(args);
            return;
        }

        // Set System or Nimbus Look and Feel for clean native UI appearance
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Start embedded Localhost Web Server on port 8080 (or 8000 fallback)
        int activePort = startLocalHttpServer(DEFAULT_PORT);

        // Launch Application Frame on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                DashboardFrame frame = new DashboardFrame();
                frame.setVisible(true);
                frame.toFront();
                frame.requestFocus();

                System.out.println("====================================================================");
                System.out.println(" [SUCCESS] Agricultural Supply Chain GUI Window is OPEN.");
                if (activePort > 0) {
                    System.out.println(" [SUCCESS] Localhost Web Server is ACTIVE at: http://localhost:" + activePort + "/");
                    System.out.println("           Raw Output Stream: http://localhost:" + activePort + "/output");
                }
                System.out.println("====================================================================");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to start Agricultural Supply Chain application:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Automatically launch the default web browser to the localhost dashboard
        if (activePort > 0) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + activePort + "/"));
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Starts an embedded HTTP server serving the interactive output dashboard and raw logs.
     */
    private static int startLocalHttpServer(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
            
            // Root context -> serves output_dashboard.html
            server.createContext("/", exchange -> {
                try {
                    File file = new File("output_dashboard.html");
                    if (!file.exists()) {
                        file = new File("output.txt");
                    }
                    byte[] bytes = file.exists() 
                        ? Files.readAllBytes(file.toPath()) 
                        : "<html><body><h2>System Ready</h2><p>Files loading...</p></body></html>".getBytes(StandardCharsets.UTF_8);
                    
                    exchange.getResponseHeaders().set("Content-Type", file.getName().endsWith(".html") ? "text/html; charset=UTF-8" : "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } catch (Exception ex) {
                    byte[] err = ("Error loading dashboard: " + ex.getMessage()).getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, err.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(err);
                    }
                }
            });

            // Plain text output context -> serves output.txt
            server.createContext("/output", exchange -> {
                try {
                    File file = new File("output.txt");
                    byte[] bytes = file.exists() ? Files.readAllBytes(file.toPath()) : "output.txt not found".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } catch (Exception ex) {
                    exchange.sendResponseHeaders(500, 0);
                }
            });

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            return port;
        } catch (Exception e) {
            if (port == DEFAULT_PORT) {
                return startLocalHttpServer(8000);
            }
            System.err.println("[WARN] Could not bind localhost port " + port + ": " + e.getMessage());
            return -1;
        }
    }
}
