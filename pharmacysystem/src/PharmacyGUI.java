import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** The main window: dark sidebar + page area. */
class PharmacyGUI extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, Refreshable> pages = new LinkedHashMap<>();

    public PharmacyGUI(PharmacySystem store) {
        super("MediCare Pharmacy - Inventory & Billing System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1400, screen.width - 40), Math.min(820, screen.height - 60));
        setMinimumSize(new Dimension(1100, 660));
        setLocationRelativeTo(null);

        content.setBackground(UI.BG);
        addPage("Dashboard", new DashboardPanel(store));
        addPage("Medicines", new MedicinePanel(store));
        addPage("Suppliers", new SupplierPanel(store));
        addPage("Billing", new BillingPanel(store));
        addPage("Reports", new ReportPanel(store));

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        show("Dashboard");
    }

    private <T extends JPanel & Refreshable> void addPage(String name, T page) {
        pages.put(name, page);
        content.add(page, name);
    }

    private void show(String name) {
        pages.get(name).refresh();
        cards.show(content, name);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(UI.PRIMARY_DARK);
        side.setPreferredSize(new Dimension(230, 0));
        side.setBorder(new EmptyBorder(26, 14, 20, 14));

        JPanel names = new JPanel();
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.setOpaque(false);
        names.add(UI.label("MediCare", UI.FONT.deriveFont(Font.BOLD, 21f), Color.WHITE));
        names.add(UI.label("Pharmacy Manager", UI.SMALL, new Color(0x99F6E4)));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(new JLabel(new UI.LogoIcon(44)), BorderLayout.WEST);
        brand.add(names, BorderLayout.CENTER);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        side.add(brand);
        side.add(Box.createVerticalStrut(34));

        ButtonGroup group = new ButtonGroup();
        boolean first = true;
        for (String name : pages.keySet()) {
            UI.NavButton b = new UI.NavButton(name);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.addActionListener(e -> show(name));
            group.add(b);
            side.add(b);
            side.add(Box.createVerticalStrut(6));
            if (first) { b.setSelected(true); first = false; }
        }
        side.add(Box.createVerticalGlue());
        side.add(UI.label("Java Major Project", UI.SMALL_BOLD, new Color(0x99F6E4)));
        side.add(UI.label("B.Tech CSE 2025-29  |  Sem III", UI.SMALL, new Color(0x5EEAD4)));
        return side;
    }
}

/** Look & feel in one place: colours, fonts and small reusable styled components. */
class UI {
    public static final String RUPEE = "\u20B9";

    // ---- colours ----
    public static final Color PRIMARY = new Color(0x0F766E);
    public static final Color PRIMARY_DARK = new Color(0x134E4A);
    public static final Color PRIMARY_LIGHT = new Color(0xCCFBF1);
    public static final Color BG = new Color(0xF1F5F9);
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color WARN = new Color(0xD97706);
    public static final Color OK = new Color(0x16A34A);
    public static final Color INFO = new Color(0x2563EB);

    // ---- fonts ----
    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD = FONT.deriveFont(Font.BOLD);
    public static final Font SMALL = FONT.deriveFont(Font.PLAIN, 12f);
    public static final Font SMALL_BOLD = FONT.deriveFont(Font.BOLD, 12f);
    public static final Font TITLE = FONT.deriveFont(Font.BOLD, 26f);
    public static final Font CELL = FONT.deriveFont(Font.PLAIN, 13f);
    public static final Font BIG = FONT.deriveFont(Font.BOLD, 30f);

    public static void install() {
        for (Object key : Collections.list(UIManager.getDefaults().keys()))
            if (UIManager.get(key) instanceof FontUIResource)
                UIManager.put(key, new FontUIResource(FONT));
    }

    // ---- small helpers ----
    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JPanel pageHeader(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(label(title, TITLE, TEXT));
        p.add(Box.createVerticalStrut(2));
        p.add(label(subtitle, FONT, MUTED));
        return p;
    }

