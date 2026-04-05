package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfGenerationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final float MARGIN_LEFT = 50;
    private static final float MARGIN_TOP = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    /**
     * Generate PDF report for an order
     * @param order Order entity
     * @return PDF file as byte array
     */
    public byte[] generateOrderReport(Order order) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = PAGE_HEIGHT - MARGIN_TOP;

                // Title
                yPosition = writeText(contentStream, "ORDER REPORT", PDType1Font.HELVETICA_BOLD, 18, yPosition);
                yPosition -= 15;

                // Order Info
                yPosition = writeText(contentStream, "Order Number: " + order.getOrderNumber(), 
                    PDType1Font.HELVETICA, 11, yPosition);
                yPosition = writeText(contentStream, "Customer: " + order.getCustomerName(), 
                    PDType1Font.HELVETICA, 11, yPosition);
                yPosition = writeText(contentStream, "Customer Email: " + order.getCustomerEmail(), 
                    PDType1Font.HELVETICA, 11, yPosition);
                yPosition = writeText(contentStream, "Order Date: " + order.getCreatedAt().format(DATE_FORMATTER), 
                    PDType1Font.HELVETICA, 11, yPosition);
                yPosition = writeText(contentStream, "Status: " + order.getStatus().toString(), 
                    PDType1Font.HELVETICA, 11, yPosition);
                yPosition -= 15;

                // Shipping Address
                yPosition = writeText(contentStream, "SHIPPING ADDRESS", PDType1Font.HELVETICA_BOLD, 12, yPosition);
                yPosition = writeText(contentStream, order.getShippingAddress() != null ? order.getShippingAddress().toString() : "N/A", 
                    PDType1Font.HELVETICA, 10, yPosition);
                yPosition -= 15;

                // Items Header
                yPosition = writeText(contentStream, "ORDER ITEMS", PDType1Font.HELVETICA_BOLD, 12, yPosition);
                yPosition = writeText(contentStream, "Product | Quantity | Unit Price | Subtotal", 
                    PDType1Font.HELVETICA_BOLD, 10, yPosition);
                yPosition -= 10;

                // Items
                for (OrderItem item : order.getItems()) {
                    String itemLine = String.format("%s | %d | %.2f | %.2f",
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
                    yPosition = writeText(contentStream, itemLine, PDType1Font.HELVETICA, 9, yPosition);

                    if (yPosition < MARGIN_TOP + 100) {
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        yPosition = PAGE_HEIGHT - MARGIN_TOP;
                    }
                }

                yPosition -= 15;

                // Total
                yPosition = writeText(contentStream, 
                    String.format("TOTAL AMOUNT: %.2f", order.getTotal()), 
                    PDType1Font.HELVETICA_BOLD, 12, yPosition);
            }

            document.save(outputStream);
            log.info("PDF generated for order: {}", order.getOrderNumber());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate PDF for order: {}", order.getOrderNumber(), e);
            throw e;
        }
    }

    /**
     * Write text to PDF at given Y position
     * @param stream PDPageContentStream to write to
     * @param text Text to write
     * @param font Font to use
     * @param fontSize Font size
     * @param yPosition Current Y position
     * @return New Y position after writing
     */
    private float writeText(PDPageContentStream stream, String text, PDFont font, int fontSize, float yPosition) 
            throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(MARGIN_LEFT, yPosition);
        stream.showText(text);
        stream.endText();
        return yPosition - (fontSize * 1.5f);
    }

}
