package test;

import model.*;
import service.*;
import storage.*;

import java.util.*;

/**
 * SystemDemonstration provides a complete console simulation and output view
 * of the Agricultural Supply Chain Management System.
 * 
 * Shows:
 * 1. Current System Topology & Master Data
 * 2. Dijkstra Shortest-Path Road Network Routing Analysis
 * 3. PriorityQueue Lot Prioritization (Urgency 1-5, Deadline)
 * 4. Batch Allocation Engine Execution & Shipment Dispatch Manifest
 * 5. Analytical Reports (Farmer, Crop, Market, Storage, Vehicle)
 */
public class SystemDemonstration {

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("              AGRICULTURAL SUPPLY CHAIN MANAGEMENT & FARMER MARKET LINKAGE                ");
        System.out.println("                     COMPLETE SYSTEM EXECUTION & OUTPUT REPORT                            ");
        System.out.println("==========================================================================================\n");

        // Load data from persistent CSV storage
        DataManager dm = new DataManager();

        // If data is empty, load sample demonstration dataset
        if (dm.getFarmers().isEmpty() || dm.getHarvestLots().isEmpty()) {
            System.out.println("[INFO] Loading standard demonstration dataset (Agri_70 / Agri_100 baseline)...");
            SampleDataLoader.populateSampleData(dm);
        }

        // -------------------------------------------------------------------------
        // 1. MASTER DATA SUMMARY
        // -------------------------------------------------------------------------
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("1. SYSTEM OVERVIEW & REGISTERED ENTITIES");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("  • Total Registered Farmers  : %d\n", dm.getTotalFarmers());
        System.out.printf("  • Total Harvest Lots        : %d\n", dm.getTotalHarvestLots());
        System.out.printf("  • Total Pending Harvest Qty : %,d kg\n", dm.getTotalPendingHarvestQty());
        System.out.printf("  • Wholesale Markets         : %d\n", dm.getMarkets().size());
        System.out.printf("  • Storage Centers           : %d\n", dm.getStorageCenters().size());
        System.out.printf("  • Transport Fleet Vehicles  : %d\n", dm.getVehicles().size());
        System.out.println();

        System.out.println("Registered Farmers:");
        System.out.printf("  %-8s %-20s %-15s %-20s\n", "ID", "Name", "Phone", "Location/Village");
        System.out.println("  -------------------------------------------------------------------");
        for (Farmer f : dm.getFarmers()) {
            System.out.printf("  %-8s %-20s %-15s %-20s\n", f.getFarmerId(), f.getName(), f.getPhone(), f.getVillage());
        }
        System.out.println();

        System.out.println("Transport Fleet Status:");
        System.out.printf("  %-8s %-18s %-12s %-10s %-12s\n", "ID", "License Plate", "Capacity", "Location", "Status");
        System.out.println("  -------------------------------------------------------------------");
        for (Vehicle v : dm.getVehicles()) {
            System.out.printf("  %-8s %-18s %-12s %-10s %-12s\n", v.getId(), v.getVehicleNumber(), v.getCapacity() + " kg", v.getLocation(), v.getStatus());
        }
        System.out.println();

        System.out.println("Wholesale Markets & Crop Demand Requirements:");
        System.out.printf("  %-10s %-18s %-10s %-12s %-14s %-14s\n", "ID", "Market Name", "Location", "Crop", "Init Demand", "Rem Demand");
        System.out.println("  -----------------------------------------------------------------------------------");
        for (Market m : dm.getMarkets()) {
            System.out.printf("  %-10s %-18s %-10s %-12s %-14s %-14s\n",
                m.getMarketId(), m.getName(), m.getLocation(), m.getCrop(),
                m.getInitialDemand() + " kg", m.getRemainingDemand() + " kg");
        }
        System.out.println();