    public static JTextField field() {
        JTextField f = new JTextField();
        f.setFont(FONT);
        f.setPreferredSize(new Dimension(200, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(4, 10, 4, 10)));
        return f;
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(FONT);
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(200, 36));
    }

    /** A label on top of an input, used to build forms. */
    public static JPanel formRow(String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(label(label, SMALL_BOLD, MUTED), BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    public static JButton button(String text, Color color) { return new RoundedButton(text, color); }
    public static RoundedPanel card() { return new RoundedPanel(); }

    // ---- tables ----
    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return getRowCount() > 0 && getValueAt(0, c) != null ? getValueAt(0, c).getClass() : Object.class;
            }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(FONT);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setSelectionBackground(PRIMARY_LIGHT);
        t.setSelectionForeground(TEXT);
        CellRenderer r = new CellRenderer();
        t.setDefaultRenderer(Object.class, r);
        t.setDefaultRenderer(Number.class, r);
        t.setDefaultRenderer(Integer.class, r);
        t.setDefaultRenderer(Double.class, r);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int row, int col) {
                super.getTableCellRendererComponent(tb, v, s, f, row, col);
                setBackground(PRIMARY);
                setForeground(Color.WHITE);
                setFont(CELL.deriveFont(Font.BOLD));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                setHorizontalAlignment(LEFT);
                return this;
            }
        });
        return t;
    }

    /** Set preferred column widths, e.g. widths(table, 60, 200, 100). */
    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            if (w[i] <= 110) t.getColumnModel().getColumn(i).setMinWidth(w[i]);   // small columns never shrink
        }
    }

    public static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    public static Color statusColor(StockStatus s) {
        switch (s) {
            case IN_STOCK:  return OK;
            case LOW_STOCK: return WARN;
            default:        return DANGER;   // OUT_OF_STOCK, EXPIRED
        }
    }

    // ---- message boxes ----
    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Oops", JOptionPane.ERROR_MESSAGE);
    }
    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Done", JOptionPane.INFORMATION_MESSAGE);
    }
    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // =====================  styled components  =====================

    /** Table cell look: zebra rows, padding, currency for Doubles, coloured status text. */
    static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            String text = v == null ? "" : (v instanceof Double ? RUPEE + " " + String.format("%,.2f", (Double) v) : v.toString());
            super.getTableCellRendererComponent(t, text, sel, false, row, col);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setFont(CELL);
            setForeground(TEXT);
            setBackground(sel ? PRIMARY_LIGHT : (row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC)));
            setHorizontalAlignment(v instanceof Number ? RIGHT : LEFT);
            if (v instanceof StockStatus) {
                setFont(CELL.deriveFont(Font.BOLD));
                setForeground(statusColor((StockStatus) v));
            }
            return this;
        }
    }

    /** Rounded, hover-aware button. */
    static class RoundedButton extends JButton {
        private final Color base;
        private boolean hover;

        RoundedButton(String text, Color base) {
            super(text);
            this.base = base;
            setFont(BOLD);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(9, 18, 9, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(!isEnabled() ? Color.LIGHT_GRAY : (hover ? base.darker() : base));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** White rounded "card" with a soft shadow. */
    static class RoundedPanel extends JPanel {
        RoundedPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(15, 23, 42, 14));
            g2.fillRoundRect(1, 3, w - 2, h - 3, 18, 18);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 2, h - 3, 18, 18);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, w - 3, h - 4, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Sidebar button. */
    static class NavButton extends JToggleButton {
        private boolean hover;

        NavButton(String text) {
            super(text);
            setFont(BOLD);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(LEFT);
            setBorder(new EmptyBorder(12, 22, 12, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected()) {
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            g2.dispose();
            setForeground(isSelected() ? Color.WHITE : new Color(0x99F6E4));
            super.paintComponent(g);
        }
    }

    /** Round logo with a plus sign. */
    static class LogoIcon implements Icon {
        private final int size;
        LogoIcon(int size) { this.size = size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x2DD4BF));
            g2.fillOval(x, y, size, size);
            g2.setColor(Color.WHITE);
            int t = size / 6, len = size / 2, cx = x + size / 2, cy = y + size / 2;
            g2.fillRoundRect(cx - len / 2, cy - t / 2, len, t, 4, 4);
            g2.fillRoundRect(cx - t / 2, cy - len / 2, t, len, 4, 4);
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }
}

/** Every page implements this so it can reload its data when shown. */
interface Refreshable {
    void refresh();
}

/** Home page: key numbers + low-stock and expired alerts. */
class DashboardPanel extends JPanel implements Refreshable {
    private final PharmacySystem store;
    private final JLabel medicinesL = big(UI.PRIMARY), suppliersL = big(UI.INFO), lowL = big(UI.WARN),
                         expiredL = big(UI.DANGER), revenueL = big(UI.OK);
    private final DefaultTableModel lowModel = UI.model("Medicine", "Stock", "Status");
    private final DefaultTableModel expModel = UI.model("Medicine", "Expired On", "Qty");

    public DashboardPanel(PharmacySystem store) {
        this.store = store;
        setLayout(new BorderLayout(0, 20));
        setBackground(UI.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(UI.pageHeader("Dashboard", "Your pharmacy at a glance"), BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 5, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard("MEDICINES", medicinesL));
        stats.add(statCard("SUPPLIERS", suppliersL));
        stats.add(statCard("LOW / OUT OF STOCK", lowL));
        stats.add(statCard("EXPIRED", expiredL));
        stats.add(statCard("TOTAL REVENUE", revenueL));

        JPanel tables = new JPanel(new GridLayout(1, 2, 18, 0));
        tables.setOpaque(false);
        tables.add(tableCard("Low stock alerts", UI.WARN, UI.table(lowModel)));
        tables.add(tableCard("Expired medicines", UI.DANGER, UI.table(expModel)));

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(stats, BorderLayout.NORTH);
        body.add(tables, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        refresh();
    }

    private static JLabel big(Color c) {
        return UI.label("0", UI.BIG, c);
    }

    private JPanel statCard(String title, JLabel value) {
        UI.RoundedPanel p = UI.card();
        p.setLayout(new BorderLayout(0, 6));
        p.setBorder(new EmptyBorder(16, 20, 18, 20));
        p.add(UI.label(title, UI.SMALL_BOLD, UI.MUTED), BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JPanel tableCard(String title, Color color, JTable table) {
        UI.RoundedPanel p = UI.card();
        p.setLayout(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(18, 18, 20, 18));
        p.add(UI.label(title, UI.BOLD.deriveFont(16f), color), BorderLayout.NORTH);
        p.add(UI.scroll(table), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void refresh() {
        medicinesL.setText(String.valueOf(store.getMedicines().size()));
        suppliersL.setText(String.valueOf(store.getSuppliers().size()));
        lowL.setText(String.valueOf(store.getLowStock().size()));
        expiredL.setText(String.valueOf(store.getExpired().size()));
        revenueL.setText(UI.RUPEE + String.format("%,.0f", store.getTotalRevenue()));

        lowModel.setRowCount(0);
        for (Medicine m : store.getLowStock())
            lowModel.addRow(new Object[]{m.getName(), m.getQuantity(), m.getStatus()});
        expModel.setRowCount(0);
        for (Medicine m : store.getExpired())
            expModel.addRow(new Object[]{m.getName(), m.getExpiryDate().toString(), m.getQuantity()});
    }
}

/** Add / update / delete / search medicines. Status is shown automatically. */
class MedicinePanel extends JPanel implements Refreshable {
    private final PharmacySystem store;
    private final JTextField searchF = UI.field(), nameF = UI.field(), priceF = UI.field(),
                             qtyF = UI.field(), expF = UI.field();
    private final JComboBox<Object> statusBox = new JComboBox<>();
    private final JComboBox<Category> catBox = new JComboBox<>(Category.values());
    private final JComboBox<Supplier> supBox = new JComboBox<>();
    private final JLabel countL = UI.label(" ", UI.SMALL, UI.MUTED);
    private final DefaultTableModel model =
            UI.model("ID", "Name", "Category", "Supplier", "Price", "Qty", "Expiry", "Status");
    private final JTable table = UI.table(model);
    private String selectedId = null;

    public MedicinePanel(PharmacySystem store) {
        this.store = store;
        setLayout(new BorderLayout(18, 18));
        setBackground(UI.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(UI.pageHeader("Medicines", "Manage stock, prices and expiry dates"), BorderLayout.NORTH);

        UI.styleCombo(statusBox); UI.styleCombo(catBox); UI.styleCombo(supBox);
        statusBox.addItem("All statuses");
        for (StockStatus s : StockStatus.values()) statusBox.addItem(s);
        statusBox.setPreferredSize(new Dimension(170, 36));

        // ---- table card ----
        UI.RoundedPanel tableCard = UI.card();
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(16, 16, 14, 16));
        JPanel search = new JPanel(new BorderLayout(10, 0));
        search.setOpaque(false);
        search.add(UI.label("Search", UI.BOLD, UI.MUTED), BorderLayout.WEST);
        search.add(searchF, BorderLayout.CENTER);
        search.add(statusBox, BorderLayout.EAST);
        tableCard.add(search, BorderLayout.NORTH);
        tableCard.add(UI.scroll(table), BorderLayout.CENTER);
        tableCard.add(countL, BorderLayout.SOUTH);
        UI.widths(table, 62, 200, 90, 170, 88, 48, 100, 112);
        add(tableCard, BorderLayout.CENTER);

        // ---- form card ----
        UI.RoundedPanel form = UI.card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setPreferredSize(new Dimension(285, 0));
        form.add(UI.label("Medicine details", UI.BOLD.deriveFont(16f), UI.TEXT));
        form.add(Box.createVerticalStrut(14));
        form.add(UI.formRow("Medicine name", nameF));
        form.add(UI.formRow("Category", catBox));
        form.add(UI.formRow("Supplier", supBox));
        form.add(UI.formRow("Price (" + UI.RUPEE + ")", priceF));
        form.add(UI.formRow("Quantity", qtyF));
        form.add(UI.formRow("Expiry date (yyyy-MM-dd)", expF));
        form.add(buttons());
        form.add(Box.createVerticalGlue());
        add(form, BorderLayout.EAST);

        searchF.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        statusBox.addActionListener(e -> refresh());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) return;
            selectedId = (String) model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
            fillForm(store.getMedicine(selectedId));
        });
        refresh();
    }

    private JPanel buttons() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton add = UI.button("Add", UI.PRIMARY), update = UI.button("Update", UI.INFO),
                delete = UI.button("Delete", UI.DANGER), clear = UI.button("Clear", UI.MUTED);
        add.addActionListener(e -> add());
        update.addActionListener(e -> update());
        delete.addActionListener(e -> delete());
        clear.addActionListener(e -> clearForm());
        p.add(add); p.add(update); p.add(delete); p.add(clear);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        return p;
    }

    private void fillForm(Medicine m) {
        if (m == null) return;
        nameF.setText(m.getName());
        catBox.setSelectedItem(m.getCategory());
        for (int i = 0; i < supBox.getItemCount(); i++)
            if (supBox.getItemAt(i).getId().equals(m.getSupplierId())) supBox.setSelectedIndex(i);
        priceF.setText(String.valueOf(m.getPrice()));
        qtyF.setText(String.valueOf(m.getQuantity()));
        expF.setText(m.getExpiryDate().toString());
    }

    private void add() {
        try {
            store.addMedicine(nameF.getText(), (Category) catBox.getSelectedItem(), (Supplier) supBox.getSelectedItem(),
                    priceF.getText(), qtyF.getText(), expF.getText());
            UI.info(this, "Medicine added successfully.");
            clearForm(); refresh();
        } catch (PharmacyException ex) { UI.error(this, ex.getMessage()); }
    }

    private void update() {
        if (selectedId == null) { UI.error(this, "Click a medicine in the table first."); return; }
        try {
            store.updateMedicine(selectedId, nameF.getText(), (Category) catBox.getSelectedItem(),
                    (Supplier) supBox.getSelectedItem(), priceF.getText(), qtyF.getText(), expF.getText());
            UI.info(this, "Medicine updated.");
            clearForm(); refresh();
        } catch (PharmacyException ex) { UI.error(this, ex.getMessage()); }
    }

    private void delete() {
        if (selectedId == null) { UI.error(this, "Click a medicine in the table first."); return; }
        if (!UI.confirm(this, "Delete this medicine?")) return;
        try {
            store.deleteMedicine(selectedId);
            clearForm(); refresh();
        } catch (PharmacyException ex) { UI.error(this, ex.getMessage()); }
    }

    private void clearForm() {
        selectedId = null;
        nameF.setText(""); priceF.setText(""); qtyF.setText(""); expF.setText("");
        catBox.setSelectedIndex(0);
        table.clearSelection();
    }

    @Override
    public void refresh() {
        // reload supplier list (keep current choice)
        Supplier keep = (Supplier) supBox.getSelectedItem();
        supBox.removeAllItems();
        for (Supplier s : store.getSuppliers()) supBox.addItem(s);
        if (keep != null)
            for (int i = 0; i < supBox.getItemCount(); i++)
                if (supBox.getItemAt(i).getId().equals(keep.getId())) supBox.setSelectedIndex(i);

        Object chosen = statusBox.getSelectedItem();
        StockStatus filter = chosen instanceof StockStatus ? (StockStatus) chosen : null;

        model.setRowCount(0);
        for (Medicine m : store.searchMedicines(searchF.getText(), filter))
            model.addRow(new Object[]{m.getId(), m.getName(), m.getCategory().toString(),
                    store.getSupplierName(m.getSupplierId()), m.getPrice(), m.getQuantity(),
                    m.getExpiryDate().toString(), m.getStatus()});
        countL.setText("Showing " + model.getRowCount() + " medicine(s)  |  Low stock = " + Medicine.LOW_STOCK_LIMIT + " units or fewer");
    }
}

/** Add / update / delete / search suppliers. */
class SupplierPanel extends JPanel implements Refreshable {
    private final PharmacySystem store;
    private final JTextField searchF = UI.field(), nameF = UI.field(), phoneF = UI.field(),
                             emailF = UI.field(), cityF = UI.field();
    private final DefaultTableModel model = UI.model("ID", "Name", "Phone", "Email", "City");
    private final JTable table = UI.table(model);
    private String selectedId = null;

    public SupplierPanel(PharmacySystem store) {
        this.store = store;
        setLayout(new BorderLayout(18, 18));
        setBackground(UI.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(UI.pageHeader("Suppliers", "Companies that supply your medicines"), BorderLayout.NORTH);

        // ---- table card ----
        UI.RoundedPanel tableCard = UI.card();
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(16, 16, 18, 16));
        JPanel search = new JPanel(new BorderLayout(10, 0));
        search.setOpaque(false);
        search.add(UI.label("Search", UI.BOLD, UI.MUTED), BorderLayout.WEST);
        search.add(searchF, BorderLayout.CENTER);
        tableCard.add(search, BorderLayout.NORTH);
        tableCard.add(UI.scroll(table), BorderLayout.CENTER);
        UI.widths(table, 68, 200, 110, 230, 100);
        add(tableCard, BorderLayout.CENTER);

        // ---- form card ----
        UI.RoundedPanel form = UI.card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setPreferredSize(new Dimension(310, 0));
        form.add(UI.label("Supplier details", UI.BOLD.deriveFont(16f), UI.TEXT));
        form.add(Box.createVerticalStrut(14));
        form.add(UI.formRow("Supplier name", nameF));
        form.add(UI.formRow("Phone (10 digits)", phoneF));
        form.add(UI.formRow("Email", emailF));
        form.add(UI.formRow("City", cityF));
        form.add(buttons());
        form.add(Box.createVerticalGlue());
        add(form, BorderLayout.EAST);

        // live search using a row filter
        searchF.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        // click a row -> fill the form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) return;
            selectedId = (String) model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
            Supplier s = store.getSupplier(selectedId);
            if (s == null) return;
            nameF.setText(s.getName()); phoneF.setText(s.getPhone());
            emailF.setText(s.getEmail()); cityF.setText(s.getCity());
        });
        refresh();
    }

    private JPanel buttons() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton add = UI.button("Add", UI.PRIMARY), update = UI.button("Update", UI.INFO),
                delete = UI.button("Delete", UI.DANGER), clear = UI.button("Clear", UI.MUTED);
        add.addActionListener(e -> add());
        update.addActionListener(e -> update());
        delete.addActionListener(e -> delete());
        clear.addActionListener(e -> clearForm());
        p.add(add); p.add(update); p.add(delete); p.add(clear);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        return p;
    }

    private void filter() {
        @SuppressWarnings("unchecked")
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
        String k = searchF.getText().trim();
        sorter.setRowFilter(k.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(k)));
    }

    private void add() {
        try {
            store.addSupplier(nameF.getText(), phoneF.getText(), emailF.getText(), cityF.getText());
            UI.info(this, "Supplier added successfully.");
            clearForm(); refresh();
        } catch (PharmacyException ex) { UI.error(this, ex.getMessage()); }
    }

    private void update() {
        if (selectedId == null) { UI.error(this, "Click a supplier in the table first."); return; }
        try {
            store.updateSupplier(selectedId, nameF.getText(), phoneF.getText(), emailF.getText(), cityF.getText());
            UI.info(this, "Supplier updated.");
            clearForm(); refresh();
        } catch (PharmacyException ex) { UI.error(this, ex.getMessage()); }
    }

    private void delete() {
        if (selectedId == null) { UI.error(this, "Click a supplier in the table first."); return; }
        if (!UI.confirm(this, "Delete this supplier?")) return;
        try {
            store.deleteSupplier(selectedId);
            clearForm(); refresh();
        } catch (PharmacyException ex) { UI.error(this, ex.getMessage()); }
    }

    private void clearForm() {
        selectedId = null;
        nameF.setText(""); phoneF.setText(""); emailF.setText(""); cityF.setText("");
        table.clearSelection();
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        for (Supplier s : store.getSuppliers())
            model.addRow(new Object[]{s.getId(), s.getName(), s.getPhone(), s.getEmail(), s.getCity()});
    }
}

