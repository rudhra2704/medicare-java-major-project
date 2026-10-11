import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * applies the business rules, and saves everything to text files (File I/O) in the "data" folder.
 */

class PharmacySystem {
    private static final Path DIR = Paths.get("data");
    private static final Path SUPPLIER_FILE = DIR.resolve("suppliers.txt");
    private static final Path MEDICINE_FILE = DIR.resolve("medicines.txt");
    private static final Path SALES_FILE = DIR.resolve("sales.txt");

    private final Map<String, Supplier> suppliers = new LinkedHashMap<>();
    private final Map<String, Medicine> medicines = new LinkedHashMap<>();
    private final List<Sale> sales = new ArrayList<>();

    public PharmacySystem() {
        try {
            if (Files.exists(SUPPLIER_FILE) && Files.exists(MEDICINE_FILE)) {
                load();
            } else {
                addSampleData();   // first run: give the app some demo data
                save();
            }
        } catch (IOException | PharmacyException e) {
            System.err.println("Storage problem: " + e.getMessage());
        }
    }

    // ===================== SUPPLIERS =====================

    public Supplier addSupplier(String name, String phone, String email, String city) throws PharmacyException {
        name = Validator.text("Supplier name", name);
        phone = Validator.phone(phone);
        email = Validator.email(email);
        city = Validator.text("City", city);
        for (Supplier s : suppliers.values())
            if (s.getName().equalsIgnoreCase(name))
                throw new DuplicateRecordException("A supplier named '" + name + "' already exists.");

        Supplier s = new Supplier(nextId("S", suppliers.keySet(), 3), name, phone, email, city);
        suppliers.put(s.getId(), s);
        save();
        return s;
    }

    public void updateSupplier(String id, String name, String phone, String email, String city) throws PharmacyException {
        Supplier s = findSupplier(id);
        name = Validator.text("Supplier name", name);
        phone = Validator.phone(phone);
        email = Validator.email(email);
        city = Validator.text("City", city);
        for (Supplier other : suppliers.values())
            if (other != s && other.getName().equalsIgnoreCase(name))
                throw new DuplicateRecordException("A supplier named '" + name + "' already exists.");
        s.setName(name); s.setPhone(phone); s.setEmail(email); s.setCity(city);
        save();
    }

    public void deleteSupplier(String id) throws PharmacyException {
        Supplier s = findSupplier(id);
        for (Medicine m : medicines.values())
            if (m.getSupplierId().equals(id))
                throw new InvalidInputException("Cannot delete " + s.getName() + " - it still supplies " + m.getName() + ".");
        suppliers.remove(id);
        save();
    }

    public Supplier getSupplier(String id) { return suppliers.get(id); }
    public List<Supplier> getSuppliers() { return new ArrayList<>(suppliers.values()); }

    public String getSupplierName(String id) {
        Supplier s = suppliers.get(id);
        return s == null ? "Unknown" : s.getName();
    }

    private Supplier findSupplier(String id) throws RecordNotFoundException {
        Supplier s = suppliers.get(id);
        if (s == null) throw new RecordNotFoundException("Supplier " + id + " was not found.");
        return s;
    }

    // ===================== MEDICINES =====================

    public Medicine addMedicine(String name, Category category, Supplier supplier,
                                String priceText, String qtyText, String expiryText) throws PharmacyException {
        name = Validator.text("Medicine name", name);
        if (category == null) throw new InvalidInputException("Please select a category.");
        if (supplier == null) throw new InvalidInputException("Please select a supplier (add one first if the list is empty).");
        double price = Validator.price(priceText);
        int qty = Validator.quantity(qtyText);
        LocalDate expiry = Validator.date(expiryText);
        if (expiry.isBefore(LocalDate.now()))
            throw new ExpiredMedicineException("Cannot add a medicine that is already expired (" + expiry + ").");
        for (Medicine m : medicines.values())
            if (m.getName().equalsIgnoreCase(name))
                throw new DuplicateRecordException("'" + name + "' already exists. Use Update to change its stock.");

        Medicine m = new Medicine(nextId("M", medicines.keySet(), 3), name, category, supplier.getId(), price, qty, expiry);
        medicines.put(m.getId(), m);
        save();
        return m;
    }

