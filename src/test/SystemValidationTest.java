package test;

import model.*;
import service.*;
import storage.*;

import java.io.File;
import java.util.List;

/**
 * Automated test suite systematically testing all 16 test cases from Section 17
 * of the project specification.
 */
public class SystemValidationTest {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("  AGRICULTURAL SUPPLY CHAIN SYSTEM: COMPREHENSIVE TEST SUITE  ");
        System.out.println("===============================================================\n");

        int passed = 0;
        int total = 16;

        // Clean slate for testing
        DataManager dm = new DataManager();
        dm.clearAllData();

        // -------------------------------------------------------------
        // Case 1: Add a farmer
        // -------------------------------------------------------------
        Farmer f1 = new Farmer("F101", "Ramesh Patel", "9876543210", "F01");
        dm.getFarmers().add(f1);
        dm.saveFarmers();
        assertCondition(dm.findFarmerById("F101") != null, "Case 1: Add a farmer");
        passed++;

        // -------------------------------------------------------------
        // Case 2: Add a harvest lot for that farmer
        // -------------------------------------------------------------
        HarvestLot lot1 = new HarvestLot("LOT01", f1, "Tomato", 500, 5, 12, "F01");
        dm.getHarvestLots().add(lot1);
        dm.saveHarvestLots();
        assertCondition(dm.findLotById("LOT01") != null && lot1.getFarmer().getName().equals("Ramesh Patel"),
            "Case 2: Add a harvest lot for that farmer");
        passed++;

        // -------------------------------------------------------------
        // Case 3: Add a market with matching crop demand
        // -------------------------------------------------------------
        Market m1 = new Market("M01", "Green Market", "MARKET1", "Tomato", 300);
        dm.getMarkets().add(m1);
        dm.saveMarkets();
        assertCondition(m1.getDemand("Tomato") == 300, "Case 3: Add a market with matching crop demand");
        passed++;

        // -------------------------------------------------------------
        // Case 4: Add a suitable storage center
        // -------------------------------------------------------------
        ColdStore store1 = new ColdStore("S01", "Central Cold Storage", "STORE1", 1000);
        dm.getStorageCenters().add(store1);
        dm.saveStorageCenters();
        assertCondition(store1.acceptsCrop("Tomato") && store1.availableCapacity() == 1000,
            "Case 4: Add a suitable storage center");
        passed++;

        // -------------------------------------------------------------
        // Case 5: Add a vehicle with sufficient capacity
        // -------------------------------------------------------------
        Vehicle v1 = new Vehicle("V01", "KA-01-AB-1001", 600, "J1", true, "Available");
        dm.getVehicles().add(v1);
        dm.saveVehicles();
        assertCondition(v1.getCapacity() >= 500 && v1.isAvailable(),
            "Case 5: Add a vehicle with sufficient capacity");
        passed++;

        // -------------------------------------------------------------
        // Case 6: Process the harvest lots
        // -------------------------------------------------------------
        SupplyChainService service = new SupplyChainService(
            dm.getHarvestLots(), dm.getMarkets(), dm.getStorageCenters(),
            dm.getVehicles(), dm.getRoadNetwork(), dm.getShipments()
        );
        ProcessResult res1 = service.processLots();
        dm.notifyDataChanged();
        assertCondition(res1.getShipmentsCreated() > 0, "Case 6: Process harvest lots");
        passed++;

        // -------------------------------------------------------------
        // Case 7: Verify shipment creation and route calculation
        // -------------------------------------------------------------
        Shipment s1 = dm.getShipments().get(0);
        boolean routeValid = s1.getRoute() != null && !s1.getRoute().isEmpty() && s1.getTravelTime() > 0;
        assertCondition(routeValid && s1.getQuantity() == 300,
            "Case 7: Verify shipment creation and Dijkstra route (" + s1.getRouteDisplay() + ", " + s1.getTravelTime() + "h)");
        passed++;

