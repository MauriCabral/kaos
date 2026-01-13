package org.example.kaos.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.example.kaos.repository.OrderRepository;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class OrderStatisticsController implements Initializable {

    @FXML private PieChart dailyBurgersPieChart;
    @FXML private PieChart weeklyBurgersPieChart;
    @FXML private BarChart<String, Number> salesPerDayBarChart;

    private final OrderRepository orderRepository = new OrderRepository();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dailyBurgersPieChart.setLabelsVisible(true);
        dailyBurgersPieChart.setLegendVisible(false);
        weeklyBurgersPieChart.setLabelsVisible(true);
        weeklyBurgersPieChart.setLegendVisible(false);
        loadDailyBurgersChart();
        loadWeeklyBurgersChart();
        loadSalesPerDayChart();
    }

    private void loadDailyBurgersChart() {
        LocalDate today = LocalDate.now();
        Map<String, Integer> burgerSales = orderRepository.getBurgerSalesForDate(today);
        dailyBurgersPieChart.getData().clear();
        for (Map.Entry<String, Integer> entry : burgerSales.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            data.setName(entry.getKey() + " (" + entry.getValue() + ")");
            dailyBurgersPieChart.getData().add(data);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, new Tooltip(entry.getKey() + ": " + entry.getValue()));
                }
            });
        }
    }

    private void loadWeeklyBurgersChart() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        Map<String, Integer> burgerSales = orderRepository.getBurgerSalesForDateRange(monday, sunday);
        weeklyBurgersPieChart.getData().clear();
        for (Map.Entry<String, Integer> entry : burgerSales.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            data.setName(entry.getKey() + " (" + entry.getValue() + ")");
            weeklyBurgersPieChart.getData().add(data);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, new Tooltip(entry.getKey() + ": " + entry.getValue()));
                }
            });
        }
    }

    private void loadSalesPerDayChart() {
        LocalDate startDate = LocalDate.now().minusDays(29);
        List<Map<String, Object>> salesData = orderRepository.getSalesPerDay(startDate, LocalDate.now());
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        salesPerDayBarChart.getData().clear();

        for (Map<String, Object> data : salesData) {
            String date = (String) data.get("date");
            Number total = (Number) data.get("total");

            String displayDate = formatDateForDisplay(date);

            XYChart.Data<String, Number> chartData = new XYChart.Data<>(displayDate, total);

            chartData.setExtraValue(total);

            series.getData().add(chartData);
        }

        salesPerDayBarChart.getData().add(series);
        salesPerDayBarChart.setLegendVisible(false);

        Platform.runLater(() -> {
            addValueLabelsToBars(series);
        });
    }

    private String formatDateForDisplay(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            return date.getDayOfMonth() + "/" + date.getMonthValue();
        } catch (Exception e) {
            return dateString;
        }
    }

    private void addValueLabelsToBars(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                Tooltip tooltip = new Tooltip(
                        String.format("Fecha: %s\nTotal: $%.2f",
                                data.getXValue(),
                                data.getYValue().doubleValue())
                );
                tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                Tooltip.install(node, tooltip);

                addLabelOnTopOfBar(data, node);
            }
        }
    }

    private void addLabelOnTopOfBar(XYChart.Data<String, Number> data, Node node) {
        if (node instanceof StackPane) {
            StackPane bar = (StackPane) node;

            Label label = new Label(String.format("$%.0f", data.getYValue().doubleValue()));
            label.setStyle(
                    "-fx-font-size: 9px; " +
                            "-fx-text-fill: black; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-color: rgba(255,255,255,0.8); " +
                            "-fx-padding: 2px 4px; " +
                            "-fx-background-radius: 3px;"
            );

            bar.getChildren().add(label);

            label.setTranslateY(-label.getHeight() - 5);

            label.translateXProperty().bind(bar.widthProperty().subtract(label.widthProperty()).divide(2));
        }
    }
}