    public void updateMedicine(String id, String name, Category category, Supplier supplier,
                               String priceText, String qtyText, String expiryText) throws PharmacyException {
        Medicine m = findMedicine(id);
        name = Validator.text("Medicine name", name);
        if (category == null) throw new InvalidInputException("Please select a category.");
        if (supplier == null) throw new InvalidInputException("Please select a supplier.");
        double price = Validator.price(priceText);
        int qty = Validator.quantity(qtyText);
        LocalDate expiry = Validator.date(expiryText);
        for (Medicine other : medicines.values())
            if (other != m && other.getName().equalsIgnoreCase(name))
                throw new DuplicateRecordException("Another medicine named '" + name + "' already exists.");

        m.setName(name); m.setCategory(category); m.setSupplierId(supplier.getId());
        m.setPrice(price); m.setQuantity(qty); m.setExpiryDate(expiry);
        save();
    }

    public void deleteMedicine(String id) throws PharmacyException {
        findMedicine(id);
        medicines.remove(id);
        save();
    }

    public Medicine getMedicine(String id) { return medicines.get(id); }
    public List<Medicine> getMedicines() { return new ArrayList<>(medicines.values()); }

    private Medicine findMedicine(String id) throws RecordNotFoundException {
        Medicine m = medicines.get(id);
        if (m == null) throw new RecordNotFoundException("Medicine " + id + " was not found.");
        return m;
    }

