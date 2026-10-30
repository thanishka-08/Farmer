package model;

/**
 * Model representing a transport vehicle in the agricultural supply chain.
 * Tracks capacity, current node location, and operational availability.
 */
public class Vehicle {
    private String id;
    private String vehicleNumber;
    private int capacity;
    private String location;
    private boolean available = true;
    private String status = "Available"; // "Available", "In Transit", "Maintenance"

    public Vehicle(String id, String vehicleNumber, int capacity, String location, boolean available, String status) {
        this.id = id != null ? id.trim() : "";
        this.vehicleNumber = vehicleNumber != null ? vehicleNumber.trim() : id;
        this.capacity = Math.max(0, capacity);
        this.location = location != null ? location.trim() : "J1";
        this.available = available;
        this.status = status != null ? status : (available ? "Available" : "In Transit");
    }

    // Convenience constructor matching console Agri_70
    public Vehicle(String id, int capacity, String location) {
        this(id, id, capacity, location, true, "Available");
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = Math.max(0, capacity); }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) {
        this.available = available;
        if (!available && "Available".equalsIgnoreCase(this.status)) {
            this.status = "In Transit";
        } else if (available) {
            this.status = "Available";
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.available = "Available".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return id + " (" + capacity + " kg) - " + location + " [" + status + "]";
    }

    public String toCsv() {
        return escape(id) + "," + escape(vehicleNumber) + "," + capacity + ","
                + escape(location) + "," + available + "," + escape(status);
    }

    public static Vehicle fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 4) {
            String id = unescape(parts[0]);
            String num = unescape(parts[1]);
            int cap = parseInt(parts[2], 0);
            String loc = unescape(parts[3]);
            boolean avail = parts.length > 4 ? Boolean.parseBoolean(parts[4].trim()) : true;
            String st = parts.length > 5 ? unescape(parts[5]) : (avail ? "Available" : "In Transit");
            return new Vehicle(id, num, cap, loc, avail, st);
        }
        return null;
    }

    private static int parseInt(String val, int def) {
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return def; }
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String unescape(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }
}
