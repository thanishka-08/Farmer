import ui.DashboardFrame;

import javax.swing.*;

/**
 * Main application entry point for Agricultural Supply Chain Management System.
 * Initializes the Swing GUI on the Event Dispatch Thread with a modern Look and Feel.
 */
public class Main {
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

        // Launch Application Frame on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                DashboardFrame frame = new DashboardFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to start Agricultural Supply Chain application:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
