-- ====================================================================
-- AGRICULTURAL SUPPLY CHAIN AND FARMER MARKET LINKAGE DATABASE
-- Database: agri_supply_chain
-- Compatible with XAMPP MySQL / MariaDB & phpMyAdmin
-- ====================================================================

CREATE DATABASE IF NOT EXISTS `agri_supply_chain` 
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `agri_supply_chain`;

-- 1. Table: farmers
DROP TABLE IF EXISTS `farmers`;
CREATE TABLE `farmers` (
    `farmer_id` VARCHAR(20) PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `village` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO `farmers` (`farmer_id`, `name`, `phone`, `village`) VALUES
('F101', 'Ramesh Patel', '9876543211', 'Village A (F01)'),
('F102', 'Suresh Kumar', '9876543212', 'Village B (F02)'),
('F103', 'Ananya Sharma', '9876543213', 'Village C (F03)'),
('F104', 'Rajesh Reddy', '9876543214', 'Village D (F04)'),
('F105', 'Meena Devi', '9876543215', 'Village E (F05)'),
('F106', 'Ramu', '7708945788', 'VillageF(F01)');

-- 2. Table: harvest_lots
DROP TABLE IF EXISTS `harvest_lots`;
CREATE TABLE `harvest_lots` (
    `lot_id` VARCHAR(20) PRIMARY KEY,
    `farmer_id` VARCHAR(20) NOT NULL,
    `crop` VARCHAR(50) NOT NULL,
    `original_quantity` INT NOT NULL,
    `processed_quantity` INT NOT NULL DEFAULT 0,
    `remaining_quantity` INT NOT NULL,
    `urgency_level` INT NOT NULL COMMENT '1 (lowest) to 5 (highest)',
    `deadline_hours` INT NOT NULL,
    `origin_location` VARCHAR(50) NOT NULL,
    `status` VARCHAR(20) GENERATED ALWAYS AS (
        CASE 
            WHEN `remaining_quantity` = 0 THEN 'Completed'
            WHEN `processed_quantity` > 0 THEN 'Partial'
            ELSE 'Pending'
        END
    ) VIRTUAL,
    FOREIGN KEY (`farmer_id`) REFERENCES `farmers`(`farmer_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO `harvest_lots` (`lot_id`, `farmer_id`, `crop`, `original_quantity`, `processed_quantity`, `remaining_quantity`, `urgency_level`, `deadline_hours`, `origin_location`) VALUES
('LOT01', 'F101', 'tomato', 500, 0, 500, 4, 12, 'F01'),
('LOT02', 'F102', 'rice', 1200, 0, 1200, 1, 48, 'F02'),
('LOT03', 'F103', 'onion', 400, 0, 400, 4, 15, 'F03'),
('LOT04', 'F104', 'carrot', 300, 0, 300, 5, 10, 'F04'),
('LOT05', 'F105', 'wheat', 900, 0, 900, 1, 48, 'F05'),
('LOT06', 'F101', 'tomato', 700, 0, 700, 4, 14, 'F01'),
('LOT07', 'F102', 'rice', 600, 0, 600, 1, 48, 'F02'),
('LOT08', 'F103', 'onion', 500, 0, 500, 3, 16, 'F03'),
('LOT09', 'F104', 'carrot', 450, 0, 450, 4, 12, 'F04'),
('LOT10', 'F105', 'wheat', 500, 0, 500, 1, 48, 'F05'),
('LOT11', 'F101', 'tomato', 350, 0, 350, 5, 10, 'F01'),
('LOT12', 'F102', 'rice', 750, 0, 750, 2, 36, 'F02'),
('LOT13', 'F103', 'onion', 250, 0, 250, 3, 18, 'F03'),
('LOT14', 'F104', 'carrot', 500, 0, 500, 4, 12, 'F04'),
('LOT15', 'F105', 'wheat', 600, 0, 600, 1, 48, 'F05'),
('LOT16', 'F101', 'tomato', 450, 0, 450, 4, 15, 'F01'),
('LOT17', 'F102', 'rice', 400, 0, 400, 2, 36, 'F02'),
('LOT18', 'F103', 'onion', 600, 0, 600, 3, 20, 'F03'),
('LOT19', 'F104', 'carrot', 250, 0, 250, 4, 12, 'F04'),
('LOT20', 'F105', 'wheat', 800, 0, 800, 1, 48, 'F05');

-- 3. Table: markets
DROP TABLE IF EXISTS `markets`;
CREATE TABLE `markets` (
    `market_id` VARCHAR(20) PRIMARY KEY,
    `market_name` VARCHAR(100) NOT NULL,
    `location_node` VARCHAR(50) NOT NULL,
    `crop` VARCHAR(50) NOT NULL,
    `initial_demand` INT NOT NULL,
    `remaining_demand` INT NOT NULL
) ENGINE=InnoDB;

INSERT INTO `markets` (`market_id`, `market_name`, `location_node`, `crop`, `initial_demand`, `remaining_demand`) VALUES
('M01-TOM', 'Green Market', 'MARKET1', 'tomato', 600, 600),
('M01-RIC', 'Green Market', 'MARKET1', 'rice', 800, 800),
('M01-ONI', 'Green Market', 'MARKET1', 'onion', 400, 400),
('M02-TOM', 'City Market', 'MARKET2', 'tomato', 300, 300),
('M02-CAR', 'City Market', 'MARKET2', 'carrot', 400, 400),
('M02-RIC', 'City Market', 'MARKET2', 'rice', 500, 500),
('M03-ONI', 'Farmers Market', 'MARKET3', 'onion', 500, 500),
('M03-WHE', 'Farmers Market', 'MARKET3', 'wheat', 700, 700),
('M03-CAR', 'Farmers Market', 'MARKET3', 'carrot', 300, 300);

-- 4. Table: storage_centers
DROP TABLE IF EXISTS `storage_centers`;
CREATE TABLE `storage_centers` (
    `storage_id` VARCHAR(20) PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `location_node` VARCHAR(50) NOT NULL,
    `storage_type` VARCHAR(50) NOT NULL COMMENT 'Cold Storage or Dry Storage',
    `total_capacity` INT NOT NULL,
    `used_capacity` INT NOT NULL DEFAULT 0,
    `available_capacity` INT GENERATED ALWAYS AS (`total_capacity` - `used_capacity`) VIRTUAL,
    `accepted_crops` VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

INSERT INTO `storage_centers` (`storage_id`, `name`, `location_node`, `storage_type`, `total_capacity`, `used_capacity`, `accepted_crops`) VALUES
('S01', 'Cold Storage', 'STORE1', 'Cold Storage', 1000, 0, 'Tomato, Onion, Carrot, Apple, Vegetables'),
('S02', 'Dry Storage', 'STORE2', 'Dry Storage', 1500, 0, 'Rice, Paddy, Wheat, Grain, Corn, Pulses');

-- 5. Table: vehicles
DROP TABLE IF EXISTS `vehicles`;
CREATE TABLE `vehicles` (
    `vehicle_id` VARCHAR(20) PRIMARY KEY,
    `license_plate` VARCHAR(50) NOT NULL,
    `capacity_kg` INT NOT NULL,
    `current_location` VARCHAR(50) NOT NULL,
    `is_available` BOOLEAN NOT NULL DEFAULT TRUE,
    `status` VARCHAR(50) NOT NULL DEFAULT 'Available'
) ENGINE=InnoDB;

INSERT INTO `vehicles` (`vehicle_id`, `license_plate`, `capacity_kg`, `current_location`, `is_available`, `status`) VALUES
('V01', 'KA-01-AB-1001', 500, 'J1', 1, 'Available'),
('V02', 'KA-01-AB-1002', 800, 'J1', 1, 'Available'),
('V03', 'KA-01-AB-1003', 1200, 'J2', 1, 'Available');

-- 6. Table: shipments
DROP TABLE IF EXISTS `shipments`;
CREATE TABLE `shipments` (
    `shipment_id` VARCHAR(30) PRIMARY KEY,
    `lot_id` VARCHAR(20) NOT NULL,
    `farmer_name` VARCHAR(100) NOT NULL,
    `crop` VARCHAR(50) NOT NULL,
    `destination_type` VARCHAR(50) NOT NULL,
    `destination_name` VARCHAR(100) NOT NULL,
    `destination_location` VARCHAR(50) NOT NULL,
    `vehicle_id` VARCHAR(20) NOT NULL,
    `quantity_kg` INT NOT NULL,
    `travel_time_hours` DECIMAL(5,2) NOT NULL,
    `dijkstra_route` VARCHAR(255) NOT NULL,
    `dispatch_status` VARCHAR(50) NOT NULL DEFAULT 'Dispatched',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO `shipments` (`shipment_id`, `lot_id`, `farmer_name`, `crop`, `destination_type`, `destination_name`, `destination_location`, `vehicle_id`, `quantity_kg`, `travel_time_hours`, `dijkstra_route`, `dispatch_status`) VALUES
('SHP001', 'LOT04', 'Rajesh Reddy', 'carrot', 'Market', 'City Market', 'MARKET2', 'V01', 300, 5.00, 'F04 -> J2 -> MARKET2', 'Dispatched'),
('SHP002', 'LOT11', 'Ramesh Patel', 'tomato', 'Market', 'Green Market', 'MARKET1', 'V02', 350, 4.00, 'F01 -> J1 -> MARKET1', 'Dispatched'),
('SHP003', 'LOT09', 'Rajesh Reddy', 'carrot', 'Market', 'City Market', 'MARKET2', 'V03', 100, 5.00, 'F04 -> J2 -> MARKET2', 'Dispatched');
