package ui;

import model.Market;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Market Management Panel for managing wholesale/retail market demand,
 * locations, and tracking remaining demand fulfillment.
 */
public class MarketPanel extends JPanel {
    private DataManager dataManager;

    // Form Fields
    private JTextField txtMarketId;
    private JTextField txtName;
    private JTextField txtLocation;
    private JTextField txtCrop;
    private JTextField txtQuantity;
    private JTextField txtSearch;

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    public MarketPanel(DataManager dataManager) {
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
        JLabel titleLbl = new JLabel("Market Demand Management");
        titleLbl.setFont(UITheme.FONT_PANEL_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);
        add(titleLbl, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);

        // LEFT: Form Card
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(320, 480));

        JLabel formTitle = new JLabel("Market & Crop Demand");
        formTitle.setFont(UITheme.FONT_BOLD);
        formTitle.setForeground(UITheme.PRIMARY_GREEN);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(10, 1, 4, 4));
        fieldsPanel.setOpaque(false);

        txtMarketId = UITheme.createTextField(15);
        txtName = UITheme.createTextField(15);
        txtLocation = UITheme.createTextField(15);
        txtCrop = UITheme.createTextField(15);
        txtQuantity = UITheme.createTextField(15);

        fieldsPanel.add(UITheme.createFormLabel("Market ID: * (e.g. M01 or M01-TOM)"));
        fieldsPanel.add(txtMarketId);
        fieldsPanel.add(UITheme.createFormLabel("Market Name: * (e.g. Green Market)"));
        fieldsPanel.add(txtName);
        fieldsPanel.add(UITheme.createFormLabel("Location Node: * (e.g. MARKET1)"));
        fieldsPanel.add(txtLocation);
        fieldsPanel.add(UITheme.createFormLabel("Required Crop: * (e.g. Tomato)"));
        fieldsPanel.add(txtCrop);
        fieldsPanel.add(UITheme.createFormLabel("Required Quantity (kg): *"));
        fieldsPanel.add(txtQuantity);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Market");
        btnAdd.addActionListener(e -> addMarket());

        JButton btnUpdate = UITheme.createSecondaryButton("Update");
        btnUpdate.addActionListener(e -> updateMarket());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteMarket());

        JButton btnClear = UITheme.createSecondaryButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        formCard.add(btnPanel, BorderLayout.SOUTH);
        mainContent.add(formCard, BorderLayout.WEST);

        // RIGHT: Table Card with Search & Refresh
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout(10, 10));

        JPanel searchBar = new JPanel(new BorderLayout(8, 8));
        searchBar.setOpaque(false);

        JPanel searchInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchInputPanel.setOpaque(false);
        searchInputPanel.add(new JLabel("Search:"));
        txtSearch = UITheme.createTextField(16);
        searchInputPanel.add(txtSearch);

        JButton btnSearch = UITheme.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> searchMarkets());
        searchInputPanel.add(btnSearch);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Table");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            refreshTable();
        });
        searchInputPanel.add(btnRefresh);

        searchBar.add(searchInputPanel, BorderLayout.WEST);
        tableCard.add(searchBar, BorderLayout.NORTH);

        // JTable
        String[] cols = {
            "Market ID", "Market Name", "Location", "Required Crop",
            "Initial Demand (kg)", "Remaining Demand (kg)", "Fulfillment %"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Row Selection -> populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < table.getRowCount()) {
                String mId = tableModel.getValueAt(row, 0).toString();
                Market m = dataManager.findMarketById(mId);
                if (m != null) {
                    txtMarketId.setText(m.getMarketId());
                    txtName.setText(m.getName());
                    txtLocation.setText(m.getLocation());
                    txtCrop.setText(m.getCrop());
                    txtQuantity.setText(String.valueOf(m.getInitialDemand()));
                    txtMarketId.setEditable(false);
                }
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        mainContent.add(tableCard, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private void addMarket() {
        String id = txtMarketId.getText().trim();
        String name = txtName.getText().trim();
        String loc = txtLocation.getText().trim();
        String crop = txtCrop.getText().trim();
        String qtyStr = txtQuantity.getText().trim();

        if (id.isEmpty() || name.isEmpty() || loc.isEmpty() || crop.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dataManager.findMarketById(id) != null) {
            JOptionPane.showMessageDialog(this, "A Market with ID '" + id + "' already exists.",
                "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Required quantity must be a positive integer in kg.",
                "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Market market = new Market(id, name, loc, crop, qty);
        dataManager.getMarkets().add(market);
        dataManager.getRoadNetwork().ensureNodeConnected(loc);
        dataManager.notifyDataChanged();

        clearForm();
        JOptionPane.showMessageDialog(this, "Market demand saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateMarket() {
        String id = txtMarketId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a market to update.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Market market = dataManager.findMarketById(id);
        if (market == null) {
            JOptionPane.showMessageDialog(this, "Market ID '" + id + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        String loc = txtLocation.getText().trim();
        String crop = txtCrop.getText().trim();
        String qtyStr = txtQuantity.getText().trim();

        if (name.isEmpty() || loc.isEmpty() || crop.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Required quantity must be a positive integer.",
                "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        market.setName(name);
        market.setLocation(loc);
        market.setCrop(crop);
        market.setInitialDemand(qty);
        dataManager.getRoadNetwork().ensureNodeConnected(loc);
        dataManager.notifyDataChanged();

        clearForm();
        JOptionPane.showMessageDialog(this, "Market updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteMarket() {
        String id = txtMarketId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a market to delete.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Market market = dataManager.findMarketById(id);
        if (market == null) {
            JOptionPane.showMessageDialog(this, "Market ID '" + id + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete market entry: " + market.getName() + " (" + market.getCrop() + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.getMarkets().remove(market);
            dataManager.notifyDataChanged();
            clearForm();
            JOptionPane.showMessageDialog(this, "Market entry deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearForm() {
        txtMarketId.setText("");
        txtName.setText("");
        txtLocation.setText("");
        txtCrop.setText("");
        txtQuantity.setText("");
        txtMarketId.setEditable(true);
        table.clearSelection();
    }

    private void searchMarkets() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        tableModel.setRowCount(0);
        for (Market m : dataManager.getMarkets()) {
            if (m.getMarketId().toLowerCase().contains(query) ||
                m.getName().toLowerCase().contains(query) ||
                m.getLocation().toLowerCase().contains(query) ||
                m.getCrop().toLowerCase().contains(query)) {

                int fulfilled = m.getInitialDemand() - m.getRemainingDemand();
                double pct = m.getInitialDemand() > 0 ? (fulfilled * 100.0 / m.getInitialDemand()) : 0.0;

                tableModel.addRow(new Object[]{
                    m.getMarketId(), m.getName(), m.getLocation(), m.getCrop(),
                    m.getInitialDemand(), m.getRemainingDemand(),
                    String.format("%.1f %%", pct)
                });
            }
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Market m : dataManager.getMarkets()) {
            int fulfilled = m.getInitialDemand() - m.getRemainingDemand();
            double pct = m.getInitialDemand() > 0 ? (fulfilled * 100.0 / m.getInitialDemand()) : 0.0;

            tableModel.addRow(new Object[]{
                m.getMarketId(), m.getName(), m.getLocation(), m.getCrop(),
                m.getInitialDemand(), m.getRemainingDemand(),
                String.format("%.1f %%", pct)
            });
        }
    }
}
