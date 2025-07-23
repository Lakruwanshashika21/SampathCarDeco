

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.JOptionPane;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

/**
 * Common billing service for both JobPanel and OrderPanel
 */
public class BillService {

    private static final Preferences prefs = Preferences.userRoot().node("OrderPanelPrefs");
    private static final String SAVE_PATH_KEY = "savePath";

    public static String getSavePath() {
        return prefs.get(SAVE_PATH_KEY, "Bill/Order Bill");
    }

    public static void setSavePath(String path) {
        prefs.put(SAVE_PATH_KEY, path);
    }

    /**
     * Generate, save, and print a bill
     *
     * @param billType   Either "ORDER" or "JOB"
     * @param customer   Customer name
     * @param items      2D array: {item name, quantity, price}
     * @param total      Total bill amount
     * @param discountToBill   Discount amount
     * @param saveFolder Folder to save PDF and barcode
     * @return Generated PDF File
     */
    public static File generateAndPrintBill(String billType, String customer, String[][] items, double total, double discountToBill, String saveFolder) {
        try {
            // Get default printer
            PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
            boolean isA4 = isA4Printer(defaultPrinter);

            // Generate unique bill ID
            String billId = billType + "_" + System.currentTimeMillis();
            String billFileName = billId + ".pdf";
            File billFile = new File(saveFolder, billFileName);

            // Create barcode image
            File barcodeFile = generateBarcodeImage(billId, saveFolder);

            // Generate PDF
            Document document = isA4 ? new Document(PageSize.A4) : new Document(new Rectangle(226, 600));
            PdfWriter.getInstance(document, new FileOutputStream(billFile));
            document.open();

            // ===== Header =====
            Paragraph header = new Paragraph("Sampath Car Deco\nNo.25, Main Street, Nittambuwa\n077-1234567\n\n",
                    new Font(Font.FontFamily.HELVETICA, isA4 ? 16 : 10, Font.BOLD));
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            // ===== Bill Info =====
            document.add(new Paragraph("Bill ID: " + billId));
            document.add(new Paragraph("Customer: " + customer));
            document.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            document.add(new Paragraph("\n"));

            // ===== Item Table =====
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{6, 2, 4});
            table.addCell("Item");
            table.addCell("Qty");
            table.addCell("Price");

            for (String[] row : items) {
                table.addCell(row[0]);
                table.addCell(row[1]);
                table.addCell(row[2]);
            }

            document.add(table);

            // ===== Total =====
            document.add(new Paragraph("\nTotal: Rs. " + total,
                    new Font(Font.FontFamily.HELVETICA, isA4 ? 14 : 10, Font.BOLD)));
            
            document.add(new Paragraph("\nDiscount: Rs. " + discountToBill,
                    new Font(Font.FontFamily.HELVETICA, isA4 ? 14 : 10, Font.BOLD)));
            
            document.add(new Paragraph("\nGrand Total: Rs. " + (total - discountToBill),
                    new Font(Font.FontFamily.HELVETICA, isA4 ? 14 : 10, Font.BOLD)));

            // ===== Barcode =====
            Image barcodeImg = Image.getInstance(barcodeFile.getAbsolutePath());
            barcodeImg.scaleToFit(150, 60);
            barcodeImg.setAlignment(Element.ALIGN_CENTER);
            document.add(barcodeImg);

            // ===== Footer =====
            Paragraph footer = new Paragraph("\nThank you! Come Again!\n",
                    new Font(Font.FontFamily.HELVETICA, isA4 ? 12 : 8));
            Paragraph StmDev = new Paragraph("System developed by: Shashika Piyomal \n +94771080809",
                new Font(Font.FontFamily.TIMES_ROMAN,6));
            footer.setAlignment(Element.ALIGN_CENTER);
            StmDev.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            document.add(StmDev);

            document.close();

            // ===== Auto Print =====
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().print(billFile);
            }

