package org.example.kaos.service.implementation;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
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
            var stream = getClass().getClassLoader().getResourceAsStream("image/kaoslogo.png");
            logo = stream != null ? ImageIO.read(stream) : null;
        } catch (Exception e) {
            logo = null;
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
        orderLoad(orderId);

        if (order == null) {
            System.err.println("ERROR: No se pudo cargar la orden con ID: " + orderId);
            return;
        }

        PDDocument document = null;
        PDPageContentStream contentStream = null;

        try {
            document = new PDDocument();
            PDPage page = new PDPage(new PDRectangle(226.77f, 841.89f));
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);

            // ======= TITULO =======
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            String title = isCopy ? "******** COPIA ********" : "****** TICKET *******";
            float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 12;
            float titleX = (226.77f - titleWidth) / 2;
            float y = 800;

            contentStream.beginText();
            contentStream.newLineAtOffset(titleX, y);
            contentStream.showText(title);
            contentStream.endText();

            // ======= LOGO =======
            y -= 5;
            if (logo != null) {
                float logoWidth = 100;
                float logoHeight = (logo.getHeight() * logoWidth) / logo.getWidth();
                float logoX = (226.77f - logoWidth) / 2;
                contentStream.drawImage(LosslessFactory.createFromImage(document, logo), logoX, y - logoHeight, logoWidth, logoHeight);
                y -= logoHeight + 15;
            }

            // ======= FECHA Y CLIENTE =======
            contentStream.setFont(PDType1Font.HELVETICA, 8); // Smaller font for date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaStr = order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : "N/A";

            contentStream.beginText();
            contentStream.newLineAtOffset(10, y);
            contentStream.showText("Fecha: " + fechaStr);
            contentStream.endText();
            y -= 15;

            String cliente = order.getCustomerName() != null ? order.getCustomerName() : "";
            contentStream.beginText();
            contentStream.newLineAtOffset(10, y);
            contentStream.showText("Cliente: " + cliente.toUpperCase());
            contentStream.endText();
            y -= 12;

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
            y -= 10;

            contentStream.moveTo(10, y);
            contentStream.lineTo(216.77f, y);
            contentStream.stroke();
            y -= 15;

            // ======= ENCABEZADO DE ITEMS =======
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.beginText();
            y -= 15;

            // ======= ITEMS, TOPPINGS Y OBSERVACIONES =======
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            if (orderDetailList != null && !orderDetailList.isEmpty()) {
                for (OrderDetail detail : orderDetailList) {

                    // Producto principal
                    String itemLine = detail.getQuantity() + "x " + detail.getProductName();
                    if (detail.getVariantName() != null && !detail.getVariantName().isEmpty()) {
                        itemLine += " (" + detail.getVariantName() + ")";
                    }
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
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

                    // TOPPINGS
                    if (detail.getOrderDetailToppings() != null) {
                        for (OrderDetailTopping topping : detail.getOrderDetailToppings()) {
                            if (topping.getQuantity() > 0) {
                                String toppingLine = "  + " + topping.getTopping().getName() + " x" + topping.getQuantity();
                                contentStream.beginText();
                                contentStream.newLineAtOffset(20, y);
                                contentStream.showText(toppingLine);
                                contentStream.endText();

                                String toppingPrice = String.format("$%.0f", topping.getTotalPrice());
                                float toppingPriceWidth = PDType1Font.HELVETICA.getStringWidth(toppingPrice) / 1000 * 9;
                                float toppingPriceX = 216.77f - 10 - toppingPriceWidth;
                                contentStream.beginText();
                                contentStream.newLineAtOffset(toppingPriceX, y);
                                contentStream.showText(toppingPrice);
                                contentStream.endText();
                                y -= 10;
                            }
                        }
                    }

                    // OBSERVACIONES
                    if (detail.getObservations() != null && !detail.getObservations().isEmpty()) {
                        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(20, y);
                        contentStream.showText("Obs: " + detail.getObservations());
                        contentStream.endText();
                        contentStream.setFont(PDType1Font.HELVETICA, 9);
                        y -= 10;
                    }

                    y -= 5;
                }
            }

            // ======= SUBTOTAL, DELIVERY, TOTAL =======
            y -= 5;
            contentStream.moveTo(10, y);
            contentStream.lineTo(216.77f, y);
            contentStream.stroke();
            y -= 10;

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
            y -= 12;

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
                y -= 10;
            }

            contentStream.moveTo(10, y);
            contentStream.lineTo(216.77f, y);
            contentStream.stroke();
            y -= 15;

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(10, y);
            contentStream.showText("TOTAL:");
            contentStream.endText();

            String totalStr = String.format("$%.0f", order.getTotal());
            float totalWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(totalStr) / 1000 * 14;
            float totalX = 216.77f - 10 - totalWidth;
            contentStream.beginText();
            contentStream.newLineAtOffset(totalX, y);
            contentStream.showText(totalStr);
            contentStream.endText();
            y -= 20;

            // ======= PAGO =======
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            StringBuilder paymentMethod = new StringBuilder("Pago: ");
            boolean hasCash = order.getCashAmount() != null && order.getCashAmount().compareTo(BigDecimal.ZERO) > 0;
            boolean hasTransfer = order.getTransferAmount() != null && order.getTransferAmount().compareTo(BigDecimal.ZERO) > 0;

            if (hasCash && hasTransfer) {
                paymentMethod.append("Efectivo + Transferencia");
            } else if (hasCash) {
                paymentMethod.append("Efectivo");
            } else if (hasTransfer) {
                paymentMethod.append("Transferencia");
            } else {
                paymentMethod.append("No especificado");
            }

            contentStream.beginText();
            contentStream.newLineAtOffset(10, y);
            contentStream.showText(paymentMethod.toString());
            contentStream.endText();
            y -= 12;

            // ======= NOTAS =======
            if (order.getNotes() != null && !order.getNotes().isEmpty()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(10, y);
                contentStream.showText("Notas: " + order.getNotes());
                contentStream.endText();
                y -= 12;
            }

            // ======= GRACIAS =======
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            String gracias = "¡Gracias por comprar en KAOS!";
            float graciasWidth = PDType1Font.HELVETICA.getStringWidth(gracias) / 1000 * 9;
            float graciasX = (226.77f - graciasWidth) / 2;
            contentStream.beginText();
            contentStream.newLineAtOffset(graciasX, y);
            contentStream.showText(gracias);
            contentStream.endText();

            contentStream.close();

            // ======= GUARDAR PDF =======
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
                System.out.println("PDF generado exitosamente: " + finalFile.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (contentStream != null) contentStream.close();
                if (document != null) document.close();
            } catch (IOException e) {
                e.printStackTrace();
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

        Font titleFont = new Font("SansSerif", Font.BOLD, 12);
        Font headerFont = new Font("SansSerif", Font.BOLD, 10);
        Font totalFont = new Font("Arial", Font.BOLD, 12);
        Font normalFont = new Font("SansSerif", Font.PLAIN, 9);
        Font smallFont = new Font("SansSerif", Font.ITALIC, 8);

        int y = 20;
        int pageWidth = (int) pageFormat.getImageableWidth();

        g2d.setFont(titleFont);
        String title = isCopy ? "******** COPIA ********" : "****** TICKET *******";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        int titleX = (pageWidth - titleWidth) / 2;
        g2d.drawString(title, titleX, y);

        y += 15;
        if (logo != null) {
            int logoWidth = 80;
            int logoHeight = (logo.getHeight() * logoWidth) / logo.getWidth();
            int logoX = (pageWidth - logoWidth) / 2;
            g2d.drawImage(logo, logoX, y, logoWidth, logoHeight, null);
            y += logoHeight + 15;
        }

        //g2d.drawString("Orden: " + order.getOrderNumber(), 10, y);
        //y += 15;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        g2d.setFont(smallFont); // Use smaller font for date
        g2d.drawString("Fecha: " + order.getCreatedAt().format(formatter), 10, y);
        y += 15;

        g2d.setFont(normalFont);
        g2d.drawString("Cliente: " + order.getCustomerName().toUpperCase(), 10, y);
        y += 12;

//        if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
//            g2d.drawString("Tel: " + order.getCustomerPhone(), 10, y);
//            y += 12;
//        }

        if (order.getCustomerAddress() != null && !order.getCustomerAddress().isEmpty()) {
            g2d.drawString("Dir: " + order.getCustomerAddress(), 10, y);
            y += 12;
        }

        String tipoPedido = Boolean.TRUE.equals(order.getIsDelivery()) ? "DELIVERY" : "PARA RETIRAR";
        g2d.setFont(headerFont);
        g2d.drawString(tipoPedido, 10, y);
        y += 10;
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(10, y, pageWidth - 10, y);

        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);
        y += 10;

        g2d.setFont(headerFont);
        y += 15;

        for (OrderDetail detail : orderDetailList) {
            String itemLine = String.format("%dx %s",
                    detail.getQuantity(),
                    detail.getProductName());

            if (detail.getVariantName() != null && !detail.getVariantName().isEmpty()) {
                itemLine += " (" + detail.getVariantName() + ")";
            }

            g2d.setFont(headerFont); // Bold font for burger names
            g2d.drawString(itemLine, 10, y);

            String priceLine = String.format("$%.0f", detail.getSubtotal());
            int priceWidth = g2d.getFontMetrics().stringWidth(priceLine);
            int priceX = pageWidth - 10 - priceWidth;
            g2d.drawString(priceLine, priceX, y);
            y += 12;

            if (detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty()) {
                for (OrderDetailTopping topping : detail.getOrderDetailToppings()) {
                    if (topping.getQuantity() > 0) {
                        String toppingLine = "  + " + topping.getTopping().getName() + " x" + topping.getQuantity();

                        g2d.drawString(toppingLine, 20, y);

                        String toppingPrice = String.format("$%.0f", topping.getTotalPrice());
                        int toppingPriceWidth = g2d.getFontMetrics().stringWidth(toppingPrice);
                        int toppingPriceX = pageWidth - 10 - toppingPriceWidth;
                        g2d.drawString(toppingPrice, toppingPriceX, y);

                        y += 10;
                    }
                }
            }

            if (detail.getObservations() != null && !detail.getObservations().isEmpty()) {
                g2d.setFont(smallFont);
                g2d.drawString("Obs: " + detail.getObservations(), 20, y);
                y += 5;
                g2d.setFont(normalFont);
            }

            y += 5;
        }
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(10, y, pageWidth - 10, y);
        g2d.setStroke(originalStroke);
        y += 10;

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
            y += 5;
        }

        g2d.setFont(headerFont);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(10, y, pageWidth - 10, y);

        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);
        y += 12;

        g2d.setFont(totalFont);
        g2d.drawString("TOTAL:", 10, y);
        String totalStr = String.format("$%.0f", order.getTotal());
        int totalWidth = g2d.getFontMetrics().stringWidth(totalStr);
        int totalX = pageWidth - 10 - totalWidth;
        g2d.drawString(totalStr, totalX, y);
        y += 20;

        g2d.setFont(normalFont);
        StringBuilder paymentMethod = new StringBuilder("Pago: ");
        boolean hasCash = order.getCashAmount() != null && order.getCashAmount().compareTo(BigDecimal.ZERO) > 0;
        boolean hasTransfer = order.getTransferAmount() != null && order.getTransferAmount().compareTo(BigDecimal.ZERO) > 0;

        if (hasCash && hasTransfer) {
            paymentMethod.append("Efectivo + Transferencia");
        } else if (hasCash) {
            paymentMethod.append("Efectivo");
        } else if (hasTransfer) {
            paymentMethod.append("Transferencia");
        } else {
            paymentMethod.append("No especificado");
        }

        g2d.drawString(paymentMethod.toString(), 10, y);
        y += 12;

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            g2d.drawString("Notas: " + order.getNotes(), 10, y);
            y += 15;
        }

        y += 15;
        g2d.setFont(smallFont);

        String gracias = "¡Gracias por comprar en KAOS!";
        int graciasWidth = g2d.getFontMetrics().stringWidth(gracias);
        int graciasX = (pageWidth - graciasWidth) / 2;
        g2d.drawString(gracias, graciasX, y);
        y += 12;

//        String kaos = "Kaos";
//        int kaosWidth = g2d.getFontMetrics().stringWidth(kaos);
//        int kaosX = (pageWidth - kaosWidth) / 2;
//        g2d.drawString(kaos, kaosX, y);


        return PAGE_EXISTS;
    }
}