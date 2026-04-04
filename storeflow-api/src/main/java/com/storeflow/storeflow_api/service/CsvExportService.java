package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CsvExportService {

    /**
     * Generate CSV export for orders
     * @param orders List of Order entities
     * @return CSV content as byte array
     */
    public byte[] generateOrdersCsv(List<Order> orders) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                 .withHeader("Order Number", "Customer Name", "Product Name", "Quantity", 
                     "Unit Price", "Total", "Order Status", "Order Date"))) {

            for (Order order : orders) {
                for (OrderItem item : order.getItems()) {
                    csvPrinter.printRecord(
                        order.getOrderNumber(),
                        order.getCustomerName(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        String.format("%.2f", item.getUnitPrice()),
                        String.format("%.2f", order.getTotal()),
                        order.getStatus().toString(),
                        order.getCreatedAt()
                    );
                }
            }

            csvPrinter.flush();
            log.info("CSV export generated for {} orders", orders.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate CSV export", e);
            throw e;
        }
    }

    /**
     * Generate CSV export for orders within date range
     * @param orders List of Order entities
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @return CSV content as byte array
     */
    public byte[] generateOrdersCsvWithDateFilter(List<Order> orders, LocalDateTime fromDate, LocalDateTime toDate) 
            throws IOException {
        List<Order> filteredOrders = orders.stream()
            .filter(order -> {
                LocalDateTime createdAt = order.getCreatedAt();
                return (createdAt.isEqual(fromDate) || createdAt.isAfter(fromDate)) &&
                       (createdAt.isEqual(toDate) || createdAt.isBefore(toDate));
            })
            .toList();

        return generateOrdersCsv(filteredOrders);
    }

}
