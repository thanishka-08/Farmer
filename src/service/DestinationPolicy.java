package service;

import model.HarvestLot;
import model.Market;
import model.RoadNetwork;
import model.StorageCenter;
import java.util.List;

/**
 * Evaluates candidate destinations (Markets and Storage Centers) for harvest lots.
 * Considers crop compatibility, available capacity/demand, travel times via Dijkstra,
 * and delivery deadlines.
 * Reused and enhanced from console Agri_70 implementation.
 */
public class DestinationPolicy {
    private RoadNetwork roads;
    private List<Market> markets;
    private List<StorageCenter> stores;

    public DestinationPolicy(RoadNetwork roads, List<Market> markets, List<StorageCenter> stores) {
        this.roads = roads;
        this.markets = markets;
        this.stores = stores;
    }

    public Market findMarket(HarvestLot lot) {
        if (lot == null || markets == null) return null;
        Market best = null;
        double bestTime = Double.MAX_VALUE;

        for (Market market : markets) {
            int demand = market.getDemand(lot.getCrop());
            if (demand <= 0) {
                continue;
            }

            double time = roads.shortestTime(lot.getLocation(), market.getLocation());
            if (time <= lot.getDeadline() && time < bestTime) {
                bestTime = time;
                best = market;
            }
        }

        // Fallback: If deadline is tight, check if market is still reachable even if time slightly exceeds
        // but within reasonable range (optional soft-check if no market found within deadline)
        if (best == null) {
            for (Market market : markets) {
                int demand = market.getDemand(lot.getCrop());
                if (demand > 0) {
                    double time = roads.shortestTime(lot.getLocation(), market.getLocation());
                    if (time < Double.MAX_VALUE && time <= (lot.getDeadline() * 1.25) && time < bestTime) {
                        bestTime = time;
                        best = market;
                    }
                }
            }
        }

        return best;
    }

    public StorageCenter findStorage(HarvestLot lot) {
        if (lot == null || stores == null) return null;
        StorageCenter best = null;
        double bestTime = Double.MAX_VALUE;

        for (StorageCenter store : stores) {
            if (!store.acceptsCrop(lot.getCrop()) || store.availableCapacity() <= 0) {
                continue;
            }

            double time = roads.shortestTime(lot.getLocation(), store.getLocation());
            if (time <= lot.getDeadline() && time < bestTime) {
                bestTime = time;
                best = store;
            }
        }

        // Fallback: If no storage strictly within deadline, find nearest available compatible storage
        if (best == null) {
            for (StorageCenter store : stores) {
                if (store.acceptsCrop(lot.getCrop()) && store.availableCapacity() > 0) {
                    double time = roads.shortestTime(lot.getLocation(), store.getLocation());
                    if (time < Double.MAX_VALUE && time < bestTime) {
                        bestTime = time;
                        best = store;
                    }
                }
            }
        }

        return best;
    }
}
