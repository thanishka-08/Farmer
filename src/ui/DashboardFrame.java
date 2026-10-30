package ui;

import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window for Agricultural Supply Chain Management System.
 * Hosts the navigation sidebar and CardLayout views for all 8 application modules.
 */
public class DashboardFrame extends JFrame {
    private DataManager dataManager;

    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private Map<String, JButton> navButtons = new HashMap<>();
    private String currentView = "Dashboard";

    // Panel references
    private DashboardPanel dashboardPanel;
    private FarmerPanel farmerPanel;
    private HarvestLotPanel harvestLotPanel;
    private MarketPanel marketPanel;
    private StoragePanel storagePanel;
    private VehiclePanel vehiclePanel;
    private ShipmentPanel shipmentPanel;
    private ReportPanel reportPanel;

    public DashboardFrame() {
        super("Agricultural Supply Chain Management System");

        // Initialize DataManager (loads existing CSV data if available)
        dataManager = new DataManager();

        initComponents();

        // Save data on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dataManager.saveAll();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG_LIGHT);

        // ==========================================
        // LEFT SIDEBAR NAVIGATION
        // ==========================================
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(UITheme.PRIMARY_DARK);
        sidebar.setBorder(new EmptyBorder(15, 12, 15, 12));

        // Brand / App Header
        JPanel brandPanel = new JPanel(new BorderLayout(5, 5));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(5, 8, 20, 8));

        JLabel lblBrandTitle = new JLabel("AGRI-CHAIN");
        lblBrandTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBrandTitle.setForeground(Color.WHITE);

        JLabel lblBrandSub = new JLabel("Farmer Market Linkage");
        lblBrandSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBrandSub.setForeground(new Color(200, 230, 200));

        brandPanel.add(lblBrandTitle, BorderLayout.NORTH);
        brandPanel.add(lblBrandSub, BorderLayout.SOUTH);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Items List
        JPanel navList = new JPanel(new GridLayout(8, 1, 0, 6));
        navList.setOpaque(false);

        String[] navItems = {
            "Dashboard",
            "Farmer Management",
            "Harvest Lot Management",
            "Market Management",
            "Storage Center Management",
            "Vehicle Management",
            "Shipment Management",
            "Reports"
        };

        for (String item : navItems) {
            JButton btn = createNavButton(item);
            navButtons.put(item, btn);
            navList.add(btn);
        }
        sidebar.add(navList, BorderLayout.CENTER);

        // Sidebar Footer
        JPanel footerPanel = new JPanel(new BorderLayout(2, 2));
        footerPanel.setOpaque(false);
        JLabel lblVer = new JLabel("College Mini-Project v1.0", SwingConstants.CENTER);
        lblVer.setFont(UITheme.FONT_SMALL);
        lblVer.setForeground(new Color(180, 210, 180));
        footerPanel.add(lblVer, BorderLayout.SOUTH);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        rootPanel.add(sidebar, BorderLayout.WEST);

        // ==========================================
        // CENTER CARDS PANEL (CARDLAYOUT)
        // ==========================================
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(UITheme.BG_LIGHT);

        dashboardPanel = new DashboardPanel(dataManager, this::navigateTo);
        farmerPanel = new FarmerPanel(dataManager);
        harvestLotPanel = new HarvestLotPanel(dataManager);
        marketPanel = new MarketPanel(dataManager);
        storagePanel = new StoragePanel(dataManager);
        vehiclePanel = new VehiclePanel(dataManager);
        shipmentPanel = new ShipmentPanel(dataManager);
        reportPanel = new ReportPanel(dataManager);

        cardsPanel.add(dashboardPanel, "Dashboard");
        cardsPanel.add(farmerPanel, "Farmer Management");
        cardsPanel.add(harvestLotPanel, "Harvest Lot Management");
        cardsPanel.add(marketPanel, "Market Management");
        cardsPanel.add(storagePanel, "Storage Center Management");
        cardsPanel.add(vehiclePanel, "Vehicle Management");
        cardsPanel.add(shipmentPanel, "Shipment Management");
        cardsPanel.add(reportPanel, "Reports");

        rootPanel.add(cardsPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);
        updateNavSelection("Dashboard");
    }

    private JButton createNavButton(String name) {
        JButton btn = new JButton(name);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(40, 110, 45), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        btn.setBackground(UITheme.PRIMARY_DARK);
        btn.setForeground(Color.WHITE);

        btn.addActionListener(e -> navigateTo(name));
        return btn;
    }

    public void navigateTo(String viewName) {
        if (!navButtons.containsKey(viewName)) return;
        currentView = viewName;
        cardLayout.show(cardsPanel, viewName);
        updateNavSelection(viewName);
    }

    private void updateNavSelection(String activeName) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(activeName)) {
                btn.setBackground(UITheme.PRIMARY_LIGHT);
                btn.setForeground(UITheme.PRIMARY_DARK);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UITheme.ACCENT_GREEN, 2, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            } else {
                btn.setBackground(UITheme.PRIMARY_DARK);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(40, 110, 45), 1, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        }
    }
}
