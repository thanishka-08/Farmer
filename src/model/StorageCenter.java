package model;

/**
 * Abstract class representing a crop storage facility.
 * Demonstrates Abstraction and Inheritance.
 */
public abstract class StorageCenter {
    protected String id;
    protected String name;
    protected String location;
    protected int capacity;
    protected int usedCapacity = 0;
    protected String customSupportedCrops = "";

    public StorageCenter(String id, String name, String location, int capacity) {
        this.id = id != null ? id.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.location = location != null ? location.trim() : "";
        this.capacity = Math.max(0, capacity);
        this.usedCapacity = 0;
    }

    public StorageCenter(String id, String name, String location, int capacity, int usedCapacity, String customSupportedCrops) {
        this.id = id != null ? id.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.location = location != null ? location.trim() : "";
        this.capacity = Math.max(0, capacity);
        this.usedCapacity = Math.max(0, usedCapacity);
        this.customSupportedCrops = customSupportedCrops != null ? customSupportedCrops.trim() : "";
    }

    public abstract boolean acceptsCrop(String crop);
    public abstract String getStorageType();
    public abstract String getDefaultSupportedCrops();

    public String getSupportedCropsDisplay() {
        if (customSupportedCrops != null && !customSupportedCrops.trim().isEmpty()) {
            return customSupportedCrops.trim();
        }
        return getDefaultSupportedCrops();
    }

    public int availableCapacity() {
        return Math.max(0, capacity - usedCapacity);
    }

    public void store(int quantity) {
        if (quantity <= 0) return;
        usedCapacity = Math.min(capacity, usedCapacity + quantity);
    }

    public void release(int quantity) {
        if (quantity <= 0) return;
        usedCapacity = Math.max(0, usedCapacity - quantity);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        this.capacity = Math.max(0, capacity);
    }

    public int getUsedCapacity() { return usedCapacity; }
    public void setUsedCapacity(int usedCapacity) {
        this.usedCapacity = Math.max(0, usedCapacity);
    }

    public String getCustomSupportedCrops() { return customSupportedCrops; }
    public void setCustomSupportedCrops(String customSupportedCrops) {
        this.customSupportedCrops = customSupportedCrops != null ? customSupportedCrops.trim() : "";
    }

    @Override
    public String toString() {
        return id + " - " + name + " [" + getStorageType() + "] (" + location + ")";
    }

    public String toCsv() {
        return escape(id) + "," + escape(name) + "," + escape(location) + ","
                + escape(getStorageType()) + "," + capacity + "," + usedCapacity + ","
                + escape(customSupportedCrops);
    }

    public static StorageCenter fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 6) {
            String id = unescape(parts[0]);
            String name = unescape(parts[1]);
            String loc = unescape(parts[2]);
            String type = unescape(parts[3]);
            int cap = parseInt(parts[4], 0);
            int used = parseInt(parts[5], 0);
            String customCrops = parts.length > 6 ? unescape(parts[6]) : "";

            if (type.equalsIgnoreCase("Dry Storage") || type.equalsIgnoreCase("DryStore")) {
                return new DryStore(id, name, loc, cap, used, customCrops);
            } else {
                return new ColdStore(id, name, loc, cap, used, customCrops);
            }
        }
        return null;
    }

    private static int parseInt(String val, int def) {
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return def; }
    }

    protected static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    protected static String unescape(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }
}
