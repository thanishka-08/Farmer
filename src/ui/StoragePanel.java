package ui;

import model.ColdStore;
import model.DryStore;
import model.StorageCenter;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Storage Center Management Panel for managing Cold and Dry storage facilities,
 * tracking total, occupied, and available capacities, and supported crop types.
 */
public class StoragePanel extends JPanel {
    private DataManager dataManager;

    // Form Fields
    private JTextField txtStorageId;
    private JTextField txtName;
    private JTextField txtLocation;
    private JComboBox<String> cbType;
    private JTextField txtCapacity;
    private JTextField txtSupportedCrops;
    private JTextField txtSearch;

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    public StoragePanel(DataManager dataManager) {
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
        JLabel titleLbl = new JLabel("Storage Center Management");
        titleLbl.setFont(UITheme.FONT_PANEL_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);
        add(titleLbl, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);

        // LEFT: Form Card
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(340, 500));

        JLabel formTitle = new JLabel("Storage Facility Details");
        formTitle.setFont(UITheme.FONT_BOLD);
        formTitle.setForeground(UITheme.PRIMARY_GREEN);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(12, 1, 3, 3));
        fieldsPanel.setOpaque(false);

        txtStorageId = UITheme.createTextField(15);
        txtName = UITheme.createTextField(15);
        txtLocation = UITheme.createTextField(15);
        cbType = new JComboBox<>(new String[]{ "Cold Storage", "Dry Storage" });
        txtCapacity = UITheme.createTextField(15);
        txtSupportedCrops = UITheme.createTextField(15);
        txtSupportedCrops.setText("Tomato, Onion, Carrot, Apple"); // default for Cold Storage

        // Change default hint when storage type changes
        cbType.addActionListener(e -> {
            String selected = (String) cbType.getSelectedItem();
            if ("Dry Storage".equals(selected)) {
                if (txtSupportedCrops.getText().contains("Tomato") || txtSupportedCrops.getText().isEmpty()) {
                    txtSupportedCrops.setText("Rice, Paddy, Wheat, Grain");
                }
            } else {
                if (txtSupportedCrops.getText().contains("Rice") || txtSupportedCrops.getText().isEmpty()) {
                    txtSupportedCrops.setText("Tomato, Onion, Carrot, Apple");
                }
            }
        });

        fieldsPanel.add(UITheme.createFormLabel("Storage ID: * (e.g. S01)"));
        fieldsPanel.add(txtStorageId);
        fieldsPanel.add(UITheme.createFormLabel("Storage Name: * (e.g. Cold Storage)"));
        fieldsPanel.add(txtName);
        fieldsPanel.add(UITheme.createFormLabel("Location Node: * (e.g. STORE1)"));
        fieldsPanel.add(txtLocation);
        fieldsPanel.add(UITheme.createFormLabel("Storage Type: *"));
        fieldsPanel.add(cbType);
        fieldsPanel.add(UITheme.createFormLabel("Total Capacity in kg: *"));
        fieldsPanel.add(txtCapacity);
        fieldsPanel.add(UITheme.createFormLabel("Supported Crops: (comma-separated)"));
        fieldsPanel.add(txtSupportedCrops);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Storage");
        btnAdd.addActionListener(e -> addStorage());

        JButton btnUpdate = UITheme.createSecondaryButton("Update");
        btnUpdate.addActionListener(e -> updateStorage());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteStorage());

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
        btnSearch.addActionListener(e -> searchStorage());
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
            "Storage ID", "Storage Name", "Location", "Type",
            "Total Cap (kg)", "Occupied (kg)", "Available (kg)", "Supported Crops"
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
                StorageCenter s = dataManager.findStorageById(id);
                if (s != null) {
                    txtStorageId.setText(s.getId());
                    txtName.setText(s.getName());
                    txtLocation.setText(s.getLocation());
                    cbType.setSelectedItem(s.getStorageType());
                    txtCapacity.setText(String.valueOf(s.getCapacity()));
                    txtSupportedCrops.setText(s.getSupportedCropsDisplay());
                    txtStorageId.setEditable(false);
                }
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        mainContent.add(tableCard, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private void addStorage() {
        String id = txtStorageId.getText().trim();
        String name = txtName.getText().trim();
        String loc = txtLocation.getText().trim();
        String type = (String) cbType.getSelectedItem();
        String capStr = txtCapacity.getText().trim();
        String crops = txtSupportedCrops.getText().trim();

        if (id.isEmpty() || name.isEmpty() || loc.isEmpty() || capStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dataManager.findStorageById(id) != null) {
            JOptionPane.showMessageDialog(this, "A Storage Center with ID '" + id + "' already exists.",
                "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cap;
        try {
            cap = Integer.parseInt(capStr);
            if (cap <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total capacity must be a positive integer in kg.",
                "Invalid Capacity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StorageCenter center;
        if ("Dry Storage".equalsIgnoreCase(type)) {
            center = new DryStore(id, name, loc, cap, 0, crops);
        } else {
            center = new ColdStore(id, name, loc, cap, 0, crops);
        }

        dataManager.getStorageCenters().add(center);
        dataManager.getRoadNetwork().ensureNodeConnected(loc);
        dataManager.notifyDataChanged();

        clearForm();
        JOptionPane.showMessageDialog(this, "Storage Center added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStorage() {
        String id = txtStorageId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a storage center to update.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StorageCenter center = dataManager.findStorageById(id);
        if (center == null) {
            JOptionPane.showMessageDialog(this, "Storage Center '" + id + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        String loc = txtLocation.getText().trim();
        String capStr = txtCapacity.getText().trim();
        String crops = txtSupportedCrops.getText().trim();

        if (name.isEmpty() || loc.isEmpty() || capStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Location, and Capacity cannot be empty.",
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

        center.setName(name);
        center.setLocation(loc);
        center.setCapacity(cap);
        center.setCustomSupportedCrops(crops);
        dataManager.getRoadNetwork().ensureNodeConnected(loc);
        dataManager.notifyDataChanged();

        clearForm();
        JOptionPane.showMessageDialog(this, "Storage Center updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteStorage() {
        String id = txtStorageId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a storage center to delete.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StorageCenter center = dataManager.findStorageById(id);
        if (center == null) {
            JOptionPane.showMessageDialog(this, "Storage Center '" + id + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete storage facility: " + center.getName() + " (" + id + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.getStorageCenters().remove(center);
            dataManager.notifyDataChanged();
            clearForm();
            JOptionPane.showMessageDialog(this, "Storage facility deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearForm() {
        txtStorageId.setText("");
        txtName.setText("");
        txtLocation.setText("");
        cbType.setSelectedIndex(0);
        txtCapacity.setText("");
        txtSupportedCrops.setText("Tomato, Onion, Carrot, Apple");
        txtStorageId.setEditable(true);
        table.clearSelection();
    }

    private void searchStorage() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        tableModel.setRowCount(0);
        for (StorageCenter s : dataManager.getStorageCenters()) {
            if (s.getId().toLowerCase().contains(query) ||
                s.getName().toLowerCase().contains(query) ||
                s.getLocation().toLowerCase().contains(query) ||
                s.getStorageType().toLowerCase().contains(query) ||
                s.getSupportedCropsDisplay().toLowerCase().contains(query)) {

                tableModel.addRow(new Object[]{
                    s.getId(), s.getName(), s.getLocation(), s.getStorageType(),
                    s.getCapacity(), s.getUsedCapacity(), s.availableCapacity(),
                    s.getSupportedCropsDisplay()
                });
            }
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (StorageCenter s : dataManager.getStorageCenters()) {
            tableModel.addRow(new Object[]{
                s.getId(), s.getName(), s.getLocation(), s.getStorageType(),
                s.getCapacity(), s.getUsedCapacity(), s.availableCapacity(),
                s.getSupportedCropsDisplay()
            });
        }
    }
}
