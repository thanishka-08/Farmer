package ui;

import model.Farmer;
import storage.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Farmer Management Panel for registering, updating, deleting,
 * searching, and viewing farmer profiles.
 */
public class FarmerPanel extends JPanel {
    private DataManager dataManager;

    // Form Fields
    private JTextField txtFarmerId;
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtVillage;
    private JTextField txtSearch;

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    public FarmerPanel(DataManager dataManager) {
        this.dataManager = dataManager;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        refreshTable();

        dataManager.addChangeListener(this::refreshTable);
    }

    private void initComponents() {
        // TOP: Panel Title
        JLabel titleLbl = new JLabel("Farmer Management");
        titleLbl.setFont(UITheme.FONT_PANEL_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);
        add(titleLbl, BorderLayout.NORTH);

        // SPLIT / GRID: Left = Input Form, Right = Table + Search
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);

        // LEFT: Form Card
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(320, 450));

        JLabel formTitle = new JLabel("Farmer Details");
        formTitle.setFont(UITheme.FONT_BOLD);
        formTitle.setForeground(UITheme.PRIMARY_GREEN);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(8, 1, 4, 4));
        fieldsPanel.setOpaque(false);

        txtFarmerId = UITheme.createTextField(15);
        txtName = UITheme.createTextField(15);
        txtPhone = UITheme.createTextField(15);
        txtVillage = UITheme.createTextField(15);

        fieldsPanel.add(UITheme.createFormLabel("Farmer ID: * (e.g. F101)"));
        fieldsPanel.add(txtFarmerId);
        fieldsPanel.add(UITheme.createFormLabel("Farmer Name: *"));
        fieldsPanel.add(txtName);
        fieldsPanel.add(UITheme.createFormLabel("Phone Number:"));
        fieldsPanel.add(txtPhone);
        fieldsPanel.add(UITheme.createFormLabel("Village / Farm Node: (e.g. F01)"));
        fieldsPanel.add(txtVillage);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Form Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Farmer");
        btnAdd.addActionListener(e -> addFarmer());

        JButton btnUpdate = UITheme.createSecondaryButton("Update");
        btnUpdate.addActionListener(e -> updateFarmer());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteFarmer());

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

        // Search Bar Row
        JPanel searchBar = new JPanel(new BorderLayout(8, 8));
        searchBar.setOpaque(false);

        JPanel searchInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchInputPanel.setOpaque(false);
        searchInputPanel.add(new JLabel("Search:"));
        txtSearch = UITheme.createTextField(16);
        searchInputPanel.add(txtSearch);

        JButton btnSearch = UITheme.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> searchFarmers());
        searchInputPanel.add(btnSearch);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Table");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            refreshTable();
        });
        searchInputPanel.add(btnRefresh);

        searchBar.add(searchInputPanel, BorderLayout.WEST);
        tableCard.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] cols = { "Farmer ID", "Farmer Name", "Phone Number", "Village / Node" };
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
                txtFarmerId.setText(tableModel.getValueAt(row, 0).toString());
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtPhone.setText(tableModel.getValueAt(row, 2).toString());
                txtVillage.setText(tableModel.getValueAt(row, 3).toString());
                txtFarmerId.setEditable(false); // ID should not be changed during edit
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        mainContent.add(tableCard, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private void addFarmer() {
        String id = txtFarmerId.getText().trim();
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String village = txtVillage.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Farmer ID and Name are required fields.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dataManager.findFarmerById(id) != null) {
            JOptionPane.showMessageDialog(this, "A farmer with ID '" + id + "' already exists.",
                "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Farmer farmer = new Farmer(id, name, phone, village);
        dataManager.getFarmers().add(farmer);
        dataManager.notifyDataChanged();

        clearForm();
        JOptionPane.showMessageDialog(this, "Farmer added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateFarmer() {
        String id = txtFarmerId.getText().trim();
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String village = txtVillage.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a farmer to update.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Farmer farmer = dataManager.findFarmerById(id);
        if (farmer == null) {
            JOptionPane.showMessageDialog(this, "Farmer ID '" + id + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Farmer Name cannot be empty.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        farmer.setName(name);
        farmer.setPhone(phone);
        farmer.setVillage(village);
        dataManager.notifyDataChanged();

        clearForm();
        JOptionPane.showMessageDialog(this, "Farmer updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteFarmer() {
        String id = txtFarmerId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a farmer to delete.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Farmer farmer = dataManager.findFarmerById(id);
        if (farmer == null) {
            JOptionPane.showMessageDialog(this, "Farmer ID '" + id + "' not found.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete farmer: " + farmer.getName() + " (" + id + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.getFarmers().remove(farmer);
            dataManager.notifyDataChanged();
            clearForm();
            JOptionPane.showMessageDialog(this, "Farmer deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearForm() {
        txtFarmerId.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtVillage.setText("");
        txtFarmerId.setEditable(true);
        table.clearSelection();
    }

    private void searchFarmers() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        tableModel.setRowCount(0);
        for (Farmer f : dataManager.getFarmers()) {
            if (f.getFarmerId().toLowerCase().contains(query) ||
                f.getName().toLowerCase().contains(query) ||
                f.getVillage().toLowerCase().contains(query) ||
                f.getPhone().toLowerCase().contains(query)) {

                tableModel.addRow(new Object[]{
                    f.getFarmerId(), f.getName(), f.getPhone(), f.getVillage()
                });
            }
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Farmer f : dataManager.getFarmers()) {
            tableModel.addRow(new Object[]{
                f.getFarmerId(), f.getName(), f.getPhone(), f.getVillage()
            });
        }
    }
}
