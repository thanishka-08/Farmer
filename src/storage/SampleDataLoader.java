package storage;

import model.*;
import java.util.List;

/**
 * Utility to load realistic sample demonstration data into the system.
 * Based on the console Agri_70 implementation.
 * Only activated on demand when the user clicks 'LOAD SAMPLE DATA'.
 */
public class SampleDataLoader {

    public static void populateSampleData(DataManager dm) {
        // Clear existing data first
        dm.getFarmers().clear();
        dm.getHarvestLots().clear();
        dm.getMarkets().clear();
        dm.getStorageCenters().clear();
        dm.getVehicles().clear();
        dm.getShipments().clear();
        dm.getRoadNetwork().initDefaultNetwork();

        // 1. Five Sample Farmers
        Farmer f1 = new Farmer("F101", "Ramesh Patel", "9876543211", "Village A (F01)");
        Farmer f2 = new Farmer("F102", "Suresh Kumar", "9876543212", "Village B (F02)");
        Farmer f3 = new Farmer("F103", "Ananya Sharma", "9876543213", "Village C (F03)");
        Farmer f4 = new Farmer("F104", "Rajesh Reddy", "9876543214", "Village D (F04)");
        Farmer f5 = new Farmer("F105", "Meena Devi", "9876543215", "Village E (F05)");

        dm.getFarmers().add(f1);
        dm.getFarmers().add(f2);
        dm.getFarmers().add(f3);
        dm.getFarmers().add(f4);
        dm.getFarmers().add(f5);

        // 2. Markets and Demands matching Agri_70
        dm.getMarkets().add(new Market("M01-TOM", "Green Market", "MARKET1", "tomato", 600));
        dm.getMarkets().add(new Market("M01-RIC", "Green Market", "MARKET1", "rice", 800));
        dm.getMarkets().add(new Market("M01-ONI", "Green Market", "MARKET1", "onion", 400));

        dm.getMarkets().add(new Market("M02-TOM", "City Market", "MARKET2", "tomato", 300));
        dm.getMarkets().add(new Market("M02-CAR", "City Market", "MARKET2", "carrot", 400));
        dm.getMarkets().add(new Market("M02-RIC", "City Market", "MARKET2", "rice", 500));

        dm.getMarkets().add(new Market("M03-ONI", "Farmers Market", "MARKET3", "onion", 500));
        dm.getMarkets().add(new Market("M03-WHE", "Farmers Market", "MARKET3", "wheat", 700));
        dm.getMarkets().add(new Market("M03-CAR", "Farmers Market", "MARKET3", "carrot", 300));

        // 3. Storage Centers matching Agri_70
        dm.getStorageCenters().add(new ColdStore("S01", "Cold Storage", "STORE1", 1000));
        dm.getStorageCenters().add(new DryStore("S02", "Dry Storage", "STORE2", 1500));

        // 4. Vehicles matching Agri_70
        dm.getVehicles().add(new Vehicle("V01", "KA-01-AB-1001", 500, "J1", true, "Available"));
        dm.getVehicles().add(new Vehicle("V02", "KA-01-AB-1002", 800, "J1", true, "Available"));
        dm.getVehicles().add(new Vehicle("V03", "KA-01-AB-1003", 1200, "J2", true, "Available"));

        // 5. Twenty Harvest Lots matching Agri_70
        String[] crops = {
            "tomato", "rice", "onion", "carrot", "wheat",
            "tomato", "rice", "onion", "carrot", "wheat",
            "tomato", "rice", "onion", "carrot", "wheat",
            "tomato", "rice", "onion", "carrot", "wheat"
        };

        int[] quantities = {
            500, 1200, 400, 300, 900,
            700, 600, 500, 450, 500,
            350, 750, 250, 500, 600,
            450, 400, 600, 250, 800
        };

        int[] deadlines = {
            12, 48, 15, 10, 48,
            14, 48, 16, 12, 48,
            10, 36, 18, 12, 48,
            15, 36, 20, 12, 48
        };

        List<Farmer> farmers = dm.getFarmers();
        for (int i = 0; i < 20; i++) {
            Farmer f = farmers.get(i % farmers.size());
            String farmLoc = "F0" + ((i % 5) + 1);

            int deadline = deadlines[i];
            int urgency = 3;
            if (deadline <= 10) urgency = 5;
            else if (deadline <= 15) urgency = 4;
            else if (deadline <= 24) urgency = 3;
            else if (deadline <= 36) urgency = 2;
            else urgency = 1;

            HarvestLot lot = new HarvestLot(
                String.format("LOT%02d", i + 1),
                f,
                crops[i],
                quantities[i],
                urgency,
                deadline,
                farmLoc
            );
            dm.getHarvestLots().add(lot);
        }

        // Save persistent files and notify listeners
        dm.notifyDataChanged();
    }
}
