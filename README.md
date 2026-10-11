MediCare - Pharmacy Inventory & Billing System

A Java Swing desktop application for managing a pharmacy: medicine and supplier records, stock and expiry tracking, billing with GST, and inventory reports. All data is saved in plain text files.

Course: Java Programming | B.Tech CSE 2025-29, Semester III (Case Study 110)

Features
Dashboard - totals for medicines, suppliers, low/out-of-stock items, expired items and revenue, plus low-stock and expired alert tables.
Medicines - add, update, delete and search medicines; filter by status (In Stock / Low Stock / Out of Stock / Expired).
Suppliers - add, update, delete and search suppliers.
Billing - build a cart, apply 5% GST, generate a bill, view the receipt and save it to a file. Stock is reduced automatically.
Reports - Full inventory, Low stock, Expired items and Sales history, with Export to CSV.
Validation - friendly error pop-ups for invalid input and broken business rules.
Requirements
JDK 11 or newer
Any OS (Windows, macOS, Linux)
How to Run

From the project folder:

bash
cd src
javac -encoding UTF-8 *.java
java Main

Or open the folder in IntelliJ IDEA and run Main.

The data/ folder is created relative to the folder you run the program from. On the first run, sample medicines and suppliers are created automatically. Every later change is saved there.

Project Structure
pharmacysystem/
├── src/
│   ├── Main.java            Entry point
│   ├── PharmacyGUI.java     Main window, styling (UI class) and all 5 pages
│   ├── PharmacySystem.java  Data (Collections), business rules, File I/O,
│   │                        Validator and all custom exceptions
│   ├── Medicine.java        Medicine model + Category and StockStatus enums
│   ├── Supplier.java        Supplier model
│   ├── Sale.java            Sale (bill) and SaleItem models
│   └── readme.md
├── data/
│   ├── medicines.txt
│   ├── suppliers.txt
│   ├── sales.txt
│   └── receipts/            Saved bills (created on demand)
└── out/                     Compiled classes (IntelliJ)
Architecture
Layer	Classes	Role
Model	Medicine, Supplier, Sale, SaleItem	Hold data and small rules
Logic	PharmacySystem, Validator, exceptions	Collections, business rules, save/load
View	PharmacyGUI, UI, DashboardPanel, MedicinePanel, SupplierPanel, BillingPanel, ReportPanel	Swing screens
Java Concepts Used
Concept	Where
Enum	Category (Tablet, Capsule, Syrup, Injection, Ointment, Drops), StockStatus (In Stock, Low Stock, Out of Stock, Expired)
Custom exceptions	PharmacyException (parent) -> InvalidInputException, DuplicateRecordException, RecordNotFoundException, InsufficientStockException, ExpiredMedicineException
File I/O	PharmacySystem.save() / load() (java.nio Files), receipt saving in BillingPanel, CSV export in ReportPanel
Swing GUI	JFrame, CardLayout, JTable, JComboBox, JSpinner, JDialog, custom painted buttons and cards
Collections	LinkedHashMap (medicines, suppliers), ArrayList (sales, cart)
OOP	Encapsulation, inheritance (exceptions, GUI classes), interface Refreshable, polymorphism, generics (<T extends JPanel & Refreshable>)
Business Rules
Rule	Reason
Expired medicines cannot be added as new stock or sold	Patient safety
A bill cannot sell more than the available stock	Accurate inventory
All cart items are checked before any stock is reduced	A failed bill never changes stock
A supplier that still supplies a medicine cannot be deleted	Avoids orphaned records
Duplicate medicine / supplier names are rejected (case-insensitive)	Data cleanliness
Status is calculated from quantity and expiry, never typed	Always correct
Low stock = 10 units or fewer (Medicine.LOW_STOCK_LIMIT)	Restock warning
GST is 5% (Sale.GST_RATE)	Billing
Input validation
Text fields cannot be empty or contain |
Phone: exactly 10 digits
Email: valid format (name@example.com)
Price: a number greater than 0
Quantity: a whole number, 0 or more
Expiry date: yyyy-MM-dd (e.g. 2027-12-31)
Data Files

Records are stored one per line, fields separated by |.

medicines.txt

M001|Paracetamol 500mg|TABLET|S001|2.5|120|2027-11-30
id | name | category | supplierId | price | quantity | expiryDate

suppliers.txt

S001|Sun Pharma Distributors|9820011122|orders@sunpharma.in|Mumbai
id | name | phone | email | city

sales.txt (one line per item sold; lines with the same bill number form one bill)

B0001|2026-10-03T16:46:20.289490|rudra yadav|M005|Insulin Pen|3|450.0
billNo | dateTime | customer | medicineId | medicineName | quantity | unitPrice

IDs are generated automatically: S001 (suppliers), M001 (medicines), B0001 (bills).

How to Use
Add a supplier on the Suppliers page (a medicine needs a supplier).
Add medicines on the Medicines page. Click a table row to edit or delete it.
Sell on the Billing page: pick a medicine, set the quantity, click Add to cart, then Generate bill. Save the receipt if needed.
Check alerts on the Dashboard and run reports on the Reports page (export to CSV).
Possible Improvements
Replace text files with a database (JDBC / MySQL)
User login and roles
Sales returns and supplier purchase orders
Print receipts directly to a printer
