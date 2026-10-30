# Agricultural Supply Chain Management System
### *College Mini-Project: Agricultural Supply Chain and Farmer Market Linkage*

A graphical desktop application built using **Java Swing** and **Object-Oriented Programming (OOP)**. The system connects farmers with wholesale markets and cold/dry storage centers, prioritizes harvest lots, calculates the shortest delivery routes and travel times using **Dijkstra’s Algorithm**, and creates traceable transport shipments.

---

## 1. Technologies & Data Structures Used

- **Language:** Java (JDK 17 / JDK 21 LTS)
- **GUI Toolkit:** Java Swing (`JFrame`, `JPanel`, `JButton`, `JTextField`, `JComboBox`, `JTable`, `JScrollPane`, `JOptionPane`, `CardLayout`)
- **Theme:** Clean Green-and-White theme (`#2E7D32` Forest Green, `#E8F5E9` Mint tint, `#FFFFFF` White)
- **Core Data Structures & Algorithms:**
  - `PriorityQueue<HarvestLot>`: Prioritizes harvest lots dynamically based on Urgency (Levels 1–5, descending) and Harvest Delivery Deadline (hours, ascending).
  - `HashMap<String, HashMap<String, Double>>`: Weighted adjacency graph representing the 12-node rural and market road network.
  - **Dijkstra's Shortest Path Algorithm**: Computes minimal travel time and builds full node-by-node path routes for vehicle dispatches.
  - `ArrayList<T>` & `TreeSet<T>`: Dynamic collections and ordered sets for lookups and tables.
  - **OOP Principles:**
    - **Encapsulation:** Private attributes with validated getters and setters across all models.
    - **Inheritance & Polymorphism:** `StorageCenter` abstract base class with specialized `ColdStore` and `DryStore` child classes implementing distinct crop acceptance rules (`acceptsCrop(crop)`).
    - **Composition:** `HarvestLot` containing `Farmer` references; `Shipment` referencing routes, vehicles, and destinations.
- **Data Persistence:** Lightweight CSV file storage located in `data/` (`farmers.csv`, `harvest_lots.csv`, `markets.csv`, `storage_centers.csv`, `vehicles.csv`, `shipments.csv`). Records are automatically saved on form updates and restored on application startup.

---

## 2. Project Directory Structure

```text
Agri_100/
│
├── .vscode/
│   ├── launch.json                   # VS Code F5 Run configuration
│   └── settings.json                 # VS Code Java source and output path settings
│
├── data/                             # CSV persistent storage files
│   ├── farmers.csv
│   ├── harvest_lots.csv
│   ├── markets.csv
│   ├── storage_centers.csv
│   ├── vehicles.csv
│   └── shipments.csv
│
├── src/
│   ├── Main.java                     # Application entry point (Look & Feel + Event Dispatch Thread)
│   │
│   ├── model/
│   │   ├── Farmer.java               # Farmer entity (ID, Name, Phone, Village)
│   │   ├── HarvestLot.java           # HarvestLot entity (Qty, Urgency 1-5, Deadline, Location, Status)
│   │   ├── Market.java               # Market entity (Crop requirements, Initial & Remaining Demand)
│   │   ├── StorageCenter.java        # Abstract StorageCenter base class
│   │   ├── ColdStore.java            # Cold Storage subclass (Tomato, Onion, Carrot, Apple)
│   │   ├── DryStore.java             # Dry Storage subclass (Rice, Paddy, Wheat, Grain)
│   │   ├── Vehicle.java              # Transport vehicle entity (ID, Plate, Capacity, Location, Status)
│   │   ├── Shipment.java             # Dispatch record (Lot, Dest, Qty, Vehicle, Dijkstra Route, Time)
│   │   └── RoadNetwork.java          # Graph representation + Dijkstra's Shortest Path Algorithm
│   │
│   ├── service/
│   │   ├── DestinationPolicy.java    # Evaluates Market demand vs. Storage suitability and deadlines
│   │   ├── SupplyChainService.java   # Core optimization engine (PriorityQueue matching & allocation)
│   │   └── ProcessResult.java        # Detailed batch processing execution results and logs
│   │
│   ├── storage/
│   │   ├── DataManager.java          # In-memory collections + CSV file read/write operations
│   │   └── SampleDataLoader.java     # Demonstration dataset loader (5 Farmers, 3 Markets, 2 Stores, etc.)
│   │
│   ├── ui/
│   │   ├── UITheme.java              # Color tokens, fonts, button styling, and table renderers
│   │   ├── DashboardFrame.java       # Main JFrame with navigation sidebar and CardLayout
│   │   ├── DashboardPanel.java       # KPI cards, Quick Action shortcuts, 'Process Harvest Lots' action
│   │   ├── FarmerPanel.java          # Farmer input form, JTable, search, update, delete
│   │   ├── HarvestLotPanel.java      # Lot input form (Farmer dropdown), JTable, search, update, delete
│   │   ├── MarketPanel.java          # Market demand form, JTable, search, update, delete
│   │   ├── StoragePanel.java         # Storage center form, JTable, type selector, capacity tracking
│   │   ├── VehiclePanel.java         # Fleet management form, JTable, availability toggle
│   │   ├── ShipmentPanel.java        # Dispatches table, Dijkstra route viewer, details modal
│   │   └── ReportPanel.java          # 7 summary reports + CSV file export dialog
│   │
│   └── test/
│       ├── SystemValidationTest.java # Automated verification suite for all 16 project test cases
│       └── SampleDataValidationTest.java
│
├── bin/                              # Compiled .class bytecode
├── compile.bat                       # One-click Windows compilation script
├── run.bat                           # One-click Windows launch script
└── README.md                         # Project documentation
```

