package mara.mybox.calculation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * @Author Mara
 * @CreateDate 2022-5-4
 * @License Apache License Version 2.0
 *
 * To accumulate the progress based on saved values.
 */
public class SimpleLinearRegression extends SimpleRegression {

    protected String xName, yName;
    protected List<String> lastData;
    protected List<Data2DColumn> columns;
    protected int scale = 8;

    public SimpleLinearRegression(boolean includeIntercept, String xName, String yName, int scale) {
        super(includeIntercept);
        this.xName = xName;
        this.yName = yName;
        this.scale = scale;
        makeColumns();
    }

    private List<Data2DColumn> makeColumns() {
        columns = new ArrayList<>();
        columns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(message("NumberOfObservations"), ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(xName, ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(yName, ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("Slope"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("Intercept"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("CoefficientOfDetermination"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("PearsonsR"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("MeanSquareError"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("SumSquaredErrors"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("TotalSumSquares"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("SumSquaredRegression"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("StandardErrorOfSlope"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("SignificanceLevelSlopeCorrelation"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("HalfWidthConfidenceIntervalOfSlope"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(message("StandardErrorOfIntercept"), ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn("SumOfCrossProducts", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn("XSumSquares", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn("Xbar", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn("SumX", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn("Ybar", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn("SumY", ColumnDefinition.ColumnType.Double));
        return columns;
    }

    public List<String> addData(long rowIndex, final double x, final double y) {
        super.addData(x, y);
        lastData = new ArrayList<>();
        lastData.add(rowIndex + "");
        lastData.add(getN() + "");
        lastData.add(DoubleTools.format(x, scale));
        lastData.add(DoubleTools.format(y, scale));
        lastData.add(DoubleTools.format(getSlope(), scale));
        lastData.add(DoubleTools.format(getIntercept(), scale));
        lastData.add(DoubleTools.format(getRSquare(), scale));
        lastData.add(DoubleTools.format(getR(), scale));
        lastData.add(DoubleTools.format(getMeanSquareError(), scale));
        lastData.add(DoubleTools.format(getSumSquaredErrors(), scale));
        lastData.add(DoubleTools.format(getTotalSumSquares(), scale));
        lastData.add(DoubleTools.format(getRegressionSumSquares(), scale));
        lastData.add(DoubleTools.format(getSlopeStdErr(), scale));
        lastData.add(DoubleTools.format(getSignificance(), scale));
        lastData.add(DoubleTools.format(getSlopeConfidenceInterval(), scale));
        lastData.add(DoubleTools.format(getInterceptStdErr(), scale));
        lastData.add(DoubleTools.format(getSumOfCrossProducts(), scale));
        lastData.add(DoubleTools.format(getSumOfCrossProducts(), scale));
        lastData.add(DoubleTools.format(getXSumSquares(), scale));

        try {
            Class superClass = getClass().getSuperclass();

            Field xbar = superClass.getDeclaredField("xbar");
            xbar.setAccessible(true);
            lastData.add(DoubleTools.format((double) xbar.get(this), scale));

            Field sumX = superClass.getDeclaredField("sumX");
            sumX.setAccessible(true);
            lastData.add(DoubleTools.format((double) sumX.get(this), scale));

            Field ybar = superClass.getDeclaredField("ybar");
            ybar.setAccessible(true);
            lastData.add(DoubleTools.format((double) ybar.get(this), scale));

            Field sumY = superClass.getDeclaredField("sumY");
            sumY.setAccessible(true);
            lastData.add(DoubleTools.format((double) sumY.get(this), scale));

        } catch (Exception e) {
            MyBoxLog.console(e);
        }

        return lastData;
    }

    /*
        get/set
     */
    public String getxName() {
        return xName;
    }

    public SimpleLinearRegression setxName(String xName) {
        this.xName = xName;
        return this;
    }

    public String getyName() {
        return yName;
    }

    public SimpleLinearRegression setyName(String yName) {
        this.yName = yName;
        return this;
    }

    public List<String> getLastData() {
        return lastData;
    }

    public List<Data2DColumn> getColumns() {
        return columns;
    }

    public int getScale() {
        return scale;
    }

    public SimpleLinearRegression setScale(int scale) {
        this.scale = scale;
        return this;
    }

}