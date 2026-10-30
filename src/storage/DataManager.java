package storage;

import model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Manages persistent storage and in-memory collections of all entities.
 * Saves and restores data using clean CSV files in the data/ directory.
 * Separates data persistence from the Swing presentation layer.
 */
public class DataManager {
    private static final String DATA_DIR = "data";
    private static final String FARMERS_FILE = DATA_DIR + File.separator + "farmers.csv";
    private static final String LOTS_FILE = DATA_DIR + File.separator + "harvest_lots.csv";
    private static final String MARKETS_FILE = DATA_DIR + File.separator + "markets.csv";
    private static final String STORAGE_FILE = DATA_DIR + File.separator + "storage_centers.csv";
    private static final String VEHICLES_FILE = DATA_DIR + File.separator + "vehicles.csv";
    private static final String SHIPMENTS_FILE = DATA_DIR + File.separator + "shipments.csv";

    private List<Farmer> farmers = new ArrayList<>();
    private List<HarvestLot> harvestLots = new ArrayList<>();
    private List<Market> markets = new ArrayList<>();
    private List<StorageCenter> storageCenters = new ArrayList<>();
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Shipment> shipments = new ArrayList<>();
    private RoadNetwork roadNetwork = new RoadNetwork();

    private List<Runnable> changeListeners = new ArrayList<>();

    public DataManager() {
        ensureDataDirectory();
        loadAll();
    }

