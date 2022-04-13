package mara.mybox.fxml.chart;

import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.controller.Data2DChartController;

/**
 * Reference:
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 * By Roland
 *
 * @Author Mara
 * @License Apache License Version 2.0
 */
public class LabeledBarChart<X, Y> extends BarChart<X, Y> {

    protected Data2DChartController chartController;
    protected ChartOptions<X, Y> options;

    public LabeledBarChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    public final void init() {
        this.setLegendSide(Side.TOP);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        options = new ChartOptions<>(this);
    }

    public LabeledBarChart setChartController(Data2DChartController chartController) {
        this.chartController = chartController;
        options = new ChartOptions<>(chartController);
        return this;
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        options.makeLabels(series, getPlotChildren());
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        options.removeLabels(series, getPlotChildren());
        super.seriesRemoved(series);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        options.displayLabels();
    }

    public LabeledBarChart<X, Y> setLabelType(ChartTools.LabelType labelType) {
        options.setLabelType(labelType);
        return this;
    }

    public LabeledBarChart<X, Y> setLabelFontSize(int labelFontSize) {
        options.setLabelFontSize(labelFontSize);
        return this;
    }

}
