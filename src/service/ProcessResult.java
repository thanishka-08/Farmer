package service;

import model.Shipment;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the results of a supply chain processing execution.
 * Provides detailed counts, created shipments, and explanation notes.
 */
public class ProcessResult {
    private int lotsExamined;
    private int shipmentsCreated;
    private int totalQuantityAllocated;
    private int totalQuantityUnallocated;
    private List<Shipment> newShipments = new ArrayList<>();
    private List<String> logs = new ArrayList<>();
    private List<String> unallocatedReasons = new ArrayList<>();

    public void addShipment(Shipment shipment) {
        newShipments.add(shipment);
        shipmentsCreated++;
        totalQuantityAllocated += shipment.getQuantity();
    }

    public void addLog(String message) {
        logs.add(message);
    }

    public void addUnallocatedReason(String reason) {
        unallocatedReasons.add(reason);
    }

    public int getLotsExamined() { return lotsExamined; }
    public void setLotsExamined(int lotsExamined) { this.lotsExamined = lotsExamined; }

    public int getShipmentsCreated() { return shipmentsCreated; }
    public int getTotalQuantityAllocated() { return totalQuantityAllocated; }
    public int getTotalQuantityUnallocated() { return totalQuantityUnallocated; }
    public void setTotalQuantityUnallocated(int qty) { this.totalQuantityUnallocated = qty; }

    public List<Shipment> getNewShipments() { return newShipments; }
    public List<String> getLogs() { return logs; }
    public List<String> getUnallocatedReasons() { return unallocatedReasons; }

    public String getSummaryText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SUPPLY CHAIN PROCESSING SUMMARY ===\n\n");
        sb.append(String.format("Harvest Lots Examined      : %d\n", lotsExamined));
        sb.append(String.format("New Shipments Created      : %d\n", shipmentsCreated));
        sb.append(String.format("Total Quantity Allocated   : %d kg\n", totalQuantityAllocated));
        sb.append(String.format("Remaining Unallocated Qty  : %d kg\n\n", totalQuantityUnallocated));

        if (!newShipments.isEmpty()) {
            sb.append("--- Newly Created Shipments ---\n");
            for (Shipment s : newShipments) {
                sb.append(String.format("• Lot %s (%s, %d kg) -> %s (%s) | Vehicle %s | Route: %s | Time: %.1f hrs\n",
                    s.getLotId(), s.getCrop(), s.getQuantity(),
                    s.getDestinationName(), s.getDestinationType(),
                    s.getVehicleId(), s.getRouteDisplay(), s.getTravelTime()));
            }
            sb.append("\n");
        }

        if (!unallocatedReasons.isEmpty()) {
            sb.append("--- Unallocated Lot Details ---\n");
            for (String reason : unallocatedReasons) {
                sb.append("• ").append(reason).append("\n");
            }
        }

        return sb.toString();
    }
}
