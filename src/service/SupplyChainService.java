package service;

import model.*;
import java.util.*;

/**
 * Core business logic engine for agricultural supply chain processing.
 * Manages prioritization with PriorityQueue, destination matching,
 * Dijkstra shortest path routing, vehicle assignment, and partial allocations.
 */
public class SupplyChainService {
    private List<HarvestLot> lots;
    private List<Market> markets;
    private List<StorageCenter> stores;
    private List<Vehicle> vehicles;
    private RoadNetwork roads;
    private DestinationPolicy policy;
    private List<Shipment> shipments;

    public SupplyChainService(List<HarvestLot> lots, List<Market> markets,
                              List<StorageCenter> stores, List<Vehicle> vehicles,
                              RoadNetwork roads, List<Shipment> shipments) {
        this.lots = lots != null ? lots : new ArrayList<>();
        this.markets = markets != null ? markets : new ArrayList<>();
        this.stores = stores != null ? stores : new ArrayList<>();
        this.vehicles = vehicles != null ? vehicles : new ArrayList<>();
        this.roads = roads != null ? roads : new RoadNetwork();
        this.shipments = shipments != null ? shipments : new ArrayList<>();
        this.policy = new DestinationPolicy(this.roads, this.markets, this.stores);
    }

    /**
     * Executes supply chain processing for unallocated harvest lots.
     */
    public ProcessResult processLots() {
        ProcessResult result = new ProcessResult();

        // 1. Filter eligible unallocated lots
        List<HarvestLot> eligibleLots = new ArrayList<>();
        for (HarvestLot lot : lots) {
            if (lot.getRemainingQuantity() > 0) {
                eligibleLots.add(lot);
            }
        }

        result.setLotsExamined(eligibleLots.size());
        if (eligibleLots.isEmpty()) {
            result.addLog("No pending harvest lots to process. All lots are already fully allocated.");
            return result;
        }

        // 2. Prioritize lots using PriorityQueue
        // Natural ordering of HarvestLot: Urgency descending, Deadline ascending
        PriorityQueue<HarvestLot> queue = new PriorityQueue<>(eligibleLots);

        // Track vehicles used in this batch run to avoid overlapping active assignments
        Set<String> vehiclesAssignedThisBatch = new HashSet<>();

        int shipmentCounter = shipments.size() + 1;

        while (!queue.isEmpty()) {
            HarvestLot lot = queue.poll();
            String farmerName = lot.getFarmer() != null ? lot.getFarmer().getName() : "Farmer";
            boolean allocatedAny = false;

            // Loop to support partial allocations across multiple destinations if quantity remains
            while (lot.getRemainingQuantity() > 0) {
                boolean allocatedStep = false;

                // Step A: Attempt to find a suitable Market
                Market targetMarket = policy.findMarket(lot);
                if (targetMarket != null && targetMarket.getDemand(lot.getCrop()) > 0) {
                    Vehicle vehicle = findAvailableVehicle(lot.getRemainingQuantity(), vehiclesAssignedThisBatch);

                    if (vehicle != null) {
                        int marketDemand = targetMarket.getDemand(lot.getCrop());
                        int allocQty = Math.min(lot.getRemainingQuantity(), Math.min(marketDemand, vehicle.getCapacity()));

                        if (allocQty > 0) {
                            double travelTime = roads.shortestTime(lot.getLocation(), targetMarket.getLocation());
                            List<String> route = roads.shortestRoute(lot.getLocation(), targetMarket.getLocation());

                            if (travelTime != Double.MAX_VALUE && !route.isEmpty()) {
                                String shipmentId = String.format("SHP%03d", shipmentCounter++);
                                Shipment shipment = new Shipment(
                                    shipmentId, lot.getLotId(), farmerName, lot.getCrop(),
                                    "Market", targetMarket.getName(), targetMarket.getLocation(),
                                    vehicle.getId(), allocQty, travelTime, route, "Dispatched"
                                );

                                // Update state
                                lot.allocateQuantity(allocQty);
                                targetMarket.reduceDemand(lot.getCrop(), allocQty);
                                vehicle.setAvailable(false);
                                vehicle.setStatus("In Transit");
                                vehicle.setLocation(targetMarket.getLocation());
                                vehiclesAssignedThisBatch.add(vehicle.getId());

                                shipments.add(shipment);
                                result.addShipment(shipment);
                                allocatedAny = true;
                                allocatedStep = true;
                            }
                        }
                    }
                }

                // Step B: If not allocated to Market, attempt Storage Center
                if (!allocatedStep && lot.getRemainingQuantity() > 0) {
                    StorageCenter targetStore = policy.findStorage(lot);

                    if (targetStore != null && targetStore.availableCapacity() > 0) {
                        Vehicle vehicle = findAvailableVehicle(lot.getRemainingQuantity(), vehiclesAssignedThisBatch);

                        if (vehicle != null) {
                            int storeAvailable = targetStore.availableCapacity();
                            int allocQty = Math.min(lot.getRemainingQuantity(), Math.min(storeAvailable, vehicle.getCapacity()));

                            if (allocQty > 0) {
                                double travelTime = roads.shortestTime(lot.getLocation(), targetStore.getLocation());
                                List<String> route = roads.shortestRoute(lot.getLocation(), targetStore.getLocation());

                                if (travelTime != Double.MAX_VALUE && !route.isEmpty()) {
                                    String shipmentId = String.format("SHP%03d", shipmentCounter++);
                                    Shipment shipment = new Shipment(
                                        shipmentId, lot.getLotId(), farmerName, lot.getCrop(),
                                        "Storage Center", targetStore.getName(), targetStore.getLocation(),
                                        vehicle.getId(), allocQty, travelTime, route, "Dispatched"
                                    );

                                    // Update state
                                    lot.allocateQuantity(allocQty);
                                    targetStore.store(allocQty);
                                    vehicle.setAvailable(false);
                                    vehicle.setStatus("In Transit");
                                    vehicle.setLocation(targetStore.getLocation());
                                    vehiclesAssignedThisBatch.add(vehicle.getId());

                                    shipments.add(shipment);
                                    result.addShipment(shipment);
                                    allocatedAny = true;
                                    allocatedStep = true;
                                }
                            }
                        }
                    }
                }

                // If this step could not allocate anything, break out of partial allocation loop
                if (!allocatedStep) {
                    break;
                }
            }

            // Record explanation if some or all quantity remains unallocated
            if (lot.getRemainingQuantity() > 0) {
                String reason = determineUnallocatedReason(lot, vehiclesAssignedThisBatch);
                result.addUnallocatedReason(String.format("Lot %s (%s, %d kg remaining): %s",
                    lot.getLotId(), lot.getCrop(), lot.getRemainingQuantity(), reason));
            }
        }

        // Calculate total remaining unallocated
        int totalUnallocated = 0;
        for (HarvestLot l : lots) {
            totalUnallocated += l.getRemainingQuantity();
        }
        result.setTotalQuantityUnallocated(totalUnallocated);

        return result;
    }

