package com.example.FFsmartBackend.controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FFsmartBackend.models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final String inventoryFilePath = "src/main/resources/data/inventory.json";
    private final String deliveriesFilePath = "src/main/resources/data/deliveries.json";
    private final Path safetyReportPath = Paths.get("src/main/resources/reports/SafetyAndComplianceReport.pdf");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean userHasReportAccess(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole();
        return role.equals("HeadChef") || role.equals("Manager") || role.equals("HealthAndSafetyOfficer");
    }
     
    @GetMapping("/safety-compliance/pdf")
    public ResponseEntity<Resource> getSafetyComplianceReport() {
        try {
            Resource resource = new UrlResource(safetyReportPath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=SafetyAndComplianceReport.pdf")
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    /**
     * GET /api/reports/inventory/csv
     * Generate CSV for the inventory report.
     */
    @GetMapping("/inventory/csv")
    public ResponseEntity<?> exportInventoryCsv(Authentication authentication) {
        return generateCsvReport(authentication, inventoryFilePath, "inventory_report.csv");
    }

    /**
     * GET /api/reports/deliveries/csv
     * Generate CSV for the deliveries report.
     */
    @GetMapping("/deliveries/csv")
    public ResponseEntity<?> exportDeliveriesCsv(Authentication authentication) {
        return generateCsvReport(authentication, deliveriesFilePath, "deliveries_report.csv");
    }

    /**
     * Helper method to generate a CSV report.
     */
    private ResponseEntity<?> generateCsvReport(Authentication authentication, String filePath, String fileName) {
        try {
            User authUser = (User) authentication.getPrincipal();
            if (!userHasReportAccess(authUser)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            List<Map<String, Object>> data = objectMapper.readValue(new File(filePath), new TypeReference<>() {});
            File tempFile = File.createTempFile(fileName, ".csv");

            try (FileWriter writer = new FileWriter(tempFile)) {
                // Write CSV header
                if (!data.isEmpty()) {
                    writer.append(String.join(",", data.get(0).keySet())).append("\n");
                }

                // Write CSV rows
                for (Map<String, Object> entry : data) {
                    writer.append(String.join(",", entry.values().toString().replace("[", "").replace("]", ""))).append("\n");
                }
            }

            // Return CSV file as a download
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + fileName)
                    .body(Files.readAllBytes(tempFile.toPath()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error generating CSV report: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reports/inventory/pdf
     * Generate a PDF for the inventory report.
     */
    @GetMapping("/inventory/pdf")
    public ResponseEntity<?> exportInventoryPdf(Authentication authentication) {
        return generatePdfReport(authentication, inventoryFilePath, "Inventory Report");
    }

    /**
     * GET /api/reports/deliveries/pdf
     * Generate a PDF for the deliveries report.
     */
    @GetMapping("/deliveries/pdf")
    public ResponseEntity<?> exportDeliveriesPdf(Authentication authentication) {
        return generatePdfReport(authentication, deliveriesFilePath, "Deliveries Report");
    }

    /**
     * Helper method to generate a PDF report.
     */

    private ResponseEntity<?> generatePdfReport(Authentication authentication, String filePath, String reportTitle) {
        try (PDDocument document = new PDDocument()) {
            User authUser = (User) authentication.getPrincipal();
            if (!userHasReportAccess(authUser)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
    
            List<Map<String, Object>> data = objectMapper.readValue(new File(filePath), new TypeReference<>() {});
    
            PDPage page = new PDPage();
            document.addPage(page);
    
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Set font and title
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(reportTitle);
                contentStream.endText();
    
                // Add table (manually position content)
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                float yPosition = 700; // Start position for data
                float rowHeight = 20;
    
                if (!data.isEmpty()) {
                    // Write headers
                    float xPosition = 50;
                    for (String header : data.get(0).keySet()) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(xPosition, yPosition);
                        contentStream.showText(header);
                        contentStream.endText();
                        xPosition += 100; // Column width
                    }
    
                    // Write data rows
                    yPosition -= rowHeight;
                    for (Map<String, Object> entry : data) {
                        xPosition = 50;
                        for (Object value : entry.values()) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(xPosition, yPosition);
                            contentStream.showText(value.toString());
                            contentStream.endText();
                            xPosition += 100;
                        }
                        yPosition -= rowHeight;
                    }
                }
            }
    
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
    
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + reportTitle.toLowerCase().replace(" ", "_") + ".pdf")
                    .body(outputStream.toByteArray());
    
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error generating PDF report: " + e.getMessage()));
        }
    }
}
