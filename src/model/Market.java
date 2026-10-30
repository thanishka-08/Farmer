package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model representing a wholesale/retail agricultural market.
 * Tracks crop demands and provides lookup and reduction operations.
 */
public class Market {
    private String marketId;
    private String name;
    private String location;
    private String crop;              // Primary crop specified for this market entry
    private int initialDemand;        // Initial required quantity (kg)
    private int remainingDemand;      // Remaining unfilled demand (kg)

    // Demand map to preserve full multi-crop compatibility with console code
    private HashMap<String, Integer> demand = new HashMap<>();

    public Market(String marketId, String name, String location, String crop, int initialDemand) {
        this.marketId = marketId != null ? marketId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.location = location != null ? location.trim() : "";
        this.crop = crop != null ? crop.trim() : "";
        this.initialDemand = Math.max(0, initialDemand);
        this.remainingDemand = this.initialDemand;
        if (!this.crop.isEmpty()) {
            setDemand(this.crop, this.initialDemand);
        }
    }

    public Market(String marketId, String name, String location, String crop, int initialDemand, int remainingDemand) {
        this.marketId = marketId != null ? marketId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.location = location != null ? location.trim() : "";
        this.crop = crop != null ? crop.trim() : "";
        this.initialDemand = Math.max(0, initialDemand);
        this.remainingDemand = Math.max(0, remainingDemand);
        if (!this.crop.isEmpty()) {
            demand.put(this.crop.toLowerCase(), this.remainingDemand);
        }
    }

    // Constructor matching console Agri_70
    public Market(String marketId, String name, String location) {
        this(marketId, name, location, "", 0);
    }

    public void setDemand(String cropName, int quantity) {
        if (cropName != null && !cropName.trim().isEmpty()) {
            demand.put(cropName.trim().toLowerCase(), Math.max(0, quantity));
            if (this.crop == null || this.crop.isEmpty()) {
                this.crop = cropName.trim();
                this.initialDemand = quantity;
                this.remainingDemand = quantity;
            }
        }
    }

    public int getDemand(String cropName) {
        if (cropName == null) return 0;
        // First check internal map
        Integer val = demand.get(cropName.trim().toLowerCase());
        if (val != null) return val;
        // If crop matches the primary crop, return remainingDemand
        if (this.crop.equalsIgnoreCase(cropName.trim())) {
            return remainingDemand;
        }
        return 0;
    }

    public void reduceDemand(String cropName, int quantity) {
        if (quantity <= 0) return;
        int current = getDemand(cropName);
        int updated = Math.max(0, current - quantity);
        demand.put(cropName.trim().toLowerCase(), updated);

        if (this.crop.equalsIgnoreCase(cropName.trim())) {
            this.remainingDemand = updated;
        }
    }

    public String getMarketId() { return marketId; }
    public void setMarketId(String marketId) { this.marketId = marketId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }

    public int getInitialDemand() { return initialDemand; }
    public void setInitialDemand(int initialDemand) {
        this.initialDemand = initialDemand;
        if (this.remainingDemand > initialDemand) {
            this.remainingDemand = initialDemand;
        }
        if (this.crop != null && !this.crop.isEmpty()) {
            demand.put(this.crop.toLowerCase(), this.remainingDemand);
        }
    }

    public int getRemainingDemand() { return remainingDemand; }
    public void setRemainingDemand(int remainingDemand) {
        this.remainingDemand = remainingDemand;
        if (this.crop != null && !this.crop.isEmpty()) {
            demand.put(this.crop.toLowerCase(), remainingDemand);
        }
    }

    @Override
    public String toString() {
        return marketId + " - " + name + " (" + location + ")";
    }

    public String toCsv() {
        return escape(marketId) + "," + escape(name) + "," + escape(location) + ","
                + escape(crop) + "," + initialDemand + "," + remainingDemand;
    }

    public static Market fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 6) {
            String mId = unescape(parts[0]);
            String name = unescape(parts[1]);
            String loc = unescape(parts[2]);
            String crop = unescape(parts[3]);
            int initDem = parseInt(parts[4], 0);
            int remDem = parseInt(parts[5], initDem);
            return new Market(mId, name, loc, crop, initDem, remDem);
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