        System.out.println("Storage Centers:");
        System.out.printf("  %-8s %-22s %-10s %-15s %-18s %-20s\n", "ID", "Facility Name", "Location", "Type", "Capacity Available", "Accepted Crops");
        System.out.println("  -----------------------------------------------------------------------------------------------");
        for (StorageCenter s : dm.getStorageCenters()) {
            System.out.printf("  %-8s %-22s %-10s %-15s %-18s %-20s\n",
                s.getId(), s.getName(), s.getLocation(), s.getStorageType(),
                s.availableCapacity() + " / " + s.getCapacity() + " kg", s.getSupportedCropsDisplay());
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // 2. DIJKSTRA'S ALGORITHM ROUTING SAMPLE MATRIX
        // -------------------------------------------------------------------------
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("2. DIJKSTRA'S SHORTEST PATH ALGORITHM: ROUTE & TRAVEL TIME ANALYSIS");
        System.out.println("------------------------------------------------------------------------------------------");
        RoadNetwork net = dm.getRoadNetwork();
        String[] sampleSources = {"F01", "F02", "F03", "F04", "F05"};
        String[] sampleTargets = {"MARKET1", "MARKET2", "MARKET3", "STORE1", "STORE2"};

        for (String src : sampleSources) {
            for (String dest : sampleTargets) {
                double time = net.shortestTime(src, dest);
                List<String> route = net.shortestRoute(src, dest);
                String routeStr = String.join(" -> ", route);
                System.out.printf("  Route %-4s -> %-8s : Travel Time = %4.1f hrs | Shortest Path: %s\n",
                    src, dest, time, routeStr);
            }
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // 3. PRIORITYQUEUE RANKING
        // -------------------------------------------------------------------------
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("3. PRIORITY QUEUE LOT RANKING (Urgency Descending, Deadline Ascending)");
        System.out.println("------------------------------------------------------------------------------------------");
        PriorityQueue<HarvestLot> pq = new PriorityQueue<>(dm.getHarvestLots());
        int rank = 1;
        System.out.printf("  %-5s %-8s %-12s %-10s %-10s %-10s %-8s\n", "Rank", "Lot ID", "Crop", "Quantity", "Urgency", "Deadline", "Origin");
        System.out.println("  -------------------------------------------------------------------");
        List<HarvestLot> orderedLots = new ArrayList<>();
        while (!pq.isEmpty()) {
            HarvestLot lot = pq.poll();
            orderedLots.add(lot);
            System.out.printf("  %-5d %-8s %-12s %-10s %-10s %-10s %-8s\n",
                rank++, lot.getLotId(), lot.getCrop(), lot.getOriginalQuantity() + " kg",
                "Level " + lot.getUrgency(), lot.getDeadline() + " hrs", lot.getLocation());
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // 4. SUPPLY CHAIN PROCESSING EXECUTION
        // -------------------------------------------------------------------------
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("4. SUPPLY CHAIN OPTIMIZATION & DISPATCH EXECUTION");
        System.out.println("------------------------------------------------------------------------------------------");
        SupplyChainService service = new SupplyChainService(
            dm.getHarvestLots(), dm.getMarkets(), dm.getStorageCenters(),
            dm.getVehicles(), dm.getRoadNetwork(), dm.getShipments()
        );

        ProcessResult result = service.processLots();
        dm.saveAll();

        System.out.printf("  • Harvest Lots Examined    : %d\n", result.getLotsExamined());
        System.out.printf("  • New Shipments Created    : %d\n", result.getShipmentsCreated());
        System.out.printf("  • Total Quantity Allocated : %,d kg\n", result.getTotalQuantityAllocated());
        System.out.printf("  • Remaining Unallocated    : %,d kg\n", result.getTotalQuantityUnallocated());
        System.out.println();

        if (!result.getNewShipments().isEmpty()) {
            System.out.println("DISPATCHED SHIPMENTS MANIFEST:");
            System.out.printf("  %-8s %-8s %-10s %-8s %-16s %-15s %-8s %-25s %-10s\n",
                "ShipID", "LotID", "Crop", "Qty", "Destination", "Type", "Vehicle", "Dijkstra Route", "Time");
            System.out.println("  --------------------------------------------------------------------------------------------------------------------");
            for (Shipment s : result.getNewShipments()) {
                System.out.printf("  %-8s %-8s %-10s %-8s %-16s %-15s %-8s %-25s %-10s\n",
                    s.getShipmentId(), s.getLotId(), s.getCrop(), s.getQuantity() + " kg",
                    s.getDestinationName(), s.getDestinationType(), s.getVehicleId(),
                    s.getRouteDisplay(), String.format("%.1f hrs", s.getTravelTime()));
            }
            System.out.println();
        }

        if (!result.getUnallocatedReasons().isEmpty()) {
            System.out.println("UNALLOCATED LOT AUDIT LOG (Resource Bottleneck Notes):");
            int shown = 0;
            for (String reason : result.getUnallocatedReasons()) {
                System.out.printf("  [NOTE] %s\n", reason);
                shown++;
                if (shown >= 8) {
                    System.out.printf("  ... and %d more pending lots awaiting subsequent vehicle availability.\n",
                        result.getUnallocatedReasons().size() - shown);
                    break;
                }
            }
            System.out.println();
        }

        // -------------------------------------------------------------------------
        // 5. POST-ALLOCATION SUMMARY KPIS
        // -------------------------------------------------------------------------
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("5. UPDATED SYSTEM METRICS & KPI STATUS");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("  • Total Farmers            : %d\n", dm.getTotalFarmers());
        System.out.printf("  • Total Harvest Lots       : %d\n", dm.getTotalHarvestLots());
        System.out.printf("  • Pending Harvest Qty      : %,d kg\n", dm.getTotalPendingHarvestQty());
        System.out.printf("  • Remaining Market Demand  : %,d kg\n", dm.getRemainingMarketDemand());
        System.out.printf("  • Available Storage Space  : %,d kg\n", dm.getAvailableStorageCapacity());
        System.out.printf("  • Available Vehicles       : %d\n", dm.getAvailableVehiclesCount());
        System.out.printf("  • Total Active Shipments   : %d\n", dm.getTotalShipmentsCount());
        System.out.println();

        System.out.println("==========================================================================================");
        System.out.println("                   DEMONSTRATION RUN COMPLETE - DATA SAVED SUCCESSFULLY                   ");
        System.out.println("==========================================================================================");
    }
}