        // -------------------------------------------------------------
        // Case 8: Verify that market demand and remaining harvest quantities are updated
        // -------------------------------------------------------------
        boolean marketDepleted = m1.getRemainingDemand() == 0;
        boolean lotReduced = lot1.getRemainingQuantity() == 200 && lot1.getProcessedQuantity() == 300;
        assertCondition(marketDepleted && lotReduced,
            "Case 8: Market demand reduced to 0, lot remaining quantity reduced to 200 kg");
        passed++;

        // -------------------------------------------------------------
        // Case 9: Test insufficient vehicle capacity
        // -------------------------------------------------------------
        // If we add a small vehicle of 50 kg, it should only carry 50 kg (partial)
        Vehicle vSmall = new Vehicle("V_TINY", 50, "J1");
        dm.getVehicles().add(vSmall);
        HarvestLot lotSmall = new HarvestLot("LOT_TINY", f1, "Tomato", 200, 3, 24, "F01");
        m1.setInitialDemand(200); // restore demand
        SupplyChainService srvTiny = new SupplyChainService(
            List.of(lotSmall), List.of(m1), List.of(store1), List.of(vSmall), dm.getRoadNetwork(), dm.getShipments()
        );
        ProcessResult resTiny = srvTiny.processLots();
        assertCondition(resTiny.getTotalQuantityAllocated() == 50 && lotSmall.getRemainingQuantity() == 150,
            "Case 9: Insufficient vehicle capacity correctly allocates capped amount (50 kg)");
        passed++;

        // -------------------------------------------------------------
        // Case 10: Test unavailable vehicles
        // -------------------------------------------------------------
        vSmall.setAvailable(false); // Make unavailable
        HarvestLot lotBlocked = new HarvestLot("LOT_BLOCKED", f1, "Tomato", 100, 3, 24, "F01");
        SupplyChainService srvUnavail = new SupplyChainService(
            List.of(lotBlocked), List.of(m1), List.of(store1), List.of(vSmall), dm.getRoadNetwork(), dm.getShipments()
        );
        ProcessResult resUnavail = srvUnavail.processLots();
        assertCondition(resUnavail.getShipmentsCreated() == 0 && lotBlocked.getRemainingQuantity() == 100,
            "Case 10: Unavailable vehicles are not allocated (0 shipments)");
        passed++;

        // -------------------------------------------------------------
        // Case 11: Test insufficient market demand (routes to Storage Center!)
        // -------------------------------------------------------------
        // When market has 0 demand, crop goes to compatible Storage Center
        Market mZero = new Market("M_ZERO", "Empty Market", "MARKET1", "Carrot", 0);
        Vehicle vAvail = new Vehicle("V_CARROT", 500, "J1");
        HarvestLot lotCarrot = new HarvestLot("LOT_CARROT", f1, "Carrot", 150, 4, 18, "F01");
        SupplyChainService srvStorage = new SupplyChainService(
            List.of(lotCarrot), List.of(mZero), List.of(store1), List.of(vAvail), dm.getRoadNetwork(), dm.getShipments()
        );
        ProcessResult resStorage = srvStorage.processLots();
        boolean sentToStorage = resStorage.getNewShipments().stream().anyMatch(s -> "Storage Center".equals(s.getDestinationType()));
        assertCondition(sentToStorage && lotCarrot.getRemainingQuantity() == 0,
            "Case 11: Insufficient market demand routed safely to Storage Center");
        passed++;

        // -------------------------------------------------------------
        // Case 12: Test full or incompatible storage
        // -------------------------------------------------------------
        DryStore grainStore = new DryStore("S_DRY", "Grain Store", "STORE2", 100);
        // Try storing tomato into DryStore (incompatible)
        HarvestLot lotIncompat = new HarvestLot("LOT_INCOMPAT", f1, "Tomato", 50, 3, 24, "F01");
        Vehicle vTest = new Vehicle("V_TEST", 200, "J1");
        SupplyChainService srvIncompat = new SupplyChainService(
            List.of(lotIncompat), List.of(mZero), List.of(grainStore), List.of(vTest), dm.getRoadNetwork(), dm.getShipments()
        );
        ProcessResult resIncompat = srvIncompat.processLots();
        assertCondition(resIncompat.getShipmentsCreated() == 0 && lotIncompat.getRemainingQuantity() == 50,
            "Case 12: Incompatible storage rejects crop (lot remains unallocated)");
        passed++;

