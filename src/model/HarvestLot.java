package model;

/**
 * Model representing a harvested crop lot from a farmer.
 * Contains harvest quantity, urgency level, deadline, and tracking status.
 */
public class HarvestLot implements Comparable<HarvestLot> {
    private String lotId;
    private Farmer farmer;
    private String crop;
    private int originalQuantity;
    private int processedQuantity;
    private int remainingQuantity;
    private int urgency;         // 1 (lowest) to 5 (highest)
    private int deadline;        // in hours
    private String location;     // Farm or village location node (e.g. F01)

    public HarvestLot(String lotId, Farmer farmer, String crop, int quantity,
                      int urgency, int deadline, String location) {
        this.lotId = lotId != null ? lotId.trim() : "";
        this.farmer = farmer;
        this.crop = crop != null ? crop.trim() : "";
        this.originalQuantity = Math.max(0, quantity);
        this.processedQuantity = 0;
        this.remainingQuantity = this.originalQuantity;
        this.urgency = Math.max(1, Math.min(5, urgency));
        this.deadline = Math.max(1, deadline);
        this.location = location != null ? location.trim() : "F01";
    }

    public HarvestLot(String lotId, Farmer farmer, String crop, int quantity,
                      int processedQuantity, int remainingQuantity,
                      int urgency, int deadline, String location) {
        this.lotId = lotId != null ? lotId.trim() : "";
        this.farmer = farmer;
        this.crop = crop != null ? crop.trim() : "";
        this.originalQuantity = Math.max(0, quantity);
        this.processedQuantity = Math.max(0, processedQuantity);
        this.remainingQuantity = Math.max(0, remainingQuantity);
        this.urgency = Math.max(1, Math.min(5, urgency));
        this.deadline = Math.max(1, deadline);
        this.location = location != null ? location.trim() : "F01";
    }

    // Convenience constructor matching console Agri_70 (default urgency = 3)
    public HarvestLot(String lotId, Farmer farmer, String crop, int quantity, int deadline, String location) {
        this(lotId, farmer, crop, quantity, 3, deadline, location);
    }

    public void allocateQuantity(int qty) {
        if (qty <= 0) return;
        int alloc = Math.min(qty, this.remainingQuantity);
        this.processedQuantity += alloc;
        this.remainingQuantity -= alloc;
    }

    public String getStatus() {
        if (remainingQuantity == 0 && originalQuantity > 0) {
            return "Completed";
        } else if (processedQuantity > 0 && remainingQuantity > 0) {
            return "Partially Shipped";
        } else {
            return "Pending";
        }
    }

    // Natural ordering for PriorityQueue:
    // 1. Higher urgency first (e.g. 5 before 1)
    // 2. Tighter deadline first (e.g. 10 hours before 48 hours)
    @Override
    public int compareTo(HarvestLot other) {
        if (this.urgency != other.urgency) {
            return Integer.compare(other.urgency, this.urgency); // Descending urgency
        }
        return Integer.compare(this.deadline, other.deadline);   // Ascending deadline
    }

    public String getLotId() { return lotId; }
    public void setLotId(String lotId) { this.lotId = lotId; }

    public Farmer getFarmer() { return farmer; }
    public void setFarmer(Farmer farmer) { this.farmer = farmer; }

    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }

    public int getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(int originalQuantity) {
        this.originalQuantity = originalQuantity;
        if (this.remainingQuantity > originalQuantity) {
            this.remainingQuantity = originalQuantity;
        }
    }

    public int getProcessedQuantity() { return processedQuantity; }
    public void setProcessedQuantity(int processedQuantity) { this.processedQuantity = processedQuantity; }

    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public int getUrgency() { return urgency; }
    public void setUrgency(int urgency) { this.urgency = Math.max(1, Math.min(5, urgency)); }

    public int getDeadline() { return deadline; }
    public void setDeadline(int deadline) { this.deadline = deadline; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String toCsv() {
        String farmerId = farmer != null ? farmer.getFarmerId() : "";
        return escape(lotId) + "," + escape(farmerId) + "," + escape(crop) + ","
                + originalQuantity + "," + processedQuantity + "," + remainingQuantity + ","
                + urgency + "," + deadline + "," + escape(location);
    }

    public static HarvestLot fromCsv(String line, java.util.Map<String, Farmer> farmerMap) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 9) {
            String lotId = unescape(parts[0]);
            String fId = unescape(parts[1]);
            Farmer farmer = farmerMap.get(fId);
            if (farmer == null) {
                farmer = new Farmer(fId, "Unknown (" + fId + ")");
            }
            String crop = unescape(parts[2]);
            int orig = parseInt(parts[3], 0);
            int proc = parseInt(parts[4], 0);
            int rem = parseInt(parts[5], orig);
            int urg = parseInt(parts[6], 3);
            int dline = parseInt(parts[7], 24);
            String loc = unescape(parts[8]);
            return new HarvestLot(lotId, farmer, crop, orig, proc, rem, urg, dline, loc);
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
