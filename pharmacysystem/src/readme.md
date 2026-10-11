# MediCare - Pharmacy Inventory and Billing System
Case Study 110 | Java Programming | B.Tech CSE 2025-29, Semester III

## How to run
Needs a JDK (Java 11 or newer). Open a terminal in this folder:

    cd src
    javac -encoding UTF-8 *.java
    java Main

(or just open the folder in IntelliJ and run Main)

The first run creates a `data/` folder with sample medicines and suppliers.
All later changes are saved there automatically.

## Objectives -> where they are met
| Objective / Outcome                    | Where                                                      |
|----------------------------------------|------------------------------------------------------------|
| Maintain medicine & supplier records   | MedicinePanel, SupplierPanel (add / update / delete / search) |
| Track stock and expiry dates           | Medicine.getStatus(), Dashboard, Medicines table           |
| Process sales and billing              | BillingPanel + DataStore.createSale() (5% GST, receipt)    |
| Identify low-stock and expired items   | Dashboard alerts, status filter, Reports                   |
| Generate inventory reports             | ReportPanel (4 reports + export to CSV)                    |
| Handle invalid input & business rules  | Validator + custom exceptions (see below)                  |
| Clear GUI interaction, summaries       | Swing GUI, Dashboard                                       |

## Required Java concepts
| Concept           | Where                                                                   |
|-------------------|-------------------------------------------------------------------------|
| Enum              | `Category` (medicine types), `StockStatus` (In Stock / Low / Out / Expired) |
| Custom Exceptions | `PharmacyException` (parent) -> `InvalidInputException`, `DuplicateRecordException`, `RecordNotFoundException`, `InsufficientStockException`, `ExpiredMedicineException` |
| File I/O          | `DataStore.save()/load()` -> data/suppliers.txt, medicines.txt, sales.txt; receipts saved to data/receipts; reports exported as CSV |
| Swing             | `MainFrame`, `DashboardPanel`, `MedicinePanel`, `SupplierPanel`, `BillingPanel`, `ReportPanel`, styled by `UI` |
| Collections / OOP | `LinkedHashMap` (medicines, suppliers), `ArrayList` (sales, cart), model classes with encapsulation, interface `Refreshable` |

## Business rules (with justification)
* Expired medicines cannot be added as new stock or sold - protects patient safety.
* A bill cannot sell more than the stock available - keeps inventory accurate.
* All items are checked BEFORE stock is reduced, so a failed bill never changes stock.
* A supplier that still supplies a medicine cannot be deleted - avoids orphaned records.
* Duplicate medicine / supplier names are rejected.
* Status is calculated from quantity and expiry (never typed by hand), so it is always correct.
* Low stock = 10 units or fewer (`Medicine.LOW_STOCK_LIMIT`).
* Phone must be 10 digits, email must be valid, price > 0, quantity >= 0, date format yyyy-MM-dd.

## File guide
* Main.java - start here
* PharmacyGUI.java - the window, sidebar, theme (UI) and all 5 pages
* PharmacySystem.java - data (Collections), business rules, File I/O, Validator, and all custom exceptions
* Medicine.java - Medicine model + the Category and StockStatus enums
* Supplier.java - Supplier model
* Sale.java - Sale (bill) and SaleItem models