    /** Search by id / name / category / supplier, optionally filtered by status (null = any status). */
    public List<Medicine> searchMedicines(String keyword, StockStatus status) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Medicine> result = new ArrayList<>();
        for (Medicine m : medicines.values()) {
            boolean match = k.isEmpty()
                    || m.getId().toLowerCase().contains(k)
                    || m.getName().toLowerCase().contains(k)
                    || m.getCategory().toString().toLowerCase().contains(k)
                    || getSupplierName(m.getSupplierId()).toLowerCase().contains(k);
            if (match && (status == null || m.getStatus() == status)) result.add(m);
        }
        return result;
    }

    /** Low-stock OR out-of-stock items that are not expired. */
    public List<Medicine> getLowStock() {
        List<Medicine> list = new ArrayList<>();
        for (Medicine m : medicines.values()) {
            StockStatus s = m.getStatus();
            if (s == StockStatus.LOW_STOCK || s == StockStatus.OUT_OF_STOCK) list.add(m);
        }
        return list;
    }

    public List<Medicine> getExpired() {
        List<Medicine> list = new ArrayList<>();
        for (Medicine m : medicines.values()) if (m.isExpired()) list.add(m);
        return list;
    }

    /** Medicines that can be billed right now. */
    public List<Medicine> getSellable() {
        List<Medicine> list = new ArrayList<>();
        for (Medicine m : medicines.values()) if (!m.isExpired() && m.getQuantity() > 0) list.add(m);
        return list;
    }

    public double getTotalStockValue() {
        double sum = 0;
        for (Medicine m : medicines.values()) sum += m.getPrice() * m.getQuantity();
        return sum;
    }

    // ===================== SALES / BILLING =====================

    public Sale createSale(String customer, List<SaleItem> cart) throws PharmacyException {
        if (cart == null || cart.isEmpty()) throw new InvalidInputException("The cart is empty. Add at least one medicine.");
        if (customer != null && customer.contains("|")) throw new InvalidInputException("Customer name cannot contain '|'.");
        String name = (customer == null || customer.trim().isEmpty()) ? "Walk-in Customer" : customer.trim();

        // 1) check EVERYTHING first, so a failed bill never changes stock
        for (SaleItem item : cart)
            findMedicine(item.getMedicineId()).checkSellable(item.getQuantity());

        // 2) then reduce stock
        for (SaleItem item : cart)
            findMedicine(item.getMedicineId()).reduceStock(item.getQuantity());

        List<String> billNos = new ArrayList<>();
        for (Sale s : sales) billNos.add(s.getBillNo());
        Sale sale = new Sale(nextId("B", billNos, 4), LocalDateTime.now(), name, new ArrayList<>(cart));
        sales.add(sale);
        save();
        return sale;
    }

    public List<Sale> getSales() { return new ArrayList<>(sales); }

    public double getTotalRevenue() {
        double sum = 0;
        for (Sale s : sales) sum += s.getTotal();
        return sum;
    }

    // ===================== FILE I/O =====================

    private void save() throws PharmacyException {
        try {
            Files.createDirectories(DIR);
            List<String> lines = new ArrayList<>();
            for (Supplier s : suppliers.values()) lines.add(s.toFileString());
            Files.write(SUPPLIER_FILE, lines, StandardCharsets.UTF_8);

            lines.clear();
            for (Medicine m : medicines.values()) lines.add(m.toFileString());
            Files.write(MEDICINE_FILE, lines, StandardCharsets.UTF_8);

            lines.clear();   // one line per sold item
            for (Sale s : sales)
                for (SaleItem i : s.getItems())
                    lines.add(String.join("|", s.getBillNo(), s.getDateTime().toString(), s.getCustomer(),
                            i.getMedicineId(), i.getMedicineName(),
                            String.valueOf(i.getQuantity()), String.valueOf(i.getUnitPrice())));
            Files.write(SALES_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PharmacyException("Could not save data to file: " + e.getMessage());
        }
    }

    private void load() throws IOException {
        for (String line : Files.readAllLines(SUPPLIER_FILE, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) continue;
            try { Supplier s = Supplier.fromFileString(line); suppliers.put(s.getId(), s); }
            catch (RuntimeException e) { System.err.println("Skipping bad supplier line: " + line); }
        }
        for (String line : Files.readAllLines(MEDICINE_FILE, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) continue;
            try { Medicine m = Medicine.fromFileString(line); medicines.put(m.getId(), m); }
            catch (RuntimeException e) { System.err.println("Skipping bad medicine line: " + line); }
        }
        if (Files.exists(SALES_FILE)) {
            Map<String, Sale> bills = new LinkedHashMap<>();
            for (String line : Files.readAllLines(SALES_FILE, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) continue;
                try {
                    String[] p = line.split("\\|", -1);
                    Sale sale = bills.get(p[0]);
                    if (sale == null) {
                        sale = new Sale(p[0], LocalDateTime.parse(p[1]), p[2], new ArrayList<>());
                        bills.put(p[0], sale);
                    }
                    sale.getItems().add(new SaleItem(p[3], p[4], Integer.parseInt(p[5]), Double.parseDouble(p[6])));
                } catch (RuntimeException e) { System.err.println("Skipping bad sales line: " + line); }
            }
            sales.addAll(bills.values());
        }
    }

    // ===================== HELPERS =====================

    /** Makes ids like S001, M012, B0005. */
    private String nextId(String prefix, Collection<String> existing, int width) {
        int max = 0;
        for (String id : existing) {
            try { max = Math.max(max, Integer.parseInt(id.substring(prefix.length()))); }
            catch (NumberFormatException ignored) { }
        }
        return String.format("%s%0" + width + "d", prefix, max + 1);
    }

    private void addSampleData() {
        LocalDate today = LocalDate.now();
        suppliers.put("S001", new Supplier("S001", "Sun Pharma Distributors", "9820011122", "orders@sunpharma.in", "Mumbai"));
        suppliers.put("S002", new Supplier("S002", "Cipla Wholesale", "9820033344", "sales@ciplawholesale.in", "Pune"));
        suppliers.put("S003", new Supplier("S003", "MedPlus Traders", "9820055566", "hello@medplus.in", "Thane"));

        Object[][] rows = {
            {"M001", "Paracetamol 500mg", Category.TABLET,    "S001",  2.50, 120, today.plusMonths(14)},
            {"M002", "Amoxicillin 250mg", Category.CAPSULE,   "S002",  8.00,  60, today.plusMonths(9)},
            {"M003", "Cetirizine 10mg",   Category.TABLET,    "S001",  3.00,   8, today.plusMonths(11)},
            {"M004", "Benadryl Cough Syrup", Category.SYRUP,  "S002", 95.00,  25, today.plusMonths(7)},
            {"M005", "Insulin Pen",       Category.INJECTION, "S003", 450.00,  5, today.plusMonths(4)},
            {"M006", "Betadine Ointment", Category.OINTMENT,  "S003", 62.00,  40, today.plusMonths(18)},
            {"M007", "Refresh Eye Drops", Category.DROPS,     "S002", 85.00,   0, today.plusMonths(6)},
            {"M008", "Azithromycin 500mg", Category.TABLET,  "S001", 24.00,  45, today.plusMonths(10)},
            {"M009", "Old Cough Syrup",   Category.SYRUP,     "S003", 70.00,  12, today.minusDays(20)},
            {"M010", "Vitamin C 500mg",   Category.TABLET,    "S001",  4.00,  90, today.plusMonths(20)},
        };
        for (Object[] r : rows)
            medicines.put((String) r[0], new Medicine((String) r[0], (String) r[1], (Category) r[2],
                    (String) r[3], (Double) r[4], (Integer) r[5], (LocalDate) r[6]));
    }
}

