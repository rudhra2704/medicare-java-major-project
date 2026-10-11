

public class Supplier {
    private final String id;
    private String name, phone, email, city;

    public Supplier(String id, String name, String phone, String email, String city) {
        this.id = id; this.name = name; this.phone = phone; this.email = email; this.city = city;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getCity() { return city; }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setCity(String city) { this.city = city; }

    /** One line in suppliers.txt */
    public String toFileString() { return String.join("|", id, name, phone, email, city); }

    public static Supplier fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        return new Supplier(p[0], p[1], p[2], p[3], p[4]);
    }

    @Override
    public String toString() { return name; }   // shown in combo boxes
}
