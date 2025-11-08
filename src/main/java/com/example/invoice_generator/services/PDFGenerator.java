package com.example.invoice_generator.services;

import com.example.invoice_generator.dto.Invoice;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class PDFGenerator {

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(240, 240, 240);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public byte[] generatePDF(Invoice invoice, byte[] qrCodeImage) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            addHeader(document, invoice);
            document.add(new Paragraph("\n"));

            addDealerAndCustomerInfo(document, invoice);
            document.add(new Paragraph("\n"));

            addVehicleDetails(document, invoice);
            document.add(new Paragraph("\n"));

            addPriceBreakdown(document, invoice);
            document.add(new Paragraph("\n"));

            addQRCodeSection(document, invoice, qrCodeImage);

            addFooter(document);

            document.close();
            log.info("PDF generated successfully for invoice: {}", invoice.getInvoiceNumber());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addHeader(Document document, Invoice invoice) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth();

        Cell titleCell = new Cell()
                .add(new Paragraph("VEHICLE SALES INVOICE")
                        .setFontSize(22)
                        .setBold()
                        .setFontColor(HEADER_COLOR))
                .add(new Paragraph("Original for Buyer")
                        .setFontSize(10)
                        .setFontColor(ColorConstants.GRAY))
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(0);

        Cell metaCell = new Cell()
                .add(new Paragraph("Invoice No: " + invoice.getInvoiceNumber())
                        .setFontSize(10).setBold())
                .add(new Paragraph("Date: " + invoice.getInvoiceDate().format(DATE_FORMATTER))
                        .setFontSize(10))
                .add(new Paragraph("Transaction ID: " + invoice.getTransactionId())
                        .setFontSize(9)
                        .setFontColor(ColorConstants.GRAY))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);

        headerTable.addCell(titleCell);
        headerTable.addCell(metaCell);

        document.add(headerTable);
        document.add(new Paragraph(" ").setMarginBottom(5));
    }

    private void addDealerAndCustomerInfo(Document document, Invoice invoice) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();

        Cell dealerCell = new Cell()
                .add(new Paragraph("Dealer Information")
                        .setFontSize(12)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(HEADER_COLOR)
                        .setPadding(5))
                .add(new Paragraph(invoice.getDealer().getDealerName())
                        .setFontSize(11).setBold().setMarginTop(10))
                .add(new Paragraph(invoice.getDealer().getAddress())
                        .setFontSize(9))
                .add(new Paragraph(invoice.getDealer().getCity() + ", " +
                        invoice.getDealer().getState() + " - " + invoice.getDealer().getZipCode())
                        .setFontSize(9))
                .add(new Paragraph("Phone: " + invoice.getDealer().getPhone())
                        .setFontSize(9))
                .add(new Paragraph("Email: " + invoice.getDealer().getEmail())
                        .setFontSize(9))
                .add(new Paragraph("GST No: " + invoice.getDealer().getGstNumber())
                        .setFontSize(9).setBold())
                .setPadding(10)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));

        Cell customerCell = new Cell()
                .add(new Paragraph("Bill To")
                        .setFontSize(12)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(HEADER_COLOR)
                        .setPadding(5))
                .add(new Paragraph(invoice.getCustomerName())
                        .setFontSize(11).setBold().setMarginTop(10))
                .add(new Paragraph("\n\n\n\n\n\n")
                        .setFontSize(9))
                .setPadding(10)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));

        infoTable.addCell(dealerCell);
        infoTable.addCell(customerCell);

        document.add(infoTable);
    }

    private void addVehicleDetails(Document document, Invoice invoice) {
        Paragraph sectionTitle = new Paragraph("Vehicle Details")
                .setFontSize(14)
                .setBold()
                .setFontColor(HEADER_COLOR);
        document.add(sectionTitle);

        Table vehicleTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));

        addVehicleRow(vehicleTable, "Vehicle ID", invoice.getVehicle().getVehicleId());
        addVehicleRow(vehicleTable, "Make & Model", invoice.getVehicle().getMake() + " " + invoice.getVehicle().getModel());
        addVehicleRow(vehicleTable, "Year", invoice.getVehicle().getYear());
        addVehicleRow(vehicleTable, "Color", invoice.getVehicle().getColor());
        addVehicleRow(vehicleTable, "VIN", invoice.getVehicle().getVin());
        addVehicleRow(vehicleTable, "Engine Number", invoice.getVehicle().getEngineNumber());
        addVehicleRow(vehicleTable, "Chassis Number", invoice.getVehicle().getChassisNumber());

        document.add(vehicleTable);
    }

    private void addVehicleRow(Table table, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFontSize(10).setBold())
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(8)
                .setBorder(Border.NO_BORDER));

        table.addCell(new Cell()
                .add(new Paragraph(value).setFontSize(10))
                .setPadding(8)
                .setBorder(Border.NO_BORDER));
    }

    private void addPriceBreakdown(Document document, Invoice invoice) {
        Paragraph sectionTitle = new Paragraph("Price Breakdown")
                .setFontSize(14)
                .setBold()
                .setFontColor(HEADER_COLOR);
        document.add(sectionTitle);

        Table priceTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth()
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));

        priceTable.addCell(createPriceCell("Tax (" + invoice.getTaxPercentage() + "%)", false));
        priceTable.addCell(createAmountCell(invoice.getTaxAmount(), false));

        priceTable.addCell(createPriceCell("Total Amount", true));
        priceTable.addCell(createAmountCell(invoice.getTotalAmount(), true));

        document.add(priceTable);
    }

    private Cell createPriceCell(String text, boolean isTotal) {
        Paragraph p = new Paragraph(text).setFontSize(isTotal ? 12 : 10);
        if (isTotal) {
            p.setBold().setFontColor(ColorConstants.WHITE);
        }

        return new Cell()
                .add(p)
                .setBackgroundColor(isTotal ? HEADER_COLOR : LIGHT_GRAY)
                .setPadding(10)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT);
    }

    private Cell createAmountCell(double amount, boolean isTotal) {
        Paragraph p = new Paragraph(String.format("₹ %.2f", amount))
                .setFontSize(isTotal ? 12 : 10);
        if (isTotal) {
            p.setBold().setFontColor(ColorConstants.WHITE);
        }

        return new Cell()
                .add(p)
                .setBackgroundColor(isTotal ? HEADER_COLOR : ColorConstants.WHITE)
                .setPadding(10)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
    }

    private void addQRCodeSection(Document document, Invoice invoice, byte[] qrCodeImage) {
        Table qrTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth()
                .setMarginTop(20);

        Cell textCell = new Cell()
                .add(new Paragraph("Transaction Verification")
                        .setFontSize(12).setBold())
                .add(new Paragraph("Scan the QR code to verify this transaction")
                        .setFontSize(9)
                        .setFontColor(ColorConstants.GRAY))
                .add(new Paragraph("Transaction ID: " + invoice.getTransactionId())
                        .setFontSize(9)
                        .setMarginTop(10))
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(20);

        try {
            Image qrImage = new Image(ImageDataFactory.create(qrCodeImage))
                    .setWidth(120)
                    .setHeight(120);

            Cell qrCell = new Cell()
                    .add(qrImage)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);

            qrTable.addCell(textCell);
            qrTable.addCell(qrCell);
            document.add(qrTable);
        } catch (Exception e) {
            log.error("Error adding QR code to PDF", e);
        }
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n"));

        Table footerTable = new Table(1)
                .useAllAvailableWidth()
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1, 0.5f));

        footerTable.addCell(new Cell()
                .add(new Paragraph("Terms & Conditions:")
                        .setFontSize(10).setBold())
                .add(new Paragraph("1. This invoice is computer generated and requires no signature.\n" +
                        "2. Payment is due within 7 days of invoice date.\n" +
                        "3. Vehicle delivery subject to full payment clearance.\n" +
                        "4. All disputes subject to local jurisdiction.")
                        .setFontSize(8)
                        .setFontColor(ColorConstants.DARK_GRAY))
                .setBorder(Border.NO_BORDER)
                .setPadding(10)
                .setBackgroundColor(LIGHT_GRAY));

        document.add(footerTable);

        Paragraph thankYou = new Paragraph("Thank you for your business!")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setItalic()
                .setFontColor(HEADER_COLOR);

        document.add(thankYou);
    }
}

