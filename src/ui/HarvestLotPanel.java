package ui;

import model.Farmer;
import model.HarvestLot;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Harvest Lot Management Panel for adding, updating, deleting,
 * searching, and viewing crop lots from farmers.
 */
public class HarvestLotPanel extends JPanel {
    private DataManager dataManager;

    // Form Fields
    private JTextField txtLotId;
    private JComboBox<FarmerItem> cbFarmer;
    private JTextField txtCrop;
    private JTextField txtQuantity;
    private JComboBox<Integer> cbUrgency;
    private JTextField txtDeadline;
    private JTextField txtLocation;
    private JTextField txtSearch;

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    public HarvestLotPanel(DataManager dataManager) {
        this.dataManager = dataManager;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        refreshFarmerDropdown();
        refreshTable();

        dataManager.addChangeListener(() -> {
            refreshFarmerDropdown();
            refreshTable();
        });
    }

    private static class FarmerItem {
        Farmer farmer;
        FarmerItem(Farmer farmer) { this.farmer = farmer; }
        @Override
        public String toString() {
            if (farmer == null) return "-- Select Farmer --";
            return farmer.getFarmerId() + " - " + farmer.getName();
        }
    }

    private void initComponents() {
        // TOP: Header
        JLabel titleLbl = new JLabel("Harvest Lot Management");
        titleLbl.setFont(UITheme.FONT_PANEL_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);
        add(titleLbl, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);

        // LEFT: Form Card
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(340, 520));

