import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** A complete bill. */
public class Sale {
    public static final double GST_RATE = 0.05;   // 5% GST

    private final String billNo, customer;
    private final LocalDateTime dateTime;
    private final List<SaleItem> items;

    public Sale(String billNo, LocalDateTime dateTime, String customer, List<SaleItem> items) {
        this.billNo = billNo; this.dateTime = dateTime; this.customer = customer; this.items = items;
    }

    public String getBillNo() { return billNo; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getCustomer() { return customer; }
    public List<SaleItem> getItems() { return items; }

    public double getSubtotal() {
        double sum = 0;
        for (SaleItem i : items) sum += i.getTotal();
        return sum;
    }
    public double getTax() { return getSubtotal() * GST_RATE; }
    public double getTotal() { return getSubtotal() + getTax(); }

    public int getTotalUnits() {
        int n = 0;
        for (SaleItem i : items) n += i.getQuantity();
        return n;
    }

    /** Printable receipt text. */
    public String toReceipt() {
        String line = "------------------------------------------------\n";
        StringBuilder sb = new StringBuilder();
        sb.append("              MEDICARE PHARMACY\n");
        sb.append("             Cash Bill / Invoice\n").append(line);
        sb.append(String.format("Bill No  : %s\n", billNo));
        sb.append(String.format("Date     : %s\n", dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"))));
        sb.append(String.format("Customer : %s\n", customer)).append(line);
        sb.append(String.format("%-22s %4s %9s %10s\n", "Item", "Qty", "Price", "Total")).append(line);
        for (SaleItem i : items) {
            String n = i.getMedicineName();
            if (n.length() > 22) n = n.substring(0, 21) + ".";
            sb.append(String.format("%-22s %4d %9.2f %10.2f\n", n, i.getQuantity(), i.getUnitPrice(), i.getTotal()));
        }
        sb.append(line);
        sb.append(String.format("%-37s %10.2f\n", "Subtotal", getSubtotal()));
        sb.append(String.format("%-37s %10.2f\n", "GST (5%)", getTax()));
        sb.append(String.format("%-37s %10.2f\n", "GRAND TOTAL (INR)", getTotal())).append(line);
        sb.append("        Thank you! Get well soon.\n");
        return sb.toString();
    }
}

/** One line of a bill (a medicine, how many, at what price). */
class SaleItem {
    private final String medicineId, medicineName;
    private int quantity;
    private final double unitPrice;

    public SaleItem(String medicineId, String medicineName, int quantity, double unitPrice) {
        this.medicineId = medicineId; this.medicineName = medicineName;
        this.quantity = quantity; this.unitPrice = unitPrice;
    }

    public String getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTotal() { return quantity * unitPrice; }
}