    private void ensureDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null) {
            changeListeners.add(listener);
        }
    }

    public void notifyDataChanged() {
        saveAll();
        for (Runnable r : changeListeners) {
            try {
                r.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================================
    // PERSISTENCE - LOAD OPERATIONS
    // ==========================================
    public void loadAll() {
        farmers.clear();
        harvestLots.clear();
        markets.clear();
        storageCenters.clear();
        vehicles.clear();
        shipments.clear();

        loadFarmers();
        loadMarkets();
        loadStorageCenters();
        loadVehicles();
        loadHarvestLots(); // Depends on loaded farmers
        loadShipments();
    }

    private void loadFarmers() {
        File f = new File(FARMERS_FILE);
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Farmer farmer = Farmer.fromCsv(line);
                if (farmer != null) {
                    farmers.add(farmer);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading farmers: " + e.getMessage());
        }
    }

    private void loadHarvestLots() {
        File f = new File(LOTS_FILE);
        if (!f.exists()) return;
        Map<String, Farmer> fMap = new HashMap<>();
        for (Farmer farmer : farmers) {
            fMap.put(farmer.getFarmerId(), farmer);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                HarvestLot lot = HarvestLot.fromCsv(line, fMap);
                if (lot != null) {
                    harvestLots.add(lot);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading harvest lots: " + e.getMessage());
        }
    }

    private void loadMarkets() {
        File f = new File(MARKETS_FILE);
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Market m = Market.fromCsv(line);
                if (m != null) {
                    markets.add(m);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading markets: " + e.getMessage());
        }
    }

    private void loadStorageCenters() {
        File f = new File(STORAGE_FILE);
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                StorageCenter s = StorageCenter.fromCsv(line);
                if (s != null) {
                    storageCenters.add(s);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading storage centers: " + e.getMessage());
        }
    }

    private void loadVehicles() {
        File f = new File(VEHICLES_FILE);
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Vehicle v = Vehicle.fromCsv(line);
                if (v != null) {
                    vehicles.add(v);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading vehicles: " + e.getMessage());
        }
    }

    private void loadShipments() {
        File f = new File(SHIPMENTS_FILE);
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Shipment s = Shipment.fromCsv(line);
                if (s != null) {
                    shipments.add(s);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading shipments: " + e.getMessage());
        }
    }

    // ==========================================
    // PERSISTENCE - SAVE OPERATIONS
    // ==========================================
    public synchronized void saveAll() {
        ensureDataDirectory();
        saveFarmers();
        saveHarvestLots();
        saveMarkets();
        saveStorageCenters();
        saveVehicles();
        saveShipments();
    }

    public synchronized void saveFarmers() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FARMERS_FILE), StandardCharsets.UTF_8))) {
            for (Farmer f : farmers) {
                writer.println(f.toCsv());
            }
        } catch (Exception e) {
            System.err.println("Error saving farmers: " + e.getMessage());
        }
    }

    public synchronized void saveHarvestLots() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(LOTS_FILE), StandardCharsets.UTF_8))) {
            for (HarvestLot l : harvestLots) {
                writer.println(l.toCsv());
            }
        } catch (Exception e) {
            System.err.println("Error saving harvest lots: " + e.getMessage());
        }
    }

    public synchronized void saveMarkets() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(MARKETS_FILE), StandardCharsets.UTF_8))) {
            for (Market m : markets) {
                writer.println(m.toCsv());
            }
        } catch (Exception e) {
            System.err.println("Error saving markets: " + e.getMessage());
        }
    }

    public synchronized void saveStorageCenters() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(STORAGE_FILE), StandardCharsets.UTF_8))) {
            for (StorageCenter s : storageCenters) {
                writer.println(s.toCsv());
            }
        } catch (Exception e) {
            System.err.println("Error saving storage centers: " + e.getMessage());
        }
    }

    public synchronized void saveVehicles() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(VEHICLES_FILE), StandardCharsets.UTF_8))) {
            for (Vehicle v : vehicles) {
                writer.println(v.toCsv());
            }
        } catch (Exception e) {
            System.err.println("Error saving vehicles: " + e.getMessage());
        }
    }

    public synchronized void saveShipments() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(SHIPMENTS_FILE), StandardCharsets.UTF_8))) {
            for (Shipment s : shipments) {
                writer.println(s.toCsv());
            }
        } catch (Exception e) {
            System.err.println("Error saving shipments: " + e.getMessage());
        }
    }

    public void clearAllData() {
        farmers.clear();
        harvestLots.clear();
        markets.clear();
        storageCenters.clear();
        vehicles.clear();
        shipments.clear();
        roadNetwork = new RoadNetwork();
        saveAll();
        notifyDataChanged();
    }

    // ==========================================
    // ENTITY ACCESSORS AND LOOKUPS
    // ==========================================
    public List<Farmer> getFarmers() { return farmers; }
    public List<HarvestLot> getHarvestLots() { return harvestLots; }
    public List<Market> getMarkets() { return markets; }
    public List<StorageCenter> getStorageCenters() { return storageCenters; }
    public List<Vehicle> getVehicles() { return vehicles; }
    public List<Shipment> getShipments() { return shipments; }
    public RoadNetwork getRoadNetwork() { return roadNetwork; }

    public Farmer findFarmerById(String id) {
        if (id == null) return null;
        for (Farmer f : farmers) {
            if (f.getFarmerId().equalsIgnoreCase(id.trim())) return f;
        }
        return null;
    }

    public HarvestLot findLotById(String id) {
        if (id == null) return null;
        for (HarvestLot l : harvestLots) {
            if (l.getLotId().equalsIgnoreCase(id.trim())) return l;
        }
        return null;
    }

    public Market findMarketById(String id) {
        if (id == null) return null;
        for (Market m : markets) {
            if (m.getMarketId().equalsIgnoreCase(id.trim())) return m;
        }
        return null;
    }

    public StorageCenter findStorageById(String id) {
        if (id == null) return null;
        for (StorageCenter s : storageCenters) {
            if (s.getId().equalsIgnoreCase(id.trim())) return s;
        }
        return null;
    }

    public Vehicle findVehicleById(String id) {
        if (id == null) return null;
        for (Vehicle v : vehicles) {
            if (v.getId().equalsIgnoreCase(id.trim())) return v;
        }
        return null;
    }

    // ==========================================
    // DASHBOARD SUMMARY METRICS
    // ==========================================
    public int getTotalFarmers() {
        return farmers.size();
    }

    public int getTotalHarvestLots() {
        return harvestLots.size();
    }

    public int getTotalPendingHarvestQty() {
        int total = 0;
        for (HarvestLot l : harvestLots) {
            total += l.getRemainingQuantity();
        }
        return total;
    }

    public int getRemainingMarketDemand() {
        int total = 0;
        for (Market m : markets) {
            total += m.getRemainingDemand();
        }
        return total;
    }

    public int getAvailableVehiclesCount() {
        int count = 0;
        for (Vehicle v : vehicles) {
            if (v.isAvailable()) count++;
        }
        return count;
    }

    public int getAvailableStorageCapacity() {
        int total = 0;
        for (StorageCenter s : storageCenters) {
            total += s.availableCapacity();
        }
        return total;
    }

    public int getTotalShipmentsCount() {
        return shipments.size();
    }

    // ==========================================
    // CSV EXPORT UTILITY FOR REPORTS
    // ==========================================
    public static boolean exportTableToCsv(javax.swing.JTable table, File targetFile) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            javax.swing.table.TableModel model = table.getModel();
            // Headers
            for (int col = 0; col < model.getColumnCount(); col++) {
                pw.print("\"" + model.getColumnName(col).replace("\"", "\"\"") + "\"");
                if (col < model.getColumnCount() - 1) pw.print(",");
            }
            pw.println();

            // Rows
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    String strVal = val != null ? val.toString() : "";
                    pw.print("\"" + strVal.replace("\"", "\"\"") + "\"");
                    if (col < model.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
