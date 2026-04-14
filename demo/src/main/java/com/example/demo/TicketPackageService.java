package com.example.demo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class TicketPackageService {

    private static final int QR_SIZE = 360;

    public MailAttachment buildTicketZipAttachment(String bookingId, List<Ticket> tickets, Map<String, TicketCategory> categories) {
        try {
            byte[] zipBytes = createZipBytes(bookingId, tickets, categories);
            return new MailAttachment(
                    "emts-booking-" + bookingId + "-tickets.zip",
                    Base64.getEncoder().encodeToString(zipBytes),
                    "application/zip"
            );
        } catch (IOException | WriterException exception) {
            throw new RuntimeException("Unable to build ticket ZIP attachment", exception);
        }
    }

    public int calculateAttachmentSizeBytes(String bookingId, List<Ticket> tickets, Map<String, TicketCategory> categories) {
        try {
            return createZipBytes(bookingId, tickets, categories).length;
        } catch (IOException | WriterException exception) {
            throw new RuntimeException("Unable to calculate ticket ZIP size", exception);
        }
    }

    private byte[] createZipBytes(String bookingId, List<Ticket> tickets, Map<String, TicketCategory> categories) throws IOException, WriterException {
        List<Ticket> sortedTickets = new ArrayList<>(tickets);
        sortedTickets.sort(Comparator.comparing(Ticket::getCategoryId).thenComparing(Ticket::getTicketId));

        Map<String, Integer> categoryCounts = new HashMap<>();
        for (Ticket ticket : sortedTickets) {
            categoryCounts.merge(ticket.getCategoryId(), 1, Integer::sum);
        }

        int maxCategoryCount = categoryCounts.values().stream().max(Integer::compareTo).orElse(1);
        int serialWidth = Math.max(2, String.valueOf(maxCategoryCount).length());
        Map<String, Integer> serials = new HashMap<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
            for (Ticket ticket : sortedTickets) {
                TicketCategory category = categories.get(ticket.getCategoryId());
                String categoryName = category == null ? "Ticket" : sanitizeFileName(category.getName());
                int serial = serials.merge(ticket.getCategoryId(), 1, Integer::sum);
                String serialLabel = String.format(Locale.ROOT, "%0" + serialWidth + "d", serial);
                String filename = categoryName + "_" + serialLabel + "_" + ticket.getTicketId() + "_" + bookingId + ".png";

                ZipEntry entry = new ZipEntry(filename);
                zipStream.putNextEntry(entry);
                zipStream.write(generateQrPngBytes(ticket));
                zipStream.closeEntry();
            }
        }
        return outputStream.toByteArray();
    }

    private byte[] generateQrPngBytes(Ticket ticket) throws WriterException, IOException {
        String payload = "{\"ticketId\":\"" + escapeJson(ticket.getTicketId()) + "\",\"qrCode\":\"" + escapeJson(ticket.getQrCode()) + "\"}";
        BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
        BufferedImage image = new BufferedImage(QR_SIZE, QR_SIZE, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < QR_SIZE; x += 1) {
            for (int y = 0; y < QR_SIZE; y += 1) {
                image.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }

    private String sanitizeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
