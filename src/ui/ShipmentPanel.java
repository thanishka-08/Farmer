package ui;

import model.Shipment;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Shipment Management Panel displaying all generated dispatches,
 * allocated quantities, Dijkstra shortest routes, travel times, and vehicle assignments.
 */
public class ShipmentPanel extends JPanel {
    private DataManager dataManager;

    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;

    public ShipmentPanel(DataManager dataManager) {
        this.dataManager = dataManager;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        refreshTable();

        dataManager.addChangeListener(this::refreshTable);
    }

    private void initComponents() {
        // TOP: Header
        JLabel titleLbl = new JLabel("Shipment & Dispatch Tracking");
        titleLbl.setFont(UITheme.FONT_PANEL_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);
        add(titleLbl, BorderLayout.NORTH);

        JPanel contentCard = UITheme.createCard();
        contentCard.setLayout(new BorderLayout(12, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(8, 8));
        toolbar.setOpaque(false);

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setOpaque(false);
        leftTools.add(new JLabel("Search Shipments:"));
        txtSearch = UITheme.createTextField(18);
        leftTools.add(txtSearch);

        JButton btnSearch = UITheme.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> searchShipments());
        leftTools.add(btnSearch);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Shipments");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            refreshTable();
        });
        leftTools.add(btnRefresh);

        JButton btnDetails = UITheme.createPrimaryButton("View Details");
        btnDetails.addActionListener(e -> viewDetails());
        leftTools.add(btnDetails);

        toolbar.add(leftTools, BorderLayout.WEST);
        contentCard.add(toolbar, BorderLayout.NORTH);

        // JTable
        String[] cols = {
            "Shipment ID", "Lot ID", "Farmer Name", "Crop",
            "Dest Type", "Destination", "Allocated (kg)",
            "Vehicle ID", "Route (Dijkstra)", "Est. Time (h)", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Double click to view details
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewDetails();
                }
            }
        });

        contentCard.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom status note
        JLabel lblNote = new JLabel("Tip: Double-click any row or select and click 'View Details' to inspect the complete route breakdown.");
        lblNote.setFont(UITheme.FONT_SMALL);
        lblNote.setForeground(UITheme.TEXT_MUTED);
        contentCard.add(lblNote, BorderLayout.SOUTH);

        add(contentCard, BorderLayout.CENTER);
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shipment from the table to view details.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String shipmentId = tableModel.getValueAt(row, 0).toString();
        Shipment s = null;
        for (Shipment ship : dataManager.getShipments()) {
            if (ship.getShipmentId().equalsIgnoreCase(shipmentId)) {
                s = ship;
                break;
            }
        }

        if (s == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("        SHIPMENT DISPATCH REPORT         \n");
        sb.append("=========================================\n\n");
        sb.append(String.format("Shipment ID      : %s\n", s.getShipmentId()));
        sb.append(String.format("Harvest Lot ID   : %s\n", s.getLotId()));
        sb.append(String.format("Farmer Name      : %s\n", s.getFarmerName()));
        sb.append(String.format("Crop Commodity   : %s\n", s.getCrop()));
        sb.append(String.format("Quantity Shipped : %d kg\n", s.getQuantity()));
        sb.append(String.format("Destination Type : %s\n", s.getDestinationType()));
        sb.append(String.format("Destination Name : %s (%s)\n", s.getDestinationName(), s.getDestinationLocation()));
        sb.append(String.format("Transport Vehicle: %s\n", s.getVehicleId()));
        sb.append(String.format("Dijkstra Route   : %s\n", s.getRouteDisplay()));
        sb.append(String.format("Est. Travel Time : %.2f hours\n", s.getTravelTime()));
        sb.append(String.format("Shipment Status  : %s\n", s.getStatus()));
        sb.append("=========================================\n");

        JTextArea txt = new JTextArea(sb.toString());
        txt.setFont(new Font("Consolas", Font.PLAIN, 12));
        txt.setEditable(false);
        JScrollPane sp = new JScrollPane(txt);
        sp.setPreferredSize(new Dimension(460, 320));

        JOptionPane.showMessageDialog(this, sp, "Shipment Details - " + s.getShipmentId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void searchShipments() {
        String q = txtSearch.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            refreshTable();
            return;
        }

        tableModel.setRowCount(0);
        for (Shipment s : dataManager.getShipments()) {
            if (s.getShipmentId().toLowerCase().contains(q) ||
                s.getLotId().toLowerCase().contains(q) ||
                s.getFarmerName().toLowerCase().contains(q) ||
                s.getCrop().toLowerCase().contains(q) ||
                s.getDestinationName().toLowerCase().contains(q) ||
                s.getDestinationType().toLowerCase().contains(q) ||
                s.getVehicleId().toLowerCase().contains(q) ||
                s.getRouteDisplay().toLowerCase().contains(q) ||
                s.getStatus().toLowerCase().contains(q)) {

                tableModel.addRow(new Object[]{
                    s.getShipmentId(), s.getLotId(), s.getFarmerName(), s.getCrop(),
                    s.getDestinationType(), s.getDestinationName(), s.getQuantity(),
                    s.getVehicleId(), s.getRouteDisplay(), String.format("%.1f", s.getTravelTime()),
                    s.getStatus()
                });
            }
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Shipment s : dataManager.getShipments()) {
            tableModel.addRow(new Object[]{
                s.getShipmentId(), s.getLotId(), s.getFarmerName(), s.getCrop(),
                s.getDestinationType(), s.getDestinationName(), s.getQuantity(),
                s.getVehicleId(), s.getRouteDisplay(), String.format("%.1f", s.getTravelTime()),
                s.getStatus()
            });
        }
    }
}