            return billFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void generateAndPrintBill(
        String billType,
        String customerName,
        String[][] items,
        String jobType,
        double jobFee,
        double total,
        String savePath,
        String email) {

    try {
        // Get default printer
        PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
        boolean isA4 = isA4Printer(defaultPrinter);

        File dir = new File(savePath);
        if (!dir.exists()) dir.mkdirs();

        String billId = billType + "_" + System.currentTimeMillis();
        File file = new File(dir, billId + ".pdf");

        // Generate PDF
            Document doc = isA4 ? new Document(PageSize.A4) : new Document(new Rectangle(226, 600));
            PdfWriter.getInstance(doc, new FileOutputStream(file));
        
            doc.open();

        // ===== Header =====
        Paragraph header = new Paragraph("Sampath Car Deco\nNo.25, Main Street, Nittambuwa\n077-1234567\n\n",
                new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
        header.setAlignment(Element.ALIGN_CENTER);
        doc.add(header);

        // ===== Bill Info =====
        doc.add(new Paragraph("Bill ID: " + billId));
        doc.add(new Paragraph("Bill Type: " + billType));
        doc.add(new Paragraph("Job Type: " + jobType));
        doc.add(new Paragraph("Customer: " + customerName));
        doc.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        doc.add(new Paragraph(" "));

        // ===== Table =====
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{6, 3, 4, 5});
        table.addCell("Item");
        table.addCell("Qty");
        table.addCell("Price");
        table.addCell("Total");

        double itemTotal = 0;
        for (String[] item : items) {
            String name = item[0];
            int qty = Integer.parseInt(item[1]);
            double price = Double.parseDouble(item[2]);
            double subtotal = qty * price;

            itemTotal += subtotal;

            table.addCell(name);
            table.addCell(String.valueOf(qty));
            table.addCell(String.format("%.2f", price));
            table.addCell(String.format("%.2f", subtotal));
        }
        doc.add(table);

        // ===== Summary =====
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Job Fee: Rs. " + String.format("%.2f", jobFee)));
        doc.add(new Paragraph("Items Total: Rs. " + String.format("%.2f", itemTotal)));
        doc.add(new Paragraph("Grand Total: Rs. " + String.format("%.2f", total),
                new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));

        // ===== Barcode =====
        File barcodeFile = generateBarcodeImage(billId, savePath);
        Image barcodeImg = Image.getInstance(barcodeFile.getAbsolutePath());
        barcodeImg.scaleToFit(150, 60);
        barcodeImg.setAlignment(Element.ALIGN_CENTER);
        doc.add(barcodeImg);

        // ===== Footer =====
        Paragraph footer = new Paragraph("\nThank you! Come Again!\n",
                new Font(Font.FontFamily.HELVETICA, 12));
        Paragraph StmDev = new Paragraph("System developed by: Shashika Piyomal \n +94771080809",
                new Font(Font.FontFamily.TIMES_ROMAN,6));
        footer.setAlignment(Element.ALIGN_CENTER);
        StmDev.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
        doc.add(StmDev);

        doc.close();

        // Auto open
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "❌ Failed to generate job bill.\n" + e.getMessage());
    }
}


    /**
     * Generate barcode image and save to file
     */
    private static File generateBarcodeImage(String data, String folder) throws IOException {
        Code128Bean bean = new Code128Bean();
        final int dpi = 150;
        bean.setModuleWidth(0.21); // barcode width

        File barcodeFile = new File(folder, data + "_barcode.png");
        OutputStream out = new FileOutputStream(barcodeFile);
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                out, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);

        bean.generateBarcode(canvas, data);
        canvas.finish();
        out.close();

        return barcodeFile;
    }

    /**
     * Detect if printer is likely an A4 printer
     */
    private static boolean isA4Printer(PrintService printer) {
        if (printer == null) return true; // default to A4 if unknown
        String name = printer.getName().toLowerCase();
        return name.contains("hp") || name.contains("canon") || name.contains("epson");
    }

    
}


