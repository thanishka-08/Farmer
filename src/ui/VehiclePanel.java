package ui;

import model.Vehicle;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Vehicle Management Panel for tracking transport fleet,
 * vehicle numbers, capacities, locations, and operational availability.
 */
public class VehiclePanel extends JPanel {
    private DataManager dataManager;

    // Form Fields
    private JTextField txtVehicleId;
    private JTextField txtVehicleNumber;
    private JTextField txtCapacity;
    private JTextField txtLocation;
    private JComboBox<String> cbStatus;
    private JTextField txtSearch;

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    public VehiclePanel(DataManager dataManager) {
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
        JLabel titleLbl = new JLabel("Vehicle & Fleet Management");
        titleLbl.setFont(UITheme.FONT_PANEL_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);
        add(titleLbl, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);

        // LEFT: Form Card
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(320, 480));

        JLabel formTitle = new JLabel("Vehicle Details");
        formTitle.setFont(UITheme.FONT_BOLD);
        formTitle.setForeground(UITheme.PRIMARY_GREEN);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(10, 1, 4, 4));
        fieldsPanel.setOpaque(false);

        txtVehicleId = UITheme.createTextField(15);
        txtVehicleNumber = UITheme.createTextField(15);
        txtCapacity = UITheme.createTextField(15);
        txtLocation = UITheme.createTextField(15);
        cbStatus = new JComboBox<>(new String[]{ "Available", "In Transit", "Maintenance" });

        fieldsPanel.add(UITheme.createFormLabel("Vehicle ID: * (e.g. V01)"));
        fieldsPanel.add(txtVehicleId);
        fieldsPanel.add(UITheme.createFormLabel("Vehicle Number / Plate: * (e.g. KA-01-AB-1234)"));
        fieldsPanel.add(txtVehicleNumber);
        fieldsPanel.add(UITheme.createFormLabel("Payload Capacity in kg: *"));
        fieldsPanel.add(txtCapacity);
        fieldsPanel.add(UITheme.createFormLabel("Current Location Node: * (e.g. J1, J2)"));
        fieldsPanel.add(txtLocation);
        fieldsPanel.add(UITheme.createFormLabel("Availability Status: *"));
        fieldsPanel.add(cbStatus);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(3, 2, 6, 6));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Vehicle");
        btnAdd.addActionListener(e -> addVehicle());

        JButton btnUpdate = UITheme.createSecondaryButton("Update");
        btnUpdate.addActionListener(e -> updateVehicle());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteVehicle());

        JButton btnClear = UITheme.createSecondaryButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        JButton btnResetAll = UITheme.createSecondaryButton("Set All Available");
        btnResetAll.addActionListener(e -> resetAllVehiclesAvailable());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnResetAll);

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
        btnSearch.addActionListener(e -> searchVehicles());
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
            "Vehicle ID", "Vehicle Number", "Capacity (kg)", "Current Location", "Status"
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
                String id = tableModel.getValueAt(row, 0).toString();
                Vehicle v = dataManager.findVehicleById(id);
                if (v != null) {
                    txtVehicleId.setText(v.getId());
                    txtVehicleNumber.setText(v.getVehicleNumber());
                    txtCapacity.setText(String.valueOf(v.getCapacity()));
                    txtLocation.setText(v.getLocation());
                    cbStatus.setSelectedItem(v.getStatus());
                    txtVehicleId.setEditable(false);
                }
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        mainContent.add(tableCard, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private void addVehicle() {
        String id = txtVehicleId.getText().trim();
        String num = txtVehicleNumber.getText().trim();
        String capStr = txtCapacity.getText().trim();
        String loc = txtLocation.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        if (id.isEmpty() || capStr.isEmpty() || loc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vehicle ID, Capacity, and Location are required.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (num.isEmpty()) num = id;

        if (dataManager.findVehicleById(id) != null) {
            JOptionPane.showMessageDialog(this, "A Vehicle with ID '" + id + "' already exists.",
                "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cap;
        try {
            cap = Integer.parseInt(capStr);
            if (cap <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacity must be a positive integer in kg.",
                "Invalid Capacity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean avail = "Available".equalsIgnoreCase(status);
        Vehicle vehicle = new Vehicle(id, num, cap, loc, avail, status);
        dataManager.getVehicles().add(vehicle);
        dataManager.getRoadNetwork().ensureNodeConnected(loc);
        dataManager.notifyDataChanged();

        clearForm();
        JOptionPane.showMessageDialog(this, "Vehicle added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateVehicle() {
        String id = txtVehicleId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to update.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vehicle vehicle = dataManager.findVehicleById(id);
        if (vehicle == null) {
            JOptionPane.showMessageDialog(this, "Vehicle '" + id + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String num = txtVehicleNumber.getText().trim();
        String capStr = txtCapacity.getText().trim();
        String loc = txtLocation.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        if (capStr.isEmpty() || loc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Capacity and Location cannot be empty.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cap;
        try {
            cap = Integer.parseInt(capStr);
            if (cap <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacity must be a positive integer.",
                "Invalid Capacity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!num.isEmpty()) vehicle.setVehicleNumber(num);
        vehicle.setCapacity(cap);
        vehicle.setLocation(loc);
        vehicle.setStatus(status);
        dataManager.getRoadNetwork().ensureNodeConnected(loc);
        dataManager.notifyDataChanged();

        clearForm();
        JOptionPane.showMessageDialog(this, "Vehicle updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteVehicle() {
        String id = txtVehicleId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to delete.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vehicle vehicle = dataManager.findVehicleById(id);
        if (vehicle == null) {
            JOptionPane.showMessageDialog(this, "Vehicle '" + id + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete vehicle: " + vehicle.getVehicleNumber() + " (" + id + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.getVehicles().remove(vehicle);
            dataManager.notifyDataChanged();
            clearForm();
            JOptionPane.showMessageDialog(this, "Vehicle deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetAllVehiclesAvailable() {
        for (Vehicle v : dataManager.getVehicles()) {
            v.setAvailable(true);
            v.setStatus("Available");
        }
        dataManager.notifyDataChanged();
        JOptionPane.showMessageDialog(this, "All vehicles reset to 'Available' status.",
            "Fleet Available", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearForm() {
        txtVehicleId.setText("");
        txtVehicleNumber.setText("");
        txtCapacity.setText("");
        txtLocation.setText("");
        cbStatus.setSelectedIndex(0);
        txtVehicleId.setEditable(true);
        table.clearSelection();
    }

    private void searchVehicles() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        tableModel.setRowCount(0);
        for (Vehicle v : dataManager.getVehicles()) {
            if (v.getId().toLowerCase().contains(query) ||
                v.getVehicleNumber().toLowerCase().contains(query) ||
                v.getLocation().toLowerCase().contains(query) ||
                v.getStatus().toLowerCase().contains(query)) {

                tableModel.addRow(new Object[]{
                    v.getId(), v.getVehicleNumber(), v.getCapacity(),
                    v.getLocation(), v.getStatus()
                });
            }
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Vehicle v : dataManager.getVehicles()) {
            tableModel.addRow(new Object[]{
                v.getId(), v.getVehicleNumber(), v.getCapacity(),
                v.getLocation(), v.getStatus()
            });
        }
    }
}
