package model;

/**
 * Model representing a registered farmer in the agricultural supply chain.
 * Demonstrates Encapsulation and OOP design.
 */
public class Farmer {
    private String farmerId;
    private String name;
    private String phone;
    private String village;

    public Farmer(String farmerId, String name, String phone, String village) {
        this.farmerId = farmerId != null ? farmerId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.phone = phone != null ? phone.trim() : "";
        this.village = village != null ? village.trim() : "";
    }

    public Farmer(String farmerId, String name) {
        this(farmerId, name, "", "");
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    @Override
    public String toString() {
        return farmerId + " - " + name + (village.isEmpty() ? "" : " (" + village + ")");
    }

    public String toCsv() {
        return escapeCsv(farmerId) + "," + escapeCsv(name) + "," + escapeCsv(phone) + "," + escapeCsv(village);
    }

    public static Farmer fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 2) {
            String id = unescapeCsv(parts[0]);
            String name = unescapeCsv(parts[1]);
            String phone = parts.length > 2 ? unescapeCsv(parts[2]) : "";
            String village = parts.length > 3 ? unescapeCsv(parts[3]) : "";
            return new Farmer(id, name, phone, village);
        }
        return null;
    }

    private static String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private static String unescapeCsv(String val) {
        if (val == null) return "";
        val = val.trim();
        if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
            val = val.substring(1, val.length() - 1).replace("\"\"", "\"");
        }
        return val;
    }
}
