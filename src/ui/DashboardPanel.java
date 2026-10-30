package ui;

import storage.DataManager;
import storage.SampleDataLoader;
import service.SupplyChainService;
import service.ProcessResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Dashboard Panel displaying real-time supply chain summary KPI cards,
 * quick action navigation shortcuts, and the primary 'Process Harvest Lots' action.
 */
public class DashboardPanel extends JPanel {
    private DataManager dataManager;
    private Consumer<String> navigationCallback;

    // Metric Labels
    private JLabel lblTotalFarmersVal;
    private JLabel lblTotalLotsVal;
    private JLabel lblPendingQtyVal;
    private JLabel lblMarketDemandVal;
    private JLabel lblAvailableVehiclesVal;
    private JLabel lblStorageCapacityVal;
    private JLabel lblTotalShipmentsVal;

    public DashboardPanel(DataManager dataManager, Consumer<String> navigationCallback) {
        this.dataManager = dataManager;
        this.navigationCallback = navigationCallback;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        refreshMetrics();

        // Listen for changes
        dataManager.addChangeListener(this::refreshMetrics);
    }

    private void initComponents() {
        // TOP: Header Banner
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(UITheme.PRIMARY_GREEN);
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.PRIMARY_DARK, 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLbl = new JLabel("Agricultural Supply Chain Dashboard");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(Color.WHITE);

        JLabel subtitleLbl = new JLabel("Real-time crop logistics, demand matching, and route optimization");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLbl.setForeground(new Color(220, 245, 220));

        bannerPanel.add(titleLbl, BorderLayout.NORTH);
        bannerPanel.add(subtitleLbl, BorderLayout.SOUTH);
        add(bannerPanel, BorderLayout.NORTH);

        // CENTER: Metrics Grid + Quick Actions
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);

        // 7 KPI Summary Cards in a responsive grid
        JPanel kpiGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        kpiGrid.setOpaque(false);

        lblTotalFarmersVal = new JLabel("0");
        lblTotalLotsVal = new JLabel("0");
        lblPendingQtyVal = new JLabel("0 kg");
        lblMarketDemandVal = new JLabel("0 kg");
        lblAvailableVehiclesVal = new JLabel("0");
        lblStorageCapacityVal = new JLabel("0 kg");
        lblTotalShipmentsVal = new JLabel("0");

        kpiGrid.add(createKpiCard("Total Farmers", lblTotalFarmersVal, "Registered in network", new Color(46, 125, 50)));
        kpiGrid.add(createKpiCard("Total Harvest Lots", lblTotalLotsVal, "Lots registered", new Color(0, 121, 107)));
        kpiGrid.add(createKpiCard("Pending Harvest Qty", lblPendingQtyVal, "Awaiting allocation", new Color(230, 81, 0)));
        kpiGrid.add(createKpiCard("Remaining Market Demand", lblMarketDemandVal, "Unfulfilled demand", new Color(21, 101, 192)));
        kpiGrid.add(createKpiCard("Available Vehicles", lblAvailableVehiclesVal, "Ready for dispatch", new Color(56, 142, 60)));
        kpiGrid.add(createKpiCard("Available Storage", lblStorageCapacityVal, "Cold & Dry store", new Color(106, 27, 154)));
        kpiGrid.add(createKpiCard("Total Shipments", lblTotalShipmentsVal, "Created / Dispatched", new Color(46, 125, 50)));

        // 8th slot in the 2x4 grid: Process Lots Action Button Box!
        JPanel processCard = new JPanel(new BorderLayout(5, 5));
        processCard.setBackground(new Color(232, 245, 233));
        processCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.PRIMARY_GREEN, 2, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        JLabel procTitle = new JLabel("Supply Chain Engine", SwingConstants.CENTER);
        procTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        procTitle.setForeground(UITheme.PRIMARY_DARK);

        JButton btnProcess = new JButton("PROCESS HARVEST LOTS");
        btnProcess.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnProcess.setBackground(UITheme.PRIMARY_GREEN);
        btnProcess.setForeground(Color.WHITE);
        btnProcess.setFocusPainted(false);
        btnProcess.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProcess.addActionListener(e -> executeProcessing());

        processCard.add(procTitle, BorderLayout.NORTH);
        processCard.add(btnProcess, BorderLayout.CENTER);
        kpiGrid.add(processCard);

        centerPanel.add(kpiGrid, BorderLayout.NORTH);

        // LOWER SECTION: Quick Actions Toolbar & Overview
        JPanel actionsCard = UITheme.createCard();
        actionsCard.setLayout(new BorderLayout(10, 10));