        JLabel formTitle = new JLabel("Harvest Lot Details");
        formTitle.setFont(UITheme.FONT_BOLD);
        formTitle.setForeground(UITheme.PRIMARY_GREEN);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(14, 1, 3, 3));
        fieldsPanel.setOpaque(false);

        txtLotId = UITheme.createTextField(15);
        cbFarmer = new JComboBox<>();
        txtCrop = UITheme.createTextField(15);
        txtQuantity = UITheme.createTextField(15);
        cbUrgency = new JComboBox<>(new Integer[]{ 1, 2, 3, 4, 5 });
        cbUrgency.setSelectedItem(3);
        txtDeadline = UITheme.createTextField(15);
        txtLocation = UITheme.createTextField(15);

        fieldsPanel.add(UITheme.createFormLabel("Lot ID: * (e.g. LOT01)"));
        fieldsPanel.add(txtLotId);
        fieldsPanel.add(UITheme.createFormLabel("Select Farmer: *"));
        fieldsPanel.add(cbFarmer);
        fieldsPanel.add(UITheme.createFormLabel("Crop Name: * (e.g. Tomato, Rice)"));
        fieldsPanel.add(txtCrop);
        fieldsPanel.add(UITheme.createFormLabel("Quantity in kg: *"));
        fieldsPanel.add(txtQuantity);
        fieldsPanel.add(UITheme.createFormLabel("Urgency Level: (1 = Low, 5 = High)"));
        fieldsPanel.add(cbUrgency);
        fieldsPanel.add(UITheme.createFormLabel("Harvest Deadline in hours: * (e.g. 12)"));
        fieldsPanel.add(txtDeadline);
        fieldsPanel.add(UITheme.createFormLabel("Harvest Location: (e.g. F01)"));
        fieldsPanel.add(txtLocation);

        formCard.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);

        // Form Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Lot");
        btnAdd.addActionListener(e -> addLot());

        JButton btnUpdate = UITheme.createSecondaryButton("Update");
        btnUpdate.addActionListener(e -> updateLot());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteLot());

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

        JPanel searchInputPanel = new FlowLayout(FlowLayout.LEFT, 6, 0) == null ? new JPanel() : new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchInputPanel.setOpaque(false);
        searchInputPanel.add(new JLabel("Search:"));
        txtSearch = UITheme.createTextField(16);
        searchInputPanel.add(txtSearch);

        JButton btnSearch = UITheme.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> searchLots());
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
            "Lot ID", "Farmer ID", "Farmer Name", "Crop",
            "Orig Qty (kg)", "Proc Qty (kg)", "Rem Qty (kg)",
            "Urgency", "Deadline (h)", "Location", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Selection listener to populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < table.getRowCount()) {
                String lotId = tableModel.getValueAt(row, 0).toString();
                HarvestLot lot = dataManager.findLotById(lotId);
                if (lot != null) {
                    txtLotId.setText(lot.getLotId());
                    txtCrop.setText(lot.getCrop());
                    txtQuantity.setText(String.valueOf(lot.getOriginalQuantity()));
                    cbUrgency.setSelectedItem(lot.getUrgency());
                    txtDeadline.setText(String.valueOf(lot.getDeadline()));
                    txtLocation.setText(lot.getLocation());
                    txtLotId.setEditable(false);

                    // Select farmer in dropdown
                    for (int i = 0; i < cbFarmer.getItemCount(); i++) {
                        FarmerItem item = cbFarmer.getItemAt(i);
                        if (item.farmer != null && lot.getFarmer() != null &&
                            item.farmer.getFarmerId().equalsIgnoreCase(lot.getFarmer().getFarmerId())) {
                            cbFarmer.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        mainContent.add(tableCard, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private void refreshFarmerDropdown() {
        cbFarmer.removeAllItems();
        cbFarmer.addItem(new FarmerItem(null));
        for (Farmer f : dataManager.getFarmers()) {
            cbFarmer.addItem(new FarmerItem(f));
        }
    }

    private void addLot() {
        String lotId = txtLotId.getText().trim();
        FarmerItem fItem = (FarmerItem) cbFarmer.getSelectedItem();
        String crop = txtCrop.getText().trim();
        String qtyStr = txtQuantity.getText().trim();
        String deadlineStr = txtDeadline.getText().trim();
        String location = txtLocation.getText().trim();
        int urgency = (Integer) cbUrgency.getSelectedItem();

        if (lotId.isEmpty() || crop.isEmpty() || qtyStr.isEmpty() || deadlineStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (Lot ID, Crop, Qty, Deadline).",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (fItem == null || fItem.farmer == null) {
            JOptionPane.showMessageDialog(this, "Please select a registered Farmer from the dropdown.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dataManager.findLotById(lotId) != null) {
            JOptionPane.showMessageDialog(this, "A Harvest Lot with ID '" + lotId + "' already exists.",
                "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int qty, deadline;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive integer in kg.",
                "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            deadline = Integer.parseInt(deadlineStr);
            if (deadline <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Deadline must be a positive integer in hours.",
                "Invalid Deadline", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (location.isEmpty()) {
            location = fItem.farmer.getVillage().isEmpty() ? "F01" : fItem.farmer.getVillage();
        }

        HarvestLot lot = new HarvestLot(lotId, fItem.farmer, crop, qty, urgency, deadline, location);
        dataManager.getHarvestLots().add(lot);
        dataManager.notifyDataChanged();

        clearForm();
        JOptionPane.showMessageDialog(this, "Harvest Lot saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateLot() {
        String lotId = txtLotId.getText().trim();
        if (lotId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a harvest lot to update.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HarvestLot lot = dataManager.findLotById(lotId);
        if (lot == null) {
            JOptionPane.showMessageDialog(this, "Harvest Lot '" + lotId + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FarmerItem fItem = (FarmerItem) cbFarmer.getSelectedItem();
        String crop = txtCrop.getText().trim();
        String qtyStr = txtQuantity.getText().trim();
        String deadlineStr = txtDeadline.getText().trim();
        String location = txtLocation.getText().trim();
        int urgency = (Integer) cbUrgency.getSelectedItem();

        if (crop.isEmpty() || qtyStr.isEmpty() || deadlineStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Crop, Quantity, and Deadline cannot be empty.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int qty, deadline;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive integer.",
                "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            deadline = Integer.parseInt(deadlineStr);
            if (deadline <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Deadline must be a positive integer.",
                "Invalid Deadline", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (fItem != null && fItem.farmer != null) {
            lot.setFarmer(fItem.farmer);
        }
        lot.setCrop(crop);
        lot.setOriginalQuantity(qty);
        lot.setUrgency(urgency);
        lot.setDeadline(deadline);
        if (!location.isEmpty()) {
            lot.setLocation(location);
        }

        dataManager.notifyDataChanged();
        clearForm();
        JOptionPane.showMessageDialog(this, "Harvest Lot updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteLot() {
        String lotId = txtLotId.getText().trim();
        if (lotId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a harvest lot to delete.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HarvestLot lot = dataManager.findLotById(lotId);
        if (lot == null) {
            JOptionPane.showMessageDialog(this, "Harvest Lot '" + lotId + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete Lot: " + lotId + " (" + lot.getCrop() + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.getHarvestLots().remove(lot);
            dataManager.notifyDataChanged();
            clearForm();
            JOptionPane.showMessageDialog(this, "Harvest Lot deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearForm() {
        txtLotId.setText("");
        txtCrop.setText("");
        txtQuantity.setText("");
        cbUrgency.setSelectedItem(3);
        txtDeadline.setText("");
        txtLocation.setText("");
        txtLotId.setEditable(true);
        if (cbFarmer.getItemCount() > 0) cbFarmer.setSelectedIndex(0);
        table.clearSelection();
    }

    private void searchLots() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        tableModel.setRowCount(0);
        for (HarvestLot l : dataManager.getHarvestLots()) {
            String fName = l.getFarmer() != null ? l.getFarmer().getName() : "";
            String fId = l.getFarmer() != null ? l.getFarmer().getFarmerId() : "";

            if (l.getLotId().toLowerCase().contains(query) ||
                l.getCrop().toLowerCase().contains(query) ||
                fName.toLowerCase().contains(query) ||
                fId.toLowerCase().contains(query) ||
                l.getLocation().toLowerCase().contains(query) ||
                l.getStatus().toLowerCase().contains(query)) {

                tableModel.addRow(new Object[]{
                    l.getLotId(), fId, fName, l.getCrop(),
                    l.getOriginalQuantity(), l.getProcessedQuantity(), l.getRemainingQuantity(),
                    l.getUrgency(), l.getDeadline(), l.getLocation(), l.getStatus()
                });
            }
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (HarvestLot l : dataManager.getHarvestLots()) {
            String fName = l.getFarmer() != null ? l.getFarmer().getName() : "";
            String fId = l.getFarmer() != null ? l.getFarmer().getFarmerId() : "";

            tableModel.addRow(new Object[]{
                l.getLotId(), fId, fName, l.getCrop(),
                l.getOriginalQuantity(), l.getProcessedQuantity(), l.getRemainingQuantity(),
                l.getUrgency(), l.getDeadline(), l.getLocation(), l.getStatus()
            });
        }
    }
}
