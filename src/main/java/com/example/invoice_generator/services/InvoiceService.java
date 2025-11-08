package com.example.invoice_generator.services;

import com.example.invoice_generator.dto.Dealer;
import com.example.invoice_generator.dto.Invoice;
import com.example.invoice_generator.dto.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InvoiceService {

    private static final double TAX_PERCENTAGE = 10.0;
    private final PDFGenerator pdfGenerator;
    private final QRCodeGenerator qrCodeGenerator;
    private final Map<String, Dealer> dealerDatabase;
    private final Map<String, Vehicle> vehicleDatabase;

    public InvoiceService(PDFGenerator pdfGenerator, QRCodeGenerator qrCodeGenerator) {
        this.pdfGenerator = pdfGenerator;
        this.qrCodeGenerator = qrCodeGenerator;
        this.dealerDatabase = new ConcurrentHashMap<>();
        this.vehicleDatabase = new ConcurrentHashMap<>();
        initializeMockData();
    }

    public byte[] generateInvoice(String dealerId, String vehicleId, String customerName) {
        log.info("Generating invoice for dealer: {}, vehicle: {}, customer: {}",
                dealerId, vehicleId, customerName);

        Dealer dealer = getDealerById(dealerId);
        Vehicle vehicle = getVehicleById(vehicleId);

        String invoiceNumber = generateInvoiceNumber();
        String transactionId = generateTransactionId();
        LocalDateTime invoiceDate = LocalDateTime.now();

        double basePrice = vehicle.getPrice();
        double taxAmount = calculateTax(basePrice);
        double totalAmount = basePrice + taxAmount;

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .transactionId(transactionId)
                .invoiceDate(invoiceDate)
                .dealer(dealer)
                .vehicle(vehicle)
                .customerName(customerName)
                .basePrice(basePrice)
                .taxAmount(taxAmount)
                .taxPercentage(TAX_PERCENTAGE)
                .totalAmount(totalAmount)
                .build();

        byte[] qrCodeImage = qrCodeGenerator.generateQRCode(transactionId);

        return pdfGenerator.generatePDF(invoice, qrCodeImage);
    }

    private Dealer getDealerById(String dealerId) {
        Dealer dealer = dealerDatabase.get(dealerId);
        if (dealer == null) {
            throw new IllegalArgumentException("Dealer not found: " + dealerId);
        }
        return dealer;
    }

    private Vehicle getVehicleById(String vehicleId) {
        Vehicle vehicle = vehicleDatabase.get(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found: " + vehicleId);
        }
        return vehicle;
    }

    private double calculateTax(double basePrice) {
        return (basePrice * TAX_PERCENTAGE) / 100.0;
    }

    private String generateInvoiceNumber() {
        return String.format("INV-%s-%06d",
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")),
                (int) (Math.random() * 1000000));
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 18).toUpperCase();
    }

    private void initializeMockData() {
        // Initialize Dealers
        dealerDatabase.put("D001", Dealer.builder()
                .dealerId("D001")
                .dealerName("Premium Auto Sales")
                .address("123 Main Street")
                .city("Mumbai")
                .state("Maharashtra")
                .zipCode("400001")
                .phone("+91-22-1234-5678")
                .email("contact@premiumauto.com")
                .gstNumber("27AABCU9603R1ZX")
                .build());

        dealerDatabase.put("D002", Dealer.builder()
                .dealerId("D002")
                .dealerName("Elite Motors")
                .address("456 Park Avenue")
                .city("Delhi")
                .state("Delhi")
                .zipCode("110001")
                .phone("+91-11-8765-4321")
                .email("info@elitemotors.com")
                .gstNumber("07AABCU9603R1ZY")
                .build());

        // Initialize Vehicles
        vehicleDatabase.put("V001", Vehicle.builder()
                .vehicleId("V001")
                .make("Honda")
                .model("City")
                .year("2024")
                .color("Pearl White")
                .vin("1HGBH41JXMN109186")
                .price(1250000.00)
                .engineNumber("K15C-2401234")
                .chassisNumber("MA3FEB81S00443821")
                .build());

        vehicleDatabase.put("V002", Vehicle.builder()
                .vehicleId("V002")
                .make("Maruti Suzuki")
                .model("Swift")
                .year("2024")
                .color("Metallic Blue")
                .vin("MA3EJE81S00362514")
                .price(850000.00)
                .engineNumber("K12M-2402567")
                .chassisNumber("MA3EJE81S00362514")
                .build());

        vehicleDatabase.put("V003", Vehicle.builder()
                .vehicleId("V003")
                .make("Hyundai")
                .model("Creta")
                .year("2024")
                .color("Titan Grey")
                .vin("MALH11CNXM2012345")
                .price(1650000.00)
                .engineNumber("G4FJ-2403891")
                .chassisNumber("MALH11CNXM2012345")
                .build());

        vehicleDatabase.put("V004", Vehicle.builder()
                .vehicleId("V004")
                .make("Tata")
                .model("Nexon")
                .year("2024")
                .color("Flame Red")
                .vin("MAT621234K1H12345")
                .price(1150000.00)
                .engineNumber("REVOTRON-2404512")
                .chassisNumber("MAT621234K1H12345")
                .build());

        log.info("Mock data initialized - Dealers: {}, Vehicles: {}",
                dealerDatabase.size(), vehicleDatabase.size());
    }
}
