package com.example.invoice_generator.controller;



import com.example.invoice_generator.dto.InvoiceRequest;
import com.example.invoice_generator.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/invoice")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateInvoice(@Valid @RequestBody InvoiceRequest request) {
        try {
            log.info("Received invoice generation request - DealerID: {}, VehicleID: {}, Customer: {}",
                    request.getDealerId(), request.getVehicleId(), request.getCustomerName());

            byte[] pdfBytes = invoiceService.generateInvoice(
                    request.getDealerId(),
                    request.getVehicleId(),
                    request.getCustomerName()
            );

            String filename = String.format("Invoice_%s_%s.pdf",
                    request.getVehicleId(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            log.info("Invoice generated successfully - Size: {} bytes", pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error generating invoice", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Invoice Generator Service is running");
    }
}