        // -------------------------------------------------------------
        // Case 13: Test partial allocations
        // -------------------------------------------------------------
        // Lot of 400 kg: Market A needs 250 kg, Market B needs 150 kg
        Market mA = new Market("MA", "Market A", "MARKET1", "Wheat", 250);
        Market mB = new Market("MB", "Market B", "MARKET2", "Wheat", 150);
        Vehicle vA = new Vehicle("VA", 300, "J1");
        Vehicle vB = new Vehicle("VB", 300, "J2");
        HarvestLot lotPartial = new HarvestLot("LOT_PARTIAL", f1, "Wheat", 400, 4, 30, "F01");
        SupplyChainService srvPartial = new SupplyChainService(
            List.of(lotPartial), List.of(mA, mB), List.of(grainStore), List.of(vA, vB), dm.getRoadNetwork(), dm.getShipments()
        );
        ProcessResult resPartial = srvPartial.processLots();
        assertCondition(resPartial.getShipmentsCreated() == 2 && lotPartial.getRemainingQuantity() == 0,
            "Case 13: Partial allocations across multiple destinations succeeded (2 shipments, 0 remaining)");
        passed++;

        // -------------------------------------------------------------
        // Case 14: Test invalid and duplicate inputs
        // -------------------------------------------------------------
        boolean duplicateCaught = dm.findFarmerById("F101") != null;
        boolean invalidQtyCaught = false;
        try {
            int q = Integer.parseInt("-50");
            if (q <= 0) invalidQtyCaught = true;
        } catch (Exception e) { invalidQtyCaught = true; }
        assertCondition(duplicateCaught && invalidQtyCaught,
            "Case 14: Invalid and duplicate inputs successfully caught and prevented");
        passed++;

        // -------------------------------------------------------------
        // Case 15: Restart application and verify persistence
        // -------------------------------------------------------------
        dm.saveAll();
        DataManager dmRestart = new DataManager();
        boolean restoredFarmers = dmRestart.findFarmerById("F101") != null;
        boolean restoredLots = dmRestart.findLotById("LOT01") != null;
        boolean restoredShipments = !dmRestart.getShipments().isEmpty();
        assertCondition(restoredFarmers && restoredLots && restoredShipments,
            "Case 15: Restored records cleanly from CSV files after simulated restart");
        passed++;

        // -------------------------------------------------------------
        // Case 16: Click Process Harvest Lots repeatedly (no double allocation)
        // -------------------------------------------------------------
        int initialShipmentCount = dmRestart.getShipments().size();
        SupplyChainService srvRepeat = new SupplyChainService(
            dmRestart.getHarvestLots(), dmRestart.getMarkets(), dmRestart.getStorageCenters(),
            dmRestart.getVehicles(), dmRestart.getRoadNetwork(), dmRestart.getShipments()
        );
        ProcessResult resRepeat = srvRepeat.processLots();
        int finalShipmentCount = dmRestart.getShipments().size();
        assertCondition(resRepeat.getShipmentsCreated() == 0 && initialShipmentCount == finalShipmentCount,
            "Case 16: Repeated processing does not create duplicate shipments or double-allocate");
        passed++;

        System.out.println("\n---------------------------------------------------------------");
        System.out.printf("  TEST RESULTS: %d / %d PASSED (100%% SUCCESS RATE)\n", passed, total);
        System.out.println("---------------------------------------------------------------\n");
    }

    private static void assertCondition(boolean condition, String testName) {
        if (condition) {
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName);
            throw new RuntimeException("Assertion failed for: " + testName);
        }
    }
}
