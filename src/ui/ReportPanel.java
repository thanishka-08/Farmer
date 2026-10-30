package ui;

import model.*;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Reports Panel generating aggregate supply chain summaries:
 * Farmer-wise, Crop-wise, Market, Storage, Vehicle allocations,
 * Pending lots, and Completed shipments with CSV file export capability.
 */
public class ReportPanel extends JPanel {
    private DataManager dataManager;

    private JComboBox<String> cbReportType;
    private JTable reportTable;
    private DefaultTableModel reportModel;
    private JLabel lblReportDesc;

    public ReportPanel(DataManager dataManager) {
        this.dataManager = dataManager;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        loadSelectedReport();

        dataManager.addChangeListener(this::loadSelectedReport);
    }

    private void initComponents() {
        // TOP: Header
        JLabel titleLbl = new JLabel("Supply Chain Reports & Analytics");
        titleLbl.setFont(UITheme.FONT_PANEL_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);
        add(titleLbl, BorderLayout.NORTH);

        JPanel contentCard = UITheme.createCard();
        contentCard.setLayout(new BorderLayout(12, 12));

        // Selector and Action Bar
        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        topBar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(new JLabel("Select Report Type:"));

        cbReportType = new JComboBox<>(new String[]{
            "Farmer-wise Harvest Summary",
            "Crop-wise Quantity Summary",
            "Market Allocation Summary",
            "Storage Allocation Summary",
            "Vehicle Allocation Summary",
            "Pending Harvest Lots",
            "Completed Shipments"
        });
        cbReportType.setFont(UITheme.FONT_BOLD);
        cbReportType.addActionListener(e -> loadSelectedReport());
        leftPanel.add(cbReportType);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadSelectedReport());
        leftPanel.add(btnRefresh);

        topBar.add(leftPanel, BorderLayout.WEST);

