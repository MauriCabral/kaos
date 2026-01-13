package org.example.kaos.service.implementation;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.kaos.entity.Order;
import org.example.kaos.entity.OrderDetail;
import org.example.kaos.entity.OrderDetailTopping;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

public class TicketPrintServiceImpl implements Printable {

    private final OrderDetailServiceImpl orderDetailService = new OrderDetailServiceImpl();
    private final OrderServiceImpl orderService = new OrderServiceImpl();

    private Order order;
    private List<OrderDetail> orderDetailList;
    private BufferedImage logo;
    private boolean isCopy = false;

    public TicketPrintServiceImpl() {
        try {
            logo = ImageIO.read(new File("C:/Kaos/kaoslogo.png"));
        } catch (IOException e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }
    }

    public void print(Long orderId) {
        orderLoad(orderId);

        PrinterJob job = PrinterJob.getPrinterJob();
        PrintService selectedPrinter = findPrinter("POS-80-Series");

        if (selectedPrinter == null) {
            selectedPrinter = findPrinter("Microsoft Print to PDF");
        }

        if (selectedPrinter != null) {
            try {
                job.setPrintService(selectedPrinter);

                PageFormat pf = job.defaultPage();
                Paper paper = new Paper();

                double width = 80 * 2.83;
                double height = 297 * 2.83;

                paper.setSize(width, height);
                paper.setImageableArea(0, 0, width, height);
                pf.setPaper(paper);
                pf.setOrientation(PageFormat.PORTRAIT);

                job.setPrintable(this, pf);

                isCopy = false;
                job.print();

                Thread.sleep(500);

                isCopy = true;
                job.print();

            } catch (PrinterException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No se encontró la impresora especificada");
        }
    }

    public void generatePDF(Long orderId) {
        System.out.println("¿Desea imprimir un PDF?");
        System.out.println("Generando PDF para la orden: " + orderId);

        orderLoad(orderId);

        if (order == null) {
            System.err.println("ERROR: No se pudo cargar la orden con ID: " + orderId);
            return;
        }

        System.out.println("Orden cargada: " + order.getOrderNumber());
        System.out.println("Total de items: " + (orderDetailList != null ? orderDetailList.size() : 0));

        PDDocument document = null;
        PDPageContentStream contentStream = null;

        try {
            document = new PDDocument();

            PDPage page = new PDPage(new PDRectangle(226.77f, 841.89f));
            document.addPage(page);

            contentStream = new PDPageContentStream(document, page);

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

            String title = "******** TICKET ********";
            float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 12;
            float titleX = (226.77f - titleWidth) / 2;

            contentStream.beginText();
            contentStream.newLineAtOffset(titleX, 800);
            contentStream.showText(title);
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);

            contentStream.beginText();
            contentStream.newLineAtOffset(10, 770);
            contentStream.showText("Orden: " + order.getOrderNumber());
            contentStream.endText();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaStr = order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : "N/A";
            contentStream.beginText();
            contentStream.newLineAtOffset(10, 755);
            contentStream.showText("Fecha: " + fechaStr);
            contentStream.endText();

            String cliente = order.getCustomerName() != null ? order.getCustomerName() : "";
            contentStream.beginText();
            contentStream.newLineAtOffset(10, 740);
            contentStream.showText("Cliente: " + cliente);
            contentStream.endText();

            float y = 725;

            if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(10, y);
                contentStream.showText("Tel: " + order.getCustomerPhone());
                contentStream.endText();
                y -= 15;
            }

            if (order.getCustomerAddress() != null && !order.getCustomerAddress().isEmpty()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(10, y);
                contentStream.showText("Dir: " + order.getCustomerAddress());
                contentStream.endText();
                y -= 15;
            }

            String tipoPedido = Boolean.TRUE.equals(order.getIsDelivery()) ? "DELIVERY" : "PARA RETIRAR";
            contentStream.beginText();
            contentStream.newLineAtOffset(10, y);
            contentStream.showText(tipoPedido);
            contentStream.endText();
            y -= 20;

            contentStream.moveTo(10, y);
            contentStream.lineTo(216.77f, y);
            contentStream.stroke();
            y -= 15;

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(10, y);
            contentStream.showText("CANT  DESCRIPCION");
            contentStream.endText();
            y -= 15;

            if (orderDetailList != null && !orderDetailList.isEmpty()) {
                for (OrderDetail detail : orderDetailList) {
                    contentStream.setFont(PDType1Font.HELVETICA, 9);

                    String itemLine = detail.getQuantity() + "x " + detail.getProductName();
                    if (detail.getVariantName() != null && !detail.getVariantName().isEmpty()) {
                        itemLine += " (" + detail.getVariantName() + ")";
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(10, y);
                    contentStream.showText(itemLine);
                    contentStream.endText();

                    String priceLine = String.format("$%.0f", detail.getSubtotal());
                    float priceWidth = PDType1Font.HELVETICA.getStringWidth(priceLine) / 1000 * 9;
                    float priceX = 216.77f - 10 - priceWidth;

                    contentStream.beginText();
                    contentStream.newLineAtOffset(priceX, y);
                    contentStream.showText(priceLine);
                    contentStream.endText();

                    y -= 12;

                    if (detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty()) {
                        for (OrderDetailTopping topping : detail.getOrderDetailToppings()) {
                            String toppingSymbol = Boolean.TRUE.equals(topping.getIsAdded()) ? "+" : "-";
                            String toppingLine = "  " + toppingSymbol + " " + topping.getTopping().getName();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(20, y);
                            contentStream.showText(toppingLine);
                            contentStream.endText();

                            if (Boolean.TRUE.equals(topping.getIsAdded()) && topping.getTotalPrice() > 0) {
                                String toppingPrice = String.format("$%.0f", topping.getTotalPrice());
                                float toppingPriceWidth = PDType1Font.HELVETICA.getStringWidth(toppingPrice) / 1000 * 9;
                                float toppingPriceX = 216.77f - 10 - toppingPriceWidth;

                                contentStream.beginText();
                                contentStream.newLineAtOffset(toppingPriceX, y);
                                contentStream.showText(toppingPrice);
                                contentStream.endText();
                            }

                            y -= 10;
                        }
                    }

                    if (detail.getObservations() != null && !detail.getObservations().isEmpty()) {
                        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(20, y);
                        contentStream.showText("Obs: " + detail.getObservations());
                        contentStream.endText();
                        y -= 10;
                        contentStream.setFont(PDType1Font.HELVETICA, 9);
                    }

                    y -= 5;
                }
            } else {
                contentStream.beginText();
                contentStream.newLineAtOffset(10, y);
                contentStream.showText("No hay items en esta orden");
                contentStream.endText();
                y -= 20;
            }

            y -= 5;
            contentStream.moveTo(10, y);
            contentStream.lineTo(216.77f, y);
            contentStream.stroke();
            y -= 15;

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(10, y);
            contentStream.showText("Subtotal:");
            contentStream.endText();

            String subtotalStr = String.format("$%.0f", order.getSubtotal());
            float subtotalWidth = PDType1Font.HELVETICA.getStringWidth(subtotalStr) / 1000 * 10;
            float subtotalX = 216.77f - 10 - subtotalWidth;

            contentStream.beginText();
            contentStream.newLineAtOffset(subtotalX, y);
            contentStream.showText(subtotalStr);
            contentStream.endText();
            y -= 15;

            if (Boolean.TRUE.equals(order.getIsDelivery()) && order.getDeliveryAmount() != null && order.getDeliveryAmount().compareTo(BigDecimal.ZERO) > 0) {
                contentStream.beginText();
                contentStream.newLineAtOffset(10, y);
                contentStream.showText("Delivery:");
                contentStream.endText();

                String deliveryStr = String.format("$%.0f", order.getDeliveryAmount());
                float deliveryWidth = PDType1Font.HELVETICA.getStringWidth(deliveryStr) / 1000 * 10;
                float deliveryX = 216.77f - 10 - deliveryWidth;

                contentStream.beginText();
                contentStream.newLineAtOffset(deliveryX, y);
                contentStream.showText(deliveryStr);
                contentStream.endText();
                y -= 15;
            }

            contentStream.moveTo(10, y);
            contentStream.lineTo(216.77f, y);
            contentStream.stroke();

            contentStream.moveTo(10, y - 2);
            contentStream.lineTo(216.77f, y - 2);
            contentStream.stroke();

            y -= 15;

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(10, y);
            contentStream.showText("TOTAL:");
            contentStream.endText();

            String totalStr = String.format("$%.0f", order.getTotal());
            float totalWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(totalStr) / 1000 * 12;
            float totalX = 216.77f - 10 - totalWidth;

            contentStream.beginText();
            contentStream.newLineAtOffset(totalX, y);
            contentStream.showText(totalStr);
            contentStream.endText();
            y -= 25;

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            StringBuilder paymentMethod = new StringBuilder("Pago: ");

            if (order.getCashAmount() != null && order.getCashAmount().compareTo(BigDecimal.ZERO) > 0) {
                paymentMethod.append("Efectivo");
            }

            if (order.getTransferAmount() != null && order.getTransferAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (paymentMethod.length() > 6) {
                    paymentMethod.append(" + ");
                }
                paymentMethod.append("Transferencia");
            }

            contentStream.beginText();
            contentStream.newLineAtOffset(10, y);
            contentStream.showText(paymentMethod.toString());
            contentStream.endText();
            y -= 15;

            if (order.getNotes() != null && !order.getNotes().isEmpty()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(10, y);
                contentStream.showText("Notas: " + order.getNotes());
                contentStream.endText();
                y -= 15;
            }

            y -= 10;

            contentStream.setFont(PDType1Font.HELVETICA, 9);
            String gracias = "¡Gracias por su compra!";
            float graciasWidth = PDType1Font.HELVETICA.getStringWidth(gracias) / 1000 * 9;
            float graciasX = (226.77f - graciasWidth) / 2;

            contentStream.beginText();
            contentStream.newLineAtOffset(graciasX, y);
            contentStream.showText(gracias);
            contentStream.endText();

            y -= 12;

            String kaos = "Kaos";
            float kaosWidth = PDType1Font.HELVETICA.getStringWidth(kaos) / 1000 * 9;
            float kaosX = (226.77f - kaosWidth) / 2;

            contentStream.beginText();
            contentStream.newLineAtOffset(kaosX, y);
            contentStream.showText(kaos);
            contentStream.endText();

            contentStream.close();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = order.getOrderNumber() + "_" + timestamp + ".pdf";

            Stage stage = new Stage();
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            fileChooser.setInitialFileName(fileName);

            File selectedFile = fileChooser.showSaveDialog(stage);

            if (selectedFile != null) {
                File finalFile = new File(selectedFile.getParent(), fileName);

                document.save(finalFile);

                System.out.println("PDF generado exitosamente: " + finalFile.getName());
                System.out.println("Archivo guardado en: " + finalFile.getAbsolutePath());
                System.out.println("Tamaño del archivo: " + finalFile.length() + " bytes");
            } else {
                System.err.println("No se seleccionó una ubicación para guardar el archivo.");
            }
        } catch (Exception e) {
            System.err.println("Error al generar PDF: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
                if (document != null) {
                    document.close();
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    private PrintService findPrinter(String printerName) {
        PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printer : printers) {
            if (printer.getName().contains(printerName)) {
                return printer;
            }
        }
        return null;
    }

    private void orderLoad(Long orderId) {
        order = orderService.getOrderById(orderId);
        orderDetailList = orderDetailService.orderDetailFindByIdOrder(orderId);
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0 || order == null) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        Font titleFont = new Font("Monospaced", Font.BOLD, 12);
        Font headerFont = new Font("Monospaced", Font.BOLD, 10);
        Font normalFont = new Font("Monospaced", Font.PLAIN, 9);
        Font smallFont = new Font("Monospaced", Font.PLAIN, 8);

        int y = 20;
        int pageWidth = (int) pageFormat.getImageableWidth();

        g2d.setFont(titleFont);
        String title = isCopy ? "******** COPIA ********" : "******** TICKET ********";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        int titleX = (pageWidth - titleWidth) / 2;
        g2d.drawString(title, titleX, y);
        y += 20;

        if (logo != null) {
            int logoWidth = 80;
            int logoHeight = 20;
            int logoX = (pageWidth - logoWidth) / 2;
            g2d.drawImage(logo, logoX, y, logoWidth, logoHeight, null);
            y += 30;
        }

        g2d.setFont(headerFont);
        g2d.drawString("Orden: " + order.getOrderNumber(), 10, y);
        y += 15;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        g2d.drawString("Fecha: " + order.getCreatedAt().format(formatter), 10, y);
        y += 15;

        g2d.setFont(normalFont);
        g2d.drawString("Cliente: " + order.getCustomerName(), 10, y);
        y += 12;

        if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
            g2d.drawString("Tel: " + order.getCustomerPhone(), 10, y);
            y += 12;
        }

        if (order.getCustomerAddress() != null && !order.getCustomerAddress().isEmpty()) {
            g2d.drawString("Dir: " + order.getCustomerAddress(), 10, y);
            y += 12;
        }

        String tipoPedido = Boolean.TRUE.equals(order.getIsDelivery()) ? "🚚 DELIVERY" : "🏪 PARA RETIRAR";
        g2d.setFont(headerFont);
        g2d.drawString(tipoPedido, 10, y);
        y += 15;

        g2d.drawLine(10, y, pageWidth - 10, y);
        y += 10;

        g2d.setFont(headerFont);
        g2d.drawString("CANT  DESCRIPCION", 10, y);
        y += 15;

        g2d.setFont(normalFont);
        for (OrderDetail detail : orderDetailList) {
            String itemLine = String.format("%dx %s",
                    detail.getQuantity(),
                    detail.getProductName());

            if (detail.getVariantName() != null && !detail.getVariantName().isEmpty()) {
                itemLine += " (" + detail.getVariantName() + ")";
            }

            g2d.drawString(itemLine, 10, y);

            String priceLine = String.format("$%.0f", detail.getSubtotal());
            int priceWidth = g2d.getFontMetrics().stringWidth(priceLine);
            int priceX = pageWidth - 10 - priceWidth;
            g2d.drawString(priceLine, priceX, y);
            y += 12;

            if (detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty()) {
                for (OrderDetailTopping topping : detail.getOrderDetailToppings()) {
                    String toppingSymbol = Boolean.TRUE.equals(topping.getIsAdded()) ? "✓" : "✗";
                    String toppingLine = "  " + toppingSymbol + " " + topping.getTopping().getName();

                    g2d.drawString(toppingLine, 20, y);

                    if (Boolean.TRUE.equals(topping.getIsAdded()) && topping.getTotalPrice() > 0) {
                        String toppingPrice = String.format("$%.0f", topping.getTotalPrice());
                        int toppingPriceWidth = g2d.getFontMetrics().stringWidth(toppingPrice);
                        int toppingPriceX = pageWidth - 10 - toppingPriceWidth;
                        g2d.drawString(toppingPrice, toppingPriceX, y);
                    }
                    y += 10;
                }
            }

            if (detail.getObservations() != null && !detail.getObservations().isEmpty()) {
                String[] obsLines = wrapText("Obs: " + detail.getObservations(), g2d, pageWidth - 40);
                for (String line : obsLines) {
                    g2d.setFont(smallFont);
                    g2d.drawString(line, 20, y);
                    y += 10;
                }
                g2d.setFont(normalFont);
            }

            y += 5;
        }

        g2d.drawLine(10, y, pageWidth - 10, y);
        y += 15;

        g2d.setFont(normalFont);

        g2d.drawString("Subtotal:", 10, y);
        String subtotalStr = String.format("$%.0f", order.getSubtotal());
        int subtotalWidth = g2d.getFontMetrics().stringWidth(subtotalStr);
        int subtotalX = pageWidth - 10 - subtotalWidth;
        g2d.drawString(subtotalStr, subtotalX, y);
        y += 12;

        if (Boolean.TRUE.equals(order.getIsDelivery()) && order.getDeliveryAmount() != null && order.getDeliveryAmount().compareTo(BigDecimal.ZERO) > 0) {
            g2d.drawString("Delivery:", 10, y);
            String deliveryStr = String.format("$%.0f", order.getDeliveryAmount());
            int deliveryWidth = g2d.getFontMetrics().stringWidth(deliveryStr);
            int deliveryX = pageWidth - 10 - deliveryWidth;
            g2d.drawString(deliveryStr, deliveryX, y);
            y += 12;
        }

        g2d.setFont(headerFont);
        g2d.drawLine(10, y, pageWidth - 10, y);
        y += 5;
        g2d.drawLine(10, y, pageWidth - 10, y);
        y += 10;

        g2d.drawString("TOTAL:", 10, y);
        String totalStr = String.format("$%.0f", order.getTotal());
        int totalWidth = g2d.getFontMetrics().stringWidth(totalStr);
        int totalX = pageWidth - 10 - totalWidth;
        g2d.drawString(totalStr, totalX, y);
        y += 20;

        g2d.setFont(normalFont);
        StringBuilder paymentMethod = new StringBuilder("Pago: ");

        if (order.getCashAmount() != null && order.getCashAmount().compareTo(BigDecimal.ZERO) > 0) {
            paymentMethod.append("Efectivo");
        }

        if (order.getTransferAmount() != null && order.getTransferAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (paymentMethod.length() > 6) {
                paymentMethod.append(" + ");
            }
            paymentMethod.append("Transferencia");
        }

        g2d.drawString(paymentMethod.toString(), 10, y);
        y += 12;

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            g2d.drawString("Notas: " + order.getNotes(), 10, y);
            y += 12;
        }

        y += 10;
        g2d.setFont(smallFont);

        String gracias = "¡Gracias por su compra!";
        int graciasWidth = g2d.getFontMetrics().stringWidth(gracias);
        int graciasX = (pageWidth - graciasWidth) / 2;
        g2d.drawString(gracias, graciasX, y);
        y += 10;

        String kaos = "Kaos";
        int kaosWidth = g2d.getFontMetrics().stringWidth(kaos);
        int kaosX = (pageWidth - kaosWidth) / 2;
        g2d.drawString(kaos, kaosX, y);

        return PAGE_EXISTS;
    }

    private String[] wrapText(String text, Graphics2D g2d, int maxWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        java.util.List<String> lines = new java.util.ArrayList<>();

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (fm.stringWidth(currentLine.toString() + word) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word + " ");
            } else {
                currentLine.append(word).append(" ");
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }

        return lines.toArray(new String[0]);
    }
}