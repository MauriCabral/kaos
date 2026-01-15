package org.example.kaos.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.kaos.entity.Store;
import org.example.kaos.repository.OrderRepository;
import org.example.kaos.repository.StoreRepository;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class OrderStatisticsController implements Initializable {

    @FXML private VBox chartsContainer;

    private final OrderRepository orderRepository = new OrderRepository();
    private final StoreRepository storeRepository = new StoreRepository();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Store> stores = storeRepository.findAll();
        for (Store store : stores) {
            createChartsForStore(store);
        }
    }

    private void createChartsForStore(Store store) {
        // Store title
        Label storeLabel = new Label(" " + store.getName());
        storeLabel.getStyleClass().add("store-title");

        // Charts grid
        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(15);

        // Daily burgers pie chart
        VBox dailyVBox = new VBox(5);
        dailyVBox.getStyleClass().add("chart-container");
        HBox dailyHBox = new HBox();
        dailyHBox.setAlignment(javafx.geometry.Pos.CENTER);
        Label dailyLabel = new Label("Más Vendidas - Hoy");
        dailyLabel.getStyleClass().add("chart-title");
        dailyHBox.getChildren().add(dailyLabel);
        PieChart dailyPieChart = new PieChart();
        dailyPieChart.setLabelsVisible(true);
        dailyPieChart.setLegendVisible(false);
        dailyVBox.getChildren().addAll(dailyHBox, dailyPieChart);
        GridPane.setConstraints(dailyVBox, 0, 0);

        // Weekly burgers pie chart
        VBox weeklyVBox = new VBox(5);
        weeklyVBox.getStyleClass().add("chart-container");
        HBox weeklyHBox = new HBox();
        weeklyHBox.setAlignment(javafx.geometry.Pos.CENTER);
        Label weeklyLabel = new Label("Más Vendidas - Semana");
        weeklyLabel.getStyleClass().add("chart-title");
        weeklyHBox.getChildren().add(weeklyLabel);
        PieChart weeklyPieChart = new PieChart();
        weeklyPieChart.setLabelsVisible(true);
        weeklyPieChart.setLegendVisible(false);
        weeklyVBox.getChildren().addAll(weeklyHBox, weeklyPieChart);
        GridPane.setConstraints(weeklyVBox, 1, 0);

        // Sales per day bar chart
        VBox salesVBox = new VBox(5);
        salesVBox.getStyleClass().add("chart-container");
        HBox salesHBox = new HBox();
        salesHBox.setAlignment(javafx.geometry.Pos.CENTER);
        Label salesLabel = new Label("Ventas Totales por Día");
        salesLabel.getStyleClass().add("chart-title");
        salesHBox.getChildren().add(salesLabel);
        BarChart<String, Number> salesBarChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        salesBarChart.setLegendVisible(false);
        salesVBox.getChildren().addAll(salesHBox, salesBarChart);
        GridPane.setColumnSpan(salesVBox, 2);
        GridPane.setConstraints(salesVBox, 0, 1);

        gridPane.getChildren().addAll(dailyVBox, weeklyVBox, salesVBox);

        // Load data
        loadDailyBurgersChart(dailyPieChart, store.getId());
        loadWeeklyBurgersChart(weeklyPieChart, store.getId());
        loadSalesPerDayChart(salesBarChart, store.getId());

        // Add to container
        VBox storeBox = new VBox(10);
        storeBox.getChildren().addAll(storeLabel, gridPane);
        chartsContainer.getChildren().add(storeBox);
    }

    private void loadDailyBurgersChart(PieChart chart, Long storeId) {
        LocalDate today = LocalDate.now();
        Map<String, Integer> burgerSales = orderRepository.getBurgerSalesForDate(today, storeId);
        chart.getData().clear();
        for (Map.Entry<String, Integer> entry : burgerSales.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            data.setName(entry.getKey() + " (" + entry.getValue() + ")");
            chart.getData().add(data);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, new Tooltip(entry.getKey() + ": " + entry.getValue()));
                }
            });
        }
    }

    private void loadWeeklyBurgersChart(PieChart chart, Long storeId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        Map<String, Integer> burgerSales = orderRepository.getBurgerSalesForDateRange(monday, sunday, storeId);
        chart.getData().clear();
        for (Map.Entry<String, Integer> entry : burgerSales.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            data.setName(entry.getKey() + " (" + entry.getValue() + ")");
            chart.getData().add(data);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, new Tooltip(entry.getKey() + ": " + entry.getValue()));
                }
            });
        }
    }

    private void loadSalesPerDayChart(BarChart<String, Number> chart, Long storeId) {
        LocalDate startDate = LocalDate.now().minusDays(29);
        List<Map<String, Object>> salesData = orderRepository.getSalesPerDay(startDate, LocalDate.now(), storeId);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        chart.getData().clear();

        for (Map<String, Object> data : salesData) {
            String date = (String) data.get("date");
            Number total = (Number) data.get("total");

            String displayDate = formatDateForDisplay(date);

            XYChart.Data<String, Number> chartData = new XYChart.Data<>(displayDate, total);

            chartData.setExtraValue(total);

            series.getData().add(chartData);
        }

        chart.getData().add(series);
        chart.setLegendVisible(false);

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