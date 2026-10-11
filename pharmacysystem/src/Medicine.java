import java.time.LocalDate;

public class Medicine {
    public static final int LOW_STOCK_LIMIT = 10;   // 10 or fewer units = low stock

    private final String id;
    private String name, supplierId;
    private Category category;
    private double price;
    private int quantity;
    private LocalDate expiryDate;

    public Medicine(String id, String name, Category category, String supplierId,
                    double price, int quantity, LocalDate expiryDate) {
        this.id = id; this.name = name; this.category = category; this.supplierId = supplierId;
        this.price = price; this.quantity = quantity; this.expiryDate = expiryDate;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public String getSupplierId() { return supplierId; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }

    public void setName(String name) { this.name = name; }
    public void setCategory(Category category) { this.category = category; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public boolean isExpired() { return expiryDate.isBefore(LocalDate.now()); }

    /** Status is calculated automatically - never stored. */
    public StockStatus getStatus() {
        if (isExpired()) return StockStatus.EXPIRED;
        if (quantity == 0) return StockStatus.OUT_OF_STOCK;
        if (quantity <= LOW_STOCK_LIMIT) return StockStatus.LOW_STOCK;
        return StockStatus.IN_STOCK;
    }

    /** Business rules for selling: not expired, and enough stock. */
    public void checkSellable(int qty) throws ExpiredMedicineException, InsufficientStockException {
        if (isExpired())
            throw new ExpiredMedicineException(name + " expired on " + expiryDate + " and cannot be sold.");
        if (qty > quantity)
            throw new InsufficientStockException("Only " + quantity + " unit(s) of " + name + " in stock.");
    }

    public void reduceStock(int qty) { quantity -= qty; }

    public String toFileString() {
        return String.join("|", id, name, category.name(), supplierId,
                String.valueOf(price), String.valueOf(quantity), expiryDate.toString());
    }

    public static Medicine fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        return new Medicine(p[0], p[1], Category.valueOf(p[2]), p[3],
                Double.parseDouble(p[4]), Integer.parseInt(p[5]), LocalDate.parse(p[6]));
    }

    @Override
    public String toString() { return name; }
}

/** ENUM: fixed medicine categories. */
enum Category {
    TABLET, CAPSULE, SYRUP, INJECTION, OINTMENT, DROPS;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}

/** ENUM: the status of a medicine (worked out automatically from stock + expiry). */
enum StockStatus {
    IN_STOCK("In Stock"), LOW_STOCK("Low Stock"), OUT_OF_STOCK("Out of Stock"), EXPIRED("Expired");

    private final String label;

    StockStatus(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
