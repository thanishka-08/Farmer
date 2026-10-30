package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * UI Theme specifications and styling helpers for Agricultural Supply Chain Management System.
 * Implements a clean, modern green-and-white college demonstration theme.
 */
public class UITheme {
    // Green and White Color Palette
    public static final Color PRIMARY_GREEN   = new Color(46, 125, 50);    // #2E7D32
    public static final Color PRIMARY_DARK    = new Color(27, 94, 32);     // #1B5E20
    public static final Color PRIMARY_LIGHT   = new Color(232, 245, 233);   // #E8F5E9
    public static final Color PRIMARY_HOVER   = new Color(56, 142, 60);    // #388E3C
    public static final Color ACCENT_GREEN    = new Color(76, 175, 80);    // #4CAF50

    public static final Color BG_LIGHT        = new Color(248, 250, 248);   // Very soft sage white
    public static final Color BG_WHITE        = Color.WHITE;
    public static final Color BG_ROW_ALT      = new Color(244, 249, 244);   // Zebra table row

    public static final Color TEXT_DARK       = new Color(33, 37, 41);
    public static final Color TEXT_MUTED      = new Color(108, 117, 125);
    public static final Color BORDER_COLOR    = new Color(200, 220, 200);

    public static final Color COLOR_WARNING   = new Color(230, 81, 0);     // Amber/orange
    public static final Color COLOR_DANGER    = new Color(198, 40, 40);    // Crimson red
    public static final Color COLOR_SUCCESS   = new Color(46, 125, 50);    // Forest green

    // Standard Typography
    public static final Font FONT_APP_TITLE   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_PANEL_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_CARD_VALUE  = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_CARD_LABEL  = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD        = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_REGULAR     = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON      = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(PRIMARY_GREEN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_DARK, 1, true),
            new EmptyBorder(7, 14, 7, 14)
        ));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_GREEN);
            }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_GREEN, 1, true),
            new EmptyBorder(7, 12, 7, 12)
        ));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_LIGHT);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(COLOR_DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_DANGER.darker(), 1, true),
            new EmptyBorder(7, 12, 7, 12)
        ));
        return btn;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(5, 7, 5, 7)
        ));
        return tf;
    }

    public static JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 235, 230));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(PRIMARY_GREEN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
        header.setReorderingAllowed(false);

        // Alternating row renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_WHITE : BG_ROW_ALT);
                    c.setForeground(TEXT_DARK);
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
}