    /**
     * Finds an available vehicle not already used in the current batch.
     * Prefers a vehicle that can carry the required amount, otherwise largest available.
     */
    private Vehicle findAvailableVehicle(int requiredCapacity, Set<String> assignedBatch) {
        Vehicle bestVehicle = null;

        for (Vehicle v : vehicles) {
            if (!v.isAvailable() || assignedBatch.contains(v.getId())) {
                continue;
            }

            // Ideal match: fits entire required amount
            if (v.getCapacity() >= requiredCapacity) {
                if (bestVehicle == null || v.getCapacity() < bestVehicle.getCapacity()) {
                    bestVehicle = v;
                }
            }
        }

        // If no vehicle fits the entire amount, pick the largest available vehicle for partial shipment
        if (bestVehicle == null) {
            for (Vehicle v : vehicles) {
                if (v.isAvailable() && !assignedBatch.contains(v.getId())) {
                    if (bestVehicle == null || v.getCapacity() > bestVehicle.getCapacity()) {
                        bestVehicle = v;
                    }
                }
            }
        }

        return bestVehicle;
    }

    private String determineUnallocatedReason(HarvestLot lot, Set<String> assignedBatch) {
        // Check vehicles
        boolean hasAnyAvailableVehicle = false;
        for (Vehicle v : vehicles) {
            if (v.isAvailable() && !assignedBatch.contains(v.getId())) {
                hasAnyAvailableVehicle = true;
                break;
            }
        }

        if (!hasAnyAvailableVehicle) {
            return "No available vehicles (all vehicles are in transit or assigned).";
        }

        // Check market demand
        boolean marketHasDemand = false;
        for (Market m : markets) {
            if (m.getDemand(lot.getCrop()) > 0) {
                marketHasDemand = true;
                break;
            }
        }

        // Check storage capacity & compatibility
        boolean storageCompatible = false;
        boolean storageHasSpace = false;
        for (StorageCenter s : stores) {
            if (s.acceptsCrop(lot.getCrop())) {
                storageCompatible = true;
                if (s.availableCapacity() > 0) {
                    storageHasSpace = true;
                    break;
                }
            }
        }

        if (!marketHasDemand && !storageCompatible) {
            return "No market demand and no compatible storage facility for " + lot.getCrop() + ".";
        }

        if (!marketHasDemand && storageCompatible && !storageHasSpace) {
            return "No market demand and compatible storage centers are at full capacity.";
        }

        return "Delivery travel time exceeds deadline or destination is unreachable.";
    }

    public List<HarvestLot> getLots() { return lots; }
    public List<Market> getMarkets() { return markets; }
    public List<StorageCenter> getStores() { return stores; }
    public List<Vehicle> getVehicles() { return vehicles; }
    public RoadNetwork getRoads() { return roads; }
    public List<Shipment> getShipments() { return shipments; }
}