        // Export CSV button
        JButton btnExport = UITheme.createPrimaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCurrentReport());
        topBar.add(btnExport, BorderLayout.EAST);

        contentCard.add(topBar, BorderLayout.NORTH);

        // Report Table
        reportModel = new DefaultTableModel();
        reportTable = new JTable(reportModel);
        UITheme.styleTable(reportTable);
        contentCard.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        // Bottom description
        lblReportDesc = new JLabel("Summary generated from live application data.");
        lblReportDesc.setFont(UITheme.FONT_SMALL);
        lblReportDesc.setForeground(UITheme.TEXT_MUTED);
        contentCard.add(lblReportDesc, BorderLayout.SOUTH);

        add(contentCard, BorderLayout.CENTER);
    }

    private void loadSelectedReport() {
        int index = cbReportType.getSelectedIndex();
        switch (index) {
            case 0:
                generateFarmerSummary();
                break;
            case 1:
                generateCropSummary();
                break;
            case 2:
                generateMarketSummary();
                break;
            case 3:
                generateStorageSummary();
                break;
            case 4:
                generateVehicleSummary();
                break;
            case 5:
                generatePendingLotsReport();
                break;
            case 6:
                generateCompletedShipmentsReport();
                break;
        }
        UITheme.styleTable(reportTable);
    }

    private void generateFarmerSummary() {
        lblReportDesc.setText("Aggregated harvest and delivery status for each registered farmer.");
        String[] cols = { "Farmer ID", "Farmer Name", "Village", "Total Lots", "Harvested (kg)", "Shipped (kg)", "Pending (kg)" };

        Map<String, int[]> stats = new HashMap<>(); // [totalLots, harvested, shipped, pending]
        for (Farmer f : dataManager.getFarmers()) {
            stats.put(f.getFarmerId(), new int[]{ 0, 0, 0, 0 });
        }

        for (HarvestLot l : dataManager.getHarvestLots()) {
            String fId = l.getFarmer() != null ? l.getFarmer().getFarmerId() : "UNKNOWN";
            int[] arr = stats.computeIfAbsent(fId, k -> new int[]{ 0, 0, 0, 0 });
            arr[0]++;
            arr[1] += l.getOriginalQuantity();
            arr[2] += l.getProcessedQuantity();
            arr[3] += l.getRemainingQuantity();
        }

        reportModel.setDataVector(new Object[0][0], cols);
        for (Farmer f : dataManager.getFarmers()) {
            int[] arr = stats.getOrDefault(f.getFarmerId(), new int[]{ 0, 0, 0, 0 });
            reportModel.addRow(new Object[]{
                f.getFarmerId(), f.getName(), f.getVillage(),
                arr[0], arr[1], arr[2], arr[3]
            });
        }
    }

    private void generateCropSummary() {
        lblReportDesc.setText("Commodity quantities harvested, allocated, and pending across all lots.");
        String[] cols = { "Crop Name", "Total Harvested (kg)", "Shipped to Market (kg)", "Stored (kg)", "Total Shipped (kg)", "Pending (kg)" };

        Map<String, int[]> cropStats = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); // [harvested, toMarket, toStore, totalShipped, pending]

        for (HarvestLot l : dataManager.getHarvestLots()) {
            int[] arr = cropStats.computeIfAbsent(l.getCrop(), k -> new int[5]);
            arr[0] += l.getOriginalQuantity();
            arr[4] += l.getRemainingQuantity();
        }

        for (Shipment s : dataManager.getShipments()) {
            int[] arr = cropStats.computeIfAbsent(s.getCrop(), k -> new int[5]);
            if ("Market".equalsIgnoreCase(s.getDestinationType())) {
                arr[1] += s.getQuantity();
            } else {
                arr[2] += s.getQuantity();
            }
            arr[3] += s.getQuantity();
        }

        reportModel.setDataVector(new Object[0][0], cols);
        for (Map.Entry<String, int[]> e : cropStats.entrySet()) {
            int[] a = e.getValue();
            reportModel.addRow(new Object[]{
                e.getKey(), a[0], a[1], a[2], a[3], a[4]
            });
        }
    }

    private void generateMarketSummary() {
        lblReportDesc.setText("Wholesale market fulfillment status and remaining unsatisfied demand.");
        String[] cols = { "Market ID", "Market Name", "Location", "Required Crop", "Initial Demand (kg)", "Delivered (kg)", "Remaining (kg)", "Fulfillment %" };

        reportModel.setDataVector(new Object[0][0], cols);
        for (Market m : dataManager.getMarkets()) {
            int delivered = m.getInitialDemand() - m.getRemainingDemand();
            double pct = m.getInitialDemand() > 0 ? (delivered * 100.0 / m.getInitialDemand()) : 0.0;
            reportModel.addRow(new Object[]{
                m.getMarketId(), m.getName(), m.getLocation(), m.getCrop(),
                m.getInitialDemand(), delivered, m.getRemainingDemand(),
                String.format("%.1f %%", pct)
            });
        }
    }

    private void generateStorageSummary() {
        lblReportDesc.setText("Storage facilities utilization, occupied space, and supported commodities.");
        String[] cols = { "Storage ID", "Storage Name", "Location", "Type", "Total Cap (kg)", "Occupied (kg)", "Available (kg)", "Occupancy %", "Supported Crops" };

        reportModel.setDataVector(new Object[0][0], cols);
        for (StorageCenter s : dataManager.getStorageCenters()) {
            double pct = s.getCapacity() > 0 ? (s.getUsedCapacity() * 100.0 / s.getCapacity()) : 0.0;
            reportModel.addRow(new Object[]{
                s.getId(), s.getName(), s.getLocation(), s.getStorageType(),
                s.getCapacity(), s.getUsedCapacity(), s.availableCapacity(),
                String.format("%.1f %%", pct), s.getSupportedCropsDisplay()
            });
        }
    }

    private void generateVehicleSummary() {
        lblReportDesc.setText("Transport fleet operational status, capacity, current location, and completed trips.");
        String[] cols = { "Vehicle ID", "Vehicle Number", "Capacity (kg)", "Current Location", "Status", "Dispatches Made" };

        Map<String, Integer> tripCounts = new HashMap<>();
        for (Shipment s : dataManager.getShipments()) {
            tripCounts.put(s.getVehicleId(), tripCounts.getOrDefault(s.getVehicleId(), 0) + 1);
        }

        reportModel.setDataVector(new Object[0][0], cols);
        for (Vehicle v : dataManager.getVehicles()) {
            reportModel.addRow(new Object[]{
                v.getId(), v.getVehicleNumber(), v.getCapacity(),
                v.getLocation(), v.getStatus(),
                tripCounts.getOrDefault(v.getId(), 0)
            });
        }
    }

    private void generatePendingLotsReport() {
        lblReportDesc.setText("Harvest lots with remaining unallocated quantities awaiting logistics dispatch.");
        String[] cols = { "Lot ID", "Farmer Name", "Crop", "Orig Qty (kg)", "Rem Qty (kg)", "Urgency", "Deadline (h)", "Location", "Status" };

        reportModel.setDataVector(new Object[0][0], cols);
        for (HarvestLot l : dataManager.getHarvestLots()) {
            if (l.getRemainingQuantity() > 0) {
                String fName = l.getFarmer() != null ? l.getFarmer().getName() : "N/A";
                reportModel.addRow(new Object[]{
                    l.getLotId(), fName, l.getCrop(),
                    l.getOriginalQuantity(), l.getRemainingQuantity(),
                    l.getUrgency(), l.getDeadline(), l.getLocation(), l.getStatus()
                });
            }
        }
    }

    private void generateCompletedShipmentsReport() {
        lblReportDesc.setText("All successfully dispatched supply chain shipments.");
        String[] cols = { "Shipment ID", "Lot ID", "Farmer", "Crop", "Destination", "Type", "Qty (kg)", "Vehicle", "Time (h)", "Route" };

        reportModel.setDataVector(new Object[0][0], cols);
        for (Shipment s : dataManager.getShipments()) {
            reportModel.addRow(new Object[]{
                s.getShipmentId(), s.getLotId(), s.getFarmerName(), s.getCrop(),
                s.getDestinationName(), s.getDestinationType(), s.getQuantity(),
                s.getVehicleId(), String.format("%.1f", s.getTravelTime()), s.getRouteDisplay()
            });
        }
    }

    private void exportCurrentReport() {
        if (reportModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "The current report has no data to export.",
                "Empty Report", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String safeName = cbReportType.getSelectedItem().toString().replaceAll("[^a-zA-Z0-9_]", "_") + ".csv";
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(safeName));

        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getAbsolutePath() + ".csv");
            }

            boolean ok = DataManager.exportTableToCsv(reportTable, target);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Report successfully exported to:\n" + target.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to write CSV file. Please check file permissions.",
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