/** Sales & billing: build a cart, check business rules, print a bill, reduce stock. */
class BillingPanel extends JPanel implements Refreshable {
    private final PharmacySystem store;
    private final List<SaleItem> cart = new ArrayList<>();          // the current cart (Collection)
    private final JTextField customerF = UI.field();
    private final JComboBox<Medicine> medBox = new JComboBox<>();
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final DefaultTableModel model = UI.model("Medicine", "Qty", "Unit Price", "Total");
    private final JTable table = UI.table(model);
    private final JLabel subL = UI.label(UI.RUPEE + " 0.00", UI.FONT, UI.TEXT),
                         taxL = UI.label(UI.RUPEE + " 0.00", UI.FONT, UI.TEXT),
                         totL = UI.label(UI.RUPEE + " 0.00", UI.BOLD.deriveFont(22f), UI.PRIMARY);

    public BillingPanel(PharmacySystem store) {
        this.store = store;
        setLayout(new BorderLayout(18, 18));
        setBackground(UI.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(UI.pageHeader("Billing", "Sell medicines and generate a bill"), BorderLayout.NORTH);

        UI.styleCombo(medBox);
        qtySpinner.setFont(UI.FONT);
        qtySpinner.setPreferredSize(new Dimension(200, 36));
        medBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, v, i, sel, foc);
                if (v instanceof Medicine) {
                    Medicine m = (Medicine) v;
                    setText(m.getName() + "  -  " + UI.RUPEE + String.format("%.2f", m.getPrice()) + "  (stock " + m.getQuantity() + ")");
                }
                return this;
            }
        });

        // ---- left: new item form ----
        UI.RoundedPanel form = UI.card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setPreferredSize(new Dimension(320, 0));
        form.add(UI.label("New bill", UI.BOLD.deriveFont(16f), UI.TEXT));
        form.add(Box.createVerticalStrut(14));
        form.add(UI.formRow("Customer name (optional)", customerF));
        form.add(UI.formRow("Medicine", medBox));
        form.add(UI.formRow("Quantity", qtySpinner));
        JButton addBtn = UI.button("Add to cart", UI.PRIMARY);
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        addBtn.addActionListener(e -> addToCart());
        form.add(addBtn);
        form.add(Box.createVerticalStrut(14));
        form.add(UI.label("<html>Expired medicines and out-of-stock<br>items are hidden from this list.</html>", UI.SMALL, UI.MUTED));
        form.add(Box.createVerticalGlue());
        add(form, BorderLayout.WEST);

        // ---- right: cart ----
        UI.RoundedPanel cartCard = UI.card();
        cartCard.setLayout(new BorderLayout(0, 14));
        cartCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        cartCard.add(UI.label("Cart", UI.BOLD.deriveFont(16f), UI.TEXT), BorderLayout.NORTH);
        cartCard.add(UI.scroll(table), BorderLayout.CENTER);

        JPanel summary = new JPanel(new GridLayout(3, 2, 0, 4));
        summary.setOpaque(false);
        summary.add(UI.label("Subtotal", UI.FONT, UI.MUTED)); summary.add(right(subL));
        summary.add(UI.label("GST (5%)", UI.FONT, UI.MUTED)); summary.add(right(taxL));
        summary.add(UI.label("Total", UI.BOLD.deriveFont(16f), UI.TEXT)); summary.add(right(totL));
        summary.setPreferredSize(new Dimension(280, 84));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        JButton remove = UI.button("Remove item", UI.WARN), clear = UI.button("Clear cart", UI.MUTED),
                bill = UI.button("Generate bill", UI.OK);
        remove.addActionListener(e -> removeSelected());
        clear.addActionListener(e -> { cart.clear(); refreshCart(); });
        bill.addActionListener(e -> generateBill());
        btns.add(remove); btns.add(clear); btns.add(bill);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(summary, BorderLayout.WEST);
        south.add(btns, BorderLayout.EAST);
        cartCard.add(south, BorderLayout.SOUTH);
        add(cartCard, BorderLayout.CENTER);
        refresh();
    }

    private JPanel right(JLabel l) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.add(l);
        return p;
    }

    private void addToCart() {
        Medicine m = (Medicine) medBox.getSelectedItem();
        if (m == null) { UI.error(this, "No medicine is available for sale."); return; }
        int qty = (Integer) qtySpinner.getValue();

        SaleItem existing = null;                       // same medicine already in cart? merge it
        for (SaleItem i : cart) if (i.getMedicineId().equals(m.getId())) existing = i;
        int newQty = qty + (existing == null ? 0 : existing.getQuantity());
        try {
            m.checkSellable(newQty);                    // business rules: not expired + enough stock
        } catch (PharmacyException ex) { UI.error(this, ex.getMessage()); return; }

        if (existing == null) cart.add(new SaleItem(m.getId(), m.getName(), qty, m.getPrice()));
        else existing.setQuantity(newQty);
        qtySpinner.setValue(1);
        refreshCart();
    }

    private void removeSelected() {
        int r = table.getSelectedRow();
        if (r < 0) { UI.error(this, "Select a cart row first."); return; }
        cart.remove(table.convertRowIndexToModel(r));
        refreshCart();
    }

    private void generateBill() {
        try {
            Sale sale = store.createSale(customerF.getText(), cart);
            cart.clear();
            customerF.setText("");
            refresh();
            showReceipt(sale);
        } catch (PharmacyException ex) { UI.error(this, ex.getMessage()); }
    }

    private void refreshCart() {
        model.setRowCount(0);
        double sub = 0;
        for (SaleItem i : cart) {
            model.addRow(new Object[]{i.getMedicineName(), i.getQuantity(), i.getUnitPrice(), i.getTotal()});
            sub += i.getTotal();
        }
        double tax = sub * Sale.GST_RATE;
        subL.setText(UI.RUPEE + String.format(" %,.2f", sub));
        taxL.setText(UI.RUPEE + String.format(" %,.2f", tax));
        totL.setText(UI.RUPEE + String.format(" %,.2f", sub + tax));
    }

    private void showReceipt(Sale sale) {
        JTextArea area = new JTextArea(sale.toReceipt());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        area.setBorder(new EmptyBorder(14, 16, 14, 16));

        JButton save = UI.button("Save receipt to file", UI.INFO);
        save.addActionListener(e -> {
            try {
                Path dir = Paths.get("data", "receipts");
                Files.createDirectories(dir);
                Path file = dir.resolve(sale.getBillNo() + ".txt");
                Files.write(file, sale.toReceipt().getBytes(StandardCharsets.UTF_8));
                UI.info(this, "Saved to " + file.toAbsolutePath());
            } catch (IOException ex) { UI.error(this, "Could not save receipt: " + ex.getMessage()); }
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(save);

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Bill " + sale.getBillNo(), Dialog.ModalityType.APPLICATION_MODAL);
        d.setLayout(new BorderLayout());
        d.add(new JScrollPane(area), BorderLayout.CENTER);
        d.add(bottom, BorderLayout.SOUTH);
        d.setSize(520, 500);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    @Override
    public void refresh() {
        Medicine keep = (Medicine) medBox.getSelectedItem();
        medBox.removeAllItems();
        for (Medicine m : store.getSellable()) medBox.addItem(m);
        if (keep != null)
            for (int i = 0; i < medBox.getItemCount(); i++)
                if (medBox.getItemAt(i).getId().equals(keep.getId())) medBox.setSelectedIndex(i);
        refreshCart();
    }
}

/** Inventory & sales reports, with export to CSV. */
class ReportPanel extends JPanel implements Refreshable {
    private static final String[] REPORTS = {"Full inventory", "Low stock items", "Expired items", "Sales history"};

    private final PharmacySystem store;
    private final JComboBox<String> typeBox = new JComboBox<>(REPORTS);
    private final JLabel summaryL = UI.label(" ", UI.BOLD, UI.PRIMARY);
    private final JTable table = UI.table(UI.model("-"));

    public ReportPanel(PharmacySystem store) {
        this.store = store;
        setLayout(new BorderLayout(0, 18));
        setBackground(UI.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(UI.pageHeader("Reports", "Inventory and sales summaries"), BorderLayout.NORTH);

        UI.styleCombo(typeBox);
        typeBox.setPreferredSize(new Dimension(220, 36));
        typeBox.addActionListener(e -> refresh());
        JButton export = UI.button("Export to CSV", UI.INFO);
        export.addActionListener(e -> exportCsv());

        UI.RoundedPanel card = UI.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(UI.label("Report", UI.BOLD, UI.MUTED));
        left.add(typeBox);
        top.add(left, BorderLayout.WEST);
        top.add(export, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        card.add(UI.scroll(table), BorderLayout.CENTER);
        card.add(summaryL, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        DefaultTableModel m;
        switch (typeBox.getSelectedIndex()) {
            case 1: {
                m = UI.model("ID", "Name", "Supplier", "Qty", "Status");
                List<Medicine> list = store.getLowStock();
                for (Medicine x : list)
                    m.addRow(new Object[]{x.getId(), x.getName(), store.getSupplierName(x.getSupplierId()), x.getQuantity(), x.getStatus()});
                summaryL.setText(list.size() + " item(s) need restocking");
                break;
            }
            case 2: {
                m = UI.model("ID", "Name", "Supplier", "Qty", "Expired On", "Value Lost");
                List<Medicine> list = store.getExpired();
                double loss = 0;
                for (Medicine x : list) {
                    double v = x.getPrice() * x.getQuantity();
                    loss += v;
                    m.addRow(new Object[]{x.getId(), x.getName(), store.getSupplierName(x.getSupplierId()),
                            x.getQuantity(), x.getExpiryDate().toString(), v});
                }
                summaryL.setText(list.size() + " expired item(s)  |  stock value lost: " + UI.RUPEE + String.format(" %,.2f", loss));
                break;
            }
            case 3: {
                m = UI.model("Bill No", "Date & Time", "Customer", "Units", "Total");
                List<Sale> list = store.getSales();
                Collections.reverse(list);   // newest first
                DateTimeFormatter f = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
                for (Sale s : list)
                    m.addRow(new Object[]{s.getBillNo(), s.getDateTime().format(f), s.getCustomer(), s.getTotalUnits(), s.getTotal()});
                summaryL.setText(list.size() + " bill(s)  |  total revenue: " + UI.RUPEE + String.format(" %,.2f", store.getTotalRevenue()));
                break;
            }
            default: {
                m = UI.model("ID", "Name", "Category", "Supplier", "Price", "Qty", "Expiry", "Status", "Stock Value");
                for (Medicine x : store.getMedicines())
                    m.addRow(new Object[]{x.getId(), x.getName(), x.getCategory().toString(),
                            store.getSupplierName(x.getSupplierId()), x.getPrice(), x.getQuantity(),
                            x.getExpiryDate().toString(), x.getStatus(), x.getPrice() * x.getQuantity()});
                summaryL.setText(store.getMedicines().size() + " medicine(s)  |  total stock value: "
                        + UI.RUPEE + String.format(" %,.2f", store.getTotalStockValue()));
            }
        }
        table.setModel(m);
        switch (typeBox.getSelectedIndex()) {
            case 0: UI.widths(table, 62, 230, 90, 200, 90, 55, 100, 112, 120); break;
            case 1: UI.widths(table, 62, 240, 220, 60, 112); break;
            case 2: UI.widths(table, 62, 240, 220, 60, 100, 110); break;
            default: UI.widths(table, 90, 210, 220, 60, 110);
        }
    }

    private void exportCsv() {
        if (table.getRowCount() == 0) { UI.error(this, "There is nothing to export."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(typeBox.getSelectedItem().toString().replace(' ', '_') + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter w = new PrintWriter(fc.getSelectedFile(), "UTF-8")) {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            for (int c = 0; c < m.getColumnCount(); c++) w.print((c > 0 ? "," : "") + csv(m.getColumnName(c)));
            w.println();
            for (int r = 0; r < m.getRowCount(); r++) {
                for (int c = 0; c < m.getColumnCount(); c++) w.print((c > 0 ? "," : "") + csv(String.valueOf(m.getValueAt(r, c))));
                w.println();
            }
            UI.info(this, "Report exported to " + fc.getSelectedFile().getAbsolutePath());
        } catch (IOException ex) { UI.error(this, "Could not export: " + ex.getMessage()); }
    }

    private String csv(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }
}