        JLabel actTitle = new JLabel("Quick Management & Operations");
        actTitle.setFont(UITheme.FONT_PANEL_TITLE);
        actTitle.setForeground(UITheme.PRIMARY_DARK);
        actionsCard.add(actTitle, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnRow.setOpaque(false);

        JButton btnAddFarmer = UITheme.createSecondaryButton("+ Add Farmer");
        btnAddFarmer.addActionListener(e -> navigationCallback.accept("Farmer Management"));

        JButton btnAddLot = UITheme.createSecondaryButton("+ Add Harvest Lot");
        btnAddLot.addActionListener(e -> navigationCallback.accept("Harvest Lot Management"));

        JButton btnAddMarket = UITheme.createSecondaryButton("+ Add Market");
        btnAddMarket.addActionListener(e -> navigationCallback.accept("Market Management"));

        JButton btnAddStore = UITheme.createSecondaryButton("+ Add Storage Center");
        btnAddStore.addActionListener(e -> navigationCallback.accept("Storage Center Management"));

        JButton btnAddVehicle = UITheme.createSecondaryButton("+ Add Vehicle");
        btnAddVehicle.addActionListener(e -> navigationCallback.accept("Vehicle Management"));

        JButton btnViewReports = UITheme.createSecondaryButton("View Reports");
        btnViewReports.addActionListener(e -> navigationCallback.accept("Reports"));

        JButton btnLoadSample = UITheme.createPrimaryButton("LOAD SAMPLE DATA");
        btnLoadSample.setBackground(new Color(33, 150, 243));
        btnLoadSample.addActionListener(e -> loadSampleDataAction());

        JButton btnResetData = UITheme.createDangerButton("Clear All Data");
        btnResetData.addActionListener(e -> clearDataAction());

        btnRow.add(btnAddFarmer);
        btnRow.add(btnAddLot);
        btnRow.add(btnAddMarket);
        btnRow.add(btnAddStore);
        btnRow.add(btnAddVehicle);
        btnRow.add(btnViewReports);
        btnRow.add(btnLoadSample);
        btnRow.add(btnResetData);

        actionsCard.add(btnRow, BorderLayout.CENTER);

        // Info notes panel at bottom
        JTextArea notesArea = new JTextArea(
            "Instructions:\n" +
            "1. Enter your custom farmers, lots, markets, storage facilities, and vehicles using the management tabs.\n" +
            "2. Click 'PROCESS HARVEST LOTS' to execute prioritization, shortest-path Dijkstra matching, and shipment generation.\n" +
            "3. Switch to 'Shipment Management' to inspect generated routes and travel times.\n" +
            "4. Or click 'LOAD SAMPLE DATA' to instantly test the 20 lots, 3 markets, 2 stores, and 3 vehicles from the college problem set."
        );
        notesArea.setEditable(false);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        notesArea.setForeground(UITheme.TEXT_MUTED);
        notesArea.setBackground(UITheme.BG_WHITE);
        notesArea.setBorder(new EmptyBorder(8, 0, 0, 0));
        actionsCard.add(notesArea, BorderLayout.SOUTH);

        centerPanel.add(actionsCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLbl, String subtext, Color accent) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(UITheme.BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_CARD_LABEL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLbl.setFont(UITheme.FONT_CARD_VALUE);
        valueLbl.setForeground(accent);

        JLabel subLbl = new JLabel(subtext);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    public void refreshMetrics() {
        lblTotalFarmersVal.setText(String.valueOf(dataManager.getTotalFarmers()));
        lblTotalLotsVal.setText(String.valueOf(dataManager.getTotalHarvestLots()));
        lblPendingQtyVal.setText(dataManager.getTotalPendingHarvestQty() + " kg");
        lblMarketDemandVal.setText(dataManager.getRemainingMarketDemand() + " kg");
        lblAvailableVehiclesVal.setText(String.valueOf(dataManager.getAvailableVehiclesCount()));
        lblStorageCapacityVal.setText(dataManager.getAvailableStorageCapacity() + " kg");
        lblTotalShipmentsVal.setText(String.valueOf(dataManager.getTotalShipmentsCount()));
    }

    private void executeProcessing() {
        if (dataManager.getTotalHarvestLots() == 0) {
            JOptionPane.showMessageDialog(this,
                "No harvest lots found in the system.\nPlease add harvest lots or load sample data first.",
                "No Data to Process", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dataManager.getTotalPendingHarvestQty() == 0) {
            JOptionPane.showMessageDialog(this,
                "All harvest lots are already 100% completed and allocated.\nNo pending quantities remain.",
                "Already Processed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SupplyChainService service = new SupplyChainService(
            dataManager.getHarvestLots(),
            dataManager.getMarkets(),
            dataManager.getStorageCenters(),
            dataManager.getVehicles(),
            dataManager.getRoadNetwork(),
            dataManager.getShipments()
        );

        ProcessResult result = service.processLots();
        dataManager.notifyDataChanged();

        // Show detailed result dialog
        JTextArea txt = new JTextArea(result.getSummaryText());
        txt.setFont(new Font("Consolas", Font.PLAIN, 12));
        txt.setEditable(false);
        JScrollPane scroll = new JScrollPane(txt);
        scroll.setPreferredSize(new Dimension(650, 400));

        JOptionPane.showMessageDialog(this, scroll,
            "Supply Chain Execution Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadSampleDataAction() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "This will load the demonstration dataset (5 Farmers, 3 Markets, 2 Stores, 3 Vehicles, 20 Lots).\nContinue?",
            "Load Sample Data", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SampleDataLoader.populateSampleData(dataManager);
            JOptionPane.showMessageDialog(this,
                "Sample data loaded successfully!\nClick 'PROCESS HARVEST LOTS' to run the optimization.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearDataAction() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear all data and start with an empty system?",
            "Confirm Clear Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.clearAllData();
            JOptionPane.showMessageDialog(this,
                "All data has been cleared. You can now enter fresh records.",
                "Data Cleared", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