---

## 3. How to Compile and Run on Windows

### Option A: Using the Batch Scripts (Easiest)
1. Double-click `compile.bat` to compile all source files into the `bin/` folder.
2. Double-click `run.bat` to start the application.

### Option B: Using Command Prompt / PowerShell
Open PowerShell or Command Prompt inside the `Agri_100` folder and run:
```powershell
# Compile all source files into the bin folder
javac -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\Main.java

# Run the application
java -cp bin Main
```

### Option C: In Visual Studio Code (VS Code)
1. In VS Code, open the folder: `c:\Users\thsni\OneDrive\Desktop\Agri_100`.
2. Ensure the official **Extension Pack for Java** is installed in VS Code.
3. Open `src/Main.java`.
4. Press `F5` or click the **Run** button at the top-right corner.

---

## 4. User Guide & Demonstration Workflow

### Workflow 1: Entering Custom Data via Swing Forms
1. **Farmer Management:**
   - Enter Farmer ID (`F101`), Name (`Ramesh`), Phone (`9876543210`), Village/Node (`F01`).
   - Click **Add Farmer**. The farmer appears in the table.
2. **Harvest Lot Management:**
   - Enter Lot ID (`LOT01`).
   - Select the registered farmer from the dropdown.
   - Enter Crop Name (`Tomato`), Quantity in kg (`500`), Urgency (1–5), Deadline in hours (`15`), and Farm Location (`F01`).
   - Click **Add Lot**.
3. **Market Management:**
   - Enter Market ID (`M01`), Market Name (`Green Market`), Location (`MARKET1`), Required Crop (`Tomato`), and Quantity (`300`).
   - Click **Add Market**.
4. **Storage Center Management:**
   - Enter Storage ID (`S01`), Name (`Cold Storage`), Location (`STORE1`), select `Cold Storage`, and Capacity (`1000`).
   - Click **Add Storage**.
5. **Vehicle Management:**
   - Enter Vehicle ID (`V01`), Vehicle Number (`KA-01-AB-1234`), Capacity (`600`), Location (`J1`), and Status (`Available`).
   - Click **Add Vehicle**.
6. **Execute Supply Chain Processing:**
   - Return to the **Dashboard** or click **PROCESS HARVEST LOTS**.
   - The engine automatically prioritizes the lot, calculates the shortest Dijkstra route (`F01 -> J1 -> MARKET1`), allocates 300 kg to Market 1, and assigns remaining 200 kg to Cold Storage if additional vehicles/stores are present.
   - A dialog displays the full execution breakdown.
7. **View Results:**
   - Open **Shipment Management** to see the created shipments, assigned vehicle, route path, and travel time.
   - Open **Reports** to view Farmer summaries, Crop summaries, Market fulfillment percentages, or click **Export CSV** to save report tables to a file.

### Workflow 2: Demonstration using Sample Data
1. Open the application.
2. On the **Dashboard**, click **LOAD SAMPLE DATA**.
   - Automatically loads 5 Farmers, 3 Markets (9 crop demand records), 2 Storage Centers, 3 Vehicles, and 20 Harvest Lots from the college benchmark problem.
3. Click **PROCESS HARVEST LOTS**.
4. View the optimization results in **Shipment Management** and **Reports**.

---

## 5. Summary of Automated Test Cases Verified

The automated test suite (`test.SystemValidationTest`) runs and passes all 16 test requirements:
1. `[PASS]` Add a farmer
2. `[PASS]` Add a harvest lot for that farmer
3. `[PASS]` Add a market with matching crop demand
4. `[PASS]` Add a suitable storage center
5. `[PASS]` Add a vehicle with sufficient capacity
6. `[PASS]` Process harvest lots
7. `[PASS]` Verify shipment creation and Dijkstra route calculation
8. `[PASS]` Verify market demand and remaining harvest quantities update correctly
9. `[PASS]` Test insufficient vehicle capacity (caps allocation to vehicle max capacity)
10. `[PASS]` Test unavailable vehicles (prevents dispatching unavailable fleet)
11. `[PASS]` Test insufficient market demand (routes surplus crop to compatible storage)
12. `[PASS]` Test full or incompatible storage (rejects unsuitable crops with reason)
13. `[PASS]` Test partial allocations (splits lot across multiple destinations seamlessly)
14. `[PASS]` Test invalid and duplicate inputs (validates positive numbers and unique IDs)
15. `[PASS]` Restart application and verify persistence (CSV restore check)
16. `[PASS]` Repeated processing prevention (avoids double allocation on repeated clicks)
