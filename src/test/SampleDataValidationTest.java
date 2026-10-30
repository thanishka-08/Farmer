package test;

import service.ProcessResult;
import service.SupplyChainService;
import storage.DataManager;
import storage.SampleDataLoader;

public class SampleDataValidationTest {
    public static void main(String[] args) {
        System.out.println("Testing SampleDataLoader and Full Processing Pipeline...");
        DataManager dm = new DataManager();
        SampleDataLoader.populateSampleData(dm);

        System.out.println("Farmers loaded: " + dm.getTotalFarmers());
        System.out.println("Harvest Lots loaded: " + dm.getTotalHarvestLots());
        System.out.println("Markets loaded: " + dm.getMarkets().size());
        System.out.println("Storage Centers loaded: " + dm.getStorageCenters().size());
        System.out.println("Vehicles loaded: " + dm.getVehicles().size());
        System.out.println("Pending Harvest Qty: " + dm.getTotalPendingHarvestQty() + " kg");

        SupplyChainService service = new SupplyChainService(
            dm.getHarvestLots(), dm.getMarkets(), dm.getStorageCenters(),
            dm.getVehicles(), dm.getRoadNetwork(), dm.getShipments()
        );

        ProcessResult result = service.processLots();
        dm.notifyDataChanged();

        System.out.println("\nExecution Results:");
        System.out.println("Shipments created: " + result.getShipmentsCreated());
        System.out.println("Allocated Qty: " + result.getTotalQuantityAllocated() + " kg");
        System.out.println("Unallocated Qty: " + result.getTotalQuantityUnallocated() + " kg");

        // Verify clean persistence
        DataManager dm2 = new DataManager();
        System.out.println("Persistence Check - Restored Shipments count: " + dm2.getShipments().size());

        if (dm2.getShipments().size() == result.getShipmentsCreated()) {
            System.out.println("[SUCCESS] Sample dataset test passed completely!");
        } else {
            System.err.println("[FAIL] Shipment persistence count mismatch!");
        }
    }
}
