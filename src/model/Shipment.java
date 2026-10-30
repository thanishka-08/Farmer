package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Model representing an allocated shipment dispatched through the supply chain.
 * Links harvest lot, destination, vehicle, travel time, and Dijkstra route.
 */
public class Shipment {
    private String shipmentId;
    private String lotId;
    private String farmerName;
    private String crop;
    private String destinationType; // "Market" or "Storage Center"
    private String destinationName;
    private String destinationLocation;
    private String vehicleId;
    private int quantity;
    private double travelTime;
    private List<String> route;
    private String status;           // "Dispatched", "In Transit", "Delivered"

    public Shipment(String shipmentId, String lotId, String farmerName, String crop,
                    String destinationType, String destinationName, String destinationLocation,
                    String vehicleId, int quantity, double travelTime, List<String> route, String status) {
        this.shipmentId = shipmentId != null ? shipmentId.trim() : "";
        this.lotId = lotId != null ? lotId.trim() : "";
        this.farmerName = farmerName != null ? farmerName.trim() : "";
        this.crop = crop != null ? crop.trim() : "";
        this.destinationType = destinationType != null ? destinationType.trim() : "Market";
        this.destinationName = destinationName != null ? destinationName.trim() : "";
        this.destinationLocation = destinationLocation != null ? destinationLocation.trim() : "";
        this.vehicleId = vehicleId != null ? vehicleId.trim() : "";
        this.quantity = quantity;
        this.travelTime = travelTime;
        this.route = route != null ? route : new ArrayList<>();
        this.status = status != null ? status : "Delivered";
    }

    // Convenience constructor matching console Agri_70
    public Shipment(String lotId, String destination, String vehicleId,
                    int quantity, double travelTime, List<String> route) {
        this("SHP" + System.currentTimeMillis() % 100000, lotId, "", "",
             "Market", destination, destination, vehicleId, quantity, travelTime, route, "Delivered");
    }

    public String getRouteDisplay() {
        if (route == null || route.isEmpty()) {
            return "Direct";
        }
        return String.join(" -> ", route);
    }

    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

    public String getLotId() { return lotId; }
    public void setLotId(String lotId) { this.lotId = lotId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }

    public String getDestinationType() { return destinationType; }
    public void setDestinationType(String destinationType) { this.destinationType = destinationType; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTravelTime() { return travelTime; }
    public void setTravelTime(double travelTime) { this.travelTime = travelTime; }

    public List<String> getRoute() { return route; }
    public void setRoute(List<String> route) { this.route = route; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String toCsv() {
        String routeStr = route != null ? String.join(";", route) : "";
        return escape(shipmentId) + "," + escape(lotId) + "," + escape(farmerName) + ","
                + escape(crop) + "," + escape(destinationType) + "," + escape(destinationName) + ","
                + escape(destinationLocation) + "," + escape(vehicleId) + "," + quantity + ","
                + travelTime + "," + escape(routeStr) + "," + escape(status);
    }

    public static Shipment fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 10) {
            String sId = unescape(parts[0]);
            String lId = unescape(parts[1]);
            String fName = unescape(parts[2]);
            String crop = unescape(parts[3]);
            String dType = unescape(parts[4]);
            String dName = unescape(parts[5]);
            String dLoc = parts.length > 6 ? unescape(parts[6]) : dName;
            String vId = parts.length > 7 ? unescape(parts[7]) : "";
            int qty = parts.length > 8 ? parseInt(parts[8], 0) : 0;
            double time = parts.length > 9 ? parseDouble(parts[9], 0.0) : 0.0;
            String routeStr = parts.length > 10 ? unescape(parts[10]) : "";
            String st = parts.length > 11 ? unescape(parts[11]) : "Delivered";

            List<String> rList = new ArrayList<>();
            if (!routeStr.isEmpty()) {
                rList.addAll(Arrays.asList(routeStr.split(";")));
            }
            return new Shipment(sId, lId, fName, crop, dType, dName, dLoc, vId, qty, time, rList, st);
        }
        return null;
    }

    private static int parseInt(String val, int def) {
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return def; }
    }

    private static double parseDouble(String val, double def) {
        try { return Double.parseDouble(val.trim()); } catch (Exception e) { return def; }
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