/** All input validation lives here. Every method throws InvalidInputException on bad data. */
class Validator {

    public static String text(String field, String value) throws InvalidInputException {
        if (value == null || value.trim().isEmpty())
            throw new InvalidInputException(field + " cannot be empty.");
        if (value.contains("|"))
            throw new InvalidInputException(field + " cannot contain the '|' character.");
        return value.trim();
    }

    public static String phone(String value) throws InvalidInputException {
        String p = text("Phone", value);
        if (!p.matches("\\d{10}"))
            throw new InvalidInputException("Phone must be exactly 10 digits.");
        return p;
    }

    public static String email(String value) throws InvalidInputException {
        String e = text("Email", value);
        if (!e.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"))
            throw new InvalidInputException("Please enter a valid email (e.g. name@example.com).");
        return e;
    }

    public static double price(String value) throws InvalidInputException {
        String s = text("Price", value);
        double d;
        try { d = Double.parseDouble(s); }
        catch (NumberFormatException ex) { throw new InvalidInputException("Price must be a number."); }
        if (Double.isNaN(d) || Double.isInfinite(d) || d <= 0)
            throw new InvalidInputException("Price must be greater than 0.");
        return d;
    }

    public static int quantity(String value) throws InvalidInputException {
        String s = text("Quantity", value);
        int q;
        try { q = Integer.parseInt(s); }
        catch (NumberFormatException ex) { throw new InvalidInputException("Quantity must be a whole number."); }
        if (q < 0) throw new InvalidInputException("Quantity cannot be negative.");
        return q;
    }

    public static LocalDate date(String value) throws InvalidInputException {
        String s = text("Expiry date", value);
        try { return LocalDate.parse(s); }
        catch (DateTimeParseException ex) {
            throw new InvalidInputException("Expiry date must be in yyyy-MM-dd format (e.g. 2027-12-31).");
        }
    }
}

/** Parent of all our custom (domain-specific) exceptions. */
class PharmacyException extends Exception {
    public PharmacyException(String message) { super(message); }
}

/** Thrown when the user types something invalid (empty, wrong format, negative...). */
class InvalidInputException extends PharmacyException {
    public InvalidInputException(String message) { super(message); }
}

/** Thrown when a record with the same name already exists. */
class DuplicateRecordException extends PharmacyException {
    public DuplicateRecordException(String message) { super(message); }
}

/** Thrown when a record cannot be found. */
class RecordNotFoundException extends PharmacyException {
    public RecordNotFoundException(String message) { super(message); }
}

/** Thrown when a sale needs more units than are in stock. */
class InsufficientStockException extends PharmacyException {
    public InsufficientStockException(String message) { super(message); }
}

/** Thrown when an expired medicine is sold or added. */
class ExpiredMedicineException extends PharmacyException {
    public ExpiredMedicineException(String message) { super(message); }
}
