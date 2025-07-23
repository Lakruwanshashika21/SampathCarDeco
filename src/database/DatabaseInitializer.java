import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String createItemsTable = """
            CREATE TABLE IF NOT EXISTS items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT,
                price REAL NOT NULL,
                quantity INTEGER NOT NULL
            );
        """;

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                active INTEGER DEFAULT 1
            );
        """;

        String createCategoriesTable="""
                CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL
            );

                """;

    

        String AdminActivitesTable="""
                CREATE TABLE IF NOT EXISTS admin_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    action TEXT NOT NULL,
                    description TEXT,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                );

                """;

        String createProductTable="""
                CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id TEXT UNIQUE NOT NULL,
                name TEXT NOT NULL,
                category_id INTEGER NOT NULL,
                image_path TEXT,
                FOREIGN KEY (category_id) REFERENCES categories(id)
            );

                """;
        
            String createCustomerTable = """
                CREATE TABLE IF NOT EXISTS customers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT UNIQUE NOT NULL,
                    email TEXT,
                    address TEXT,
                    vehicle_number TEXT
                );
            """;
            


         // Table 1: inventory_logs
        String inventoryLogsTable = """
            CREATE TABLE IF NOT EXISTS inventory_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                item_id TEXT NOT NULL,
                category TEXT,
                item_name TEXT,
                quantity INTEGER,
                buy_price REAL,
                sell_price REAL,
                date TEXT
            );
        """;


        // Table 2: current_inventory
        String currentInventoryTable = """
            CREATE TABLE IF NOT EXISTS current_inventory (
                item_id TEXT PRIMARY KEY,
                category TEXT,
                item_name TEXT,
                quantity INTEGER,
                buy_price REAL,
                sell_price REAL
            );
        """;

        String createCustomersTable = """
            CREATE TABLE IF NOT EXISTS customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                phone TEXT UNIQUE NOT NULL,
                email TEXT,
                address TEXT,
                vehical_number TEXT
            );
        """;

        String createSalesTable = """
            CREATE TABLE IF NOT EXISTS sales (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_phone TEXT NOT NULL,
                total REAL,
                discount REAL,
                date TEXT
            );
        """;

        String createSalesItemsTable = """
            CREATE TABLE IF NOT EXISTS sales_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sale_id INTEGER,
                item_id TEXT,
                item_name TEXT,
                quantity INTEGER,
                price REAL,
                subtotal REAL,
                FOREIGN KEY (sale_id) REFERENCES sales(id)
            );
        """;

        String createBillItemTable="""
                CREATE TABLE bill_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    bill_id INTEGER NOT NULL,
                    product_id TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    price REAL NOT NULL,
                    FOREIGN KEY (bill_id) REFERENCES bills(id),
                    FOREIGN KEY (product_id) REFERENCES products(product_id)
                );

                """;

        String createBillTable = """
            CREATE TABLE bills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                total_amount REAL NOT NULL,
                discount REAL DEFAULT 0,
                FOREIGN KEY (customer_id) REFERENCES customers(id)
                );

        """;


        String createAppointmentsTable="""
                CREATE TABLE appointments (
                appointment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_name TEXT,
                service TEXT,
                preferred_time TEXT,
                notes TEXT,
                status TEXT, -- Pending, Confirmed, Cancelled
                created_at TEXT
            );
                """;
        
        String createJobTable="""
                CREATE TABLE jobs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_name TEXT NOT NULL,
                mobile TEXT NOT NULL,
                vehicle_number TEXT,
                job_type TEXT NOT NULL,
                time TEXT,
                note TEXT,
                item TEXT,
                quantity INTEGER DEFAULT 0,
                fee REAL DEFAULT 0.0,
                status TEXT DEFAULT 'Pending',
                date TEXT,
                time_closed TEXT
            );


                """;

        String createJobBillTable="""
                CREATE TABLE jobBills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                job_id INTEGER NOT NULL,
                customer TEXT NOT NULL,
                phone TEXT NOT NULL,
                items TEXT,
                quantity INTEGER DEFAULT 0,
                job_fee REAL DEFAULT 0.0,
                total REAL DEFAULT 0.0,
                bill_time TEXT,
                FOREIGN KEY (job_id) REFERENCES jobs(id)
            );

                """;

        String createJob_itemTable="""
                CREATE TABLE IF NOT EXISTS job_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                job_id INTEGER,
                item_id TEXT,
                quantity INTEGER,
                price REAL,
                total REAL,
                FOREIGN KEY(job_id) REFERENCES jobs(id)
            );

                """;

        String saveBillData="""
                CREATE TABLE bills (
                id TEXT PRIMARY KEY,
                type TEXT,
                customer TEXT,
                total REAL,
                date TEXT,
                file_path TEXT
            );

                """;




        String insertDefaultSuperAdmin = """
            INSERT OR IGNORE INTO users (email, password, role, active)
            VALUES ('lakruwanshashika21@gmail.com', '1234', 'superadmin', 1);
        """;

        String insertDefaultAdmin = """
            INSERT OR IGNORE INTO users (email, password, role, active)
            VALUES ('a@gmail.com', '1', 'admin', 1);
        """;

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createItemsTable);
            stmt.execute(createUsersTable);
            stmt.execute(createCategoriesTable);
            stmt.execute(createProductTable);
            stmt.execute(createCustomerTable);
            stmt.execute(insertDefaultSuperAdmin);
            stmt.execute(insertDefaultAdmin);
            stmt.execute(inventoryLogsTable);
            stmt.execute(currentInventoryTable);
            stmt.execute(createCustomersTable);
            stmt.execute(createSalesTable);
            stmt.execute(createSalesItemsTable);
            stmt.execute(createBillTable);
            stmt.execute(createBillItemTable);
            stmt.execute(createAppointmentsTable);
            stmt.execute(createJobTable);
            stmt.execute(createJobBillTable);
            stmt.execute(createJob_itemTable);
            stmt.execute(AdminActivitesTable);
            stmt.execute(saveBillData);

            System.out.println("Tables initialized and default user added.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
