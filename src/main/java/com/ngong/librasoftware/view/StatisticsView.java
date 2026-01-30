package com.ngong.librasoftware.view;


import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.utils.AnimationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Map;

public class StatisticsView extends VBox {

    private final DatabaseService db = new DatabaseService();
    private final VBox chartsContainer = new VBox(30);

    public StatisticsView() {
        this.setPadding(new Insets(30));
        this.setSpacing(30);
        this.getStyleClass().add("statistics-view");

        Label title = new Label("Library Statistics");
        title.setFont(new Font("Matura MT Script Capitals", 20));

//        title.getStyleClass().add("statistics-header");

        refreshCharts();


        AnimationUtils.applyFadeIn(this, 600);
        this.getChildren().addAll(title, chartsContainer);
    }


    private void refreshCharts() {
        chartsContainer.getChildren().clear();
        int male = db.countUnclearedStudentsByGender("Male", null, null, null);
        int female = db.countUnclearedStudentsByGender("Female", null, null, null);
        int total = male + female;

        PieChart genderChart = createPieChart("📚 Gender Distribution", Map.of(
                "Male", male,
                "Female", female
        ), total);

        VBox genderCard = new VBox(genderChart);
        genderCard.getStyleClass().add("chart");

        // === Bar Chart: Active Students by Class ===
        Map<String, Integer> borrowingsByClass = db.getActiveStudentsByClass();

//        Map<String, Integer> borrowingsByClass = db.getActiveStudentsByClass(term, studentClass, month  );

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Class");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Active Students");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("📘 Active Students by Class");
        barChart.setLegendVisible(false);
        barChart.setCategoryGap(20);
        barChart.setBarGap(5);
        barChart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        borrowingsByClass.forEach((className, count) -> {
            String label = String.format("%s (%d)", className, count);
            series.getData().add(new XYChart.Data<>(label, count));
        });
        barChart.getData().add(series);

        chartsContainer.getChildren().addAll(genderCard, barChart);
    }

    private PieChart createPieChart(String title, Map<String, Integer> data, int total) {
        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();

        data.forEach((label, count) -> {
            double percentage = total > 0 ? (count * 100.0 / total) : 0.0;
            String fullLabel = String.format("%s (%d, %.1f%%)", label, count, percentage);
            slices.add(new PieChart.Data(fullLabel, count));
        });

        PieChart chart = new PieChart(slices);
        chart.setTitle(title);
        chart.setLabelsVisible(true);
        chart.setClockwise(true);
        chart.setLegendVisible(false);

        return chart;
    }
}

