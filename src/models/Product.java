package models;

public class Product {
    private int id;
    private String productId; // unique ID for barcode, etc.
    private String name;
    private int categoryId;
    private String categoryName; // for display convenience
    private String imagePath; // optional

    public Product(int id, String productId, String name, int categoryId, String categoryName, String imagePath) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getImagePath() { return imagePath; }

    @Override
    public String toString() {
        return name + " (" + productId + ")";
    }
}
