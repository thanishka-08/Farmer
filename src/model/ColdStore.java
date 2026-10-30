package model;

import java.util.Arrays;
import java.util.List;

/**
 * Cold Storage facility specialized for perishable crops like tomato, onion, carrot, apple.
 * Extends StorageCenter (Inheritance & Polymorphism).
 */
public class ColdStore extends StorageCenter {
    private static final List<String> DEFAULT_CROPS = Arrays.asList(
        "tomato", "onion", "carrot", "apple", "potato", "cabbage", "fruits", "vegetables"
    );

    public ColdStore(String id, String name, String location, int capacity) {
        super(id, name, location, capacity);
    }

    public ColdStore(String id, String name, String location, int capacity, int usedCapacity, String customCrops) {
        super(id, name, location, capacity, usedCapacity, customCrops);
    }

    @Override
    public boolean acceptsCrop(String crop) {
        if (crop == null || crop.trim().isEmpty()) return false;
        String clean = crop.trim().toLowerCase();

        // Check custom supported crops if set
        if (customSupportedCrops != null && !customSupportedCrops.trim().isEmpty()) {
            String[] tokens = customSupportedCrops.toLowerCase().split("[,;\\s]+");
            for (String t : tokens) {
                if (t.equalsIgnoreCase(clean)) return true;
            }
        }

        // Check default crops
        for (String def : DEFAULT_CROPS) {
            if (def.equalsIgnoreCase(clean)) return true;
        }
        return false;
    }

    @Override
    public String getStorageType() {
        return "Cold Storage";
    }

    @Override
    public String getDefaultSupportedCrops() {
        return "Tomato, Onion, Carrot, Apple, Vegetables";
    }
}
