package com.tnsoft.web.util;

import com.tnsoft.hibernate.model.TempExpress;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ChartUtil {

    public static BufferedImage getChart(List<TempExpress> tempExpressList, Float maxTemp, Float minTemp, int
            flag) {
        if (null == tempExpressList || tempExpressList.isEmpty()) {
            JFreeChart emptyChart = ChartFactory.createLineChart("No Data!", "", null, null, PlotOrientation.VERTICAL, true, false, false);
            return emptyChart.createBufferedImage(1000, 500, BufferedImage.TYPE_INT_RGB, null);
        }
        String title = "";
//        if (flag == Constants.Version.EXPRESS) {
//            title = "订单温湿度";
//        } else {
//            title = "监测点温湿度";
//        }

        DefaultCategoryDataset tempDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset humidityDataset = new DefaultCategoryDataset();
        String temp = "温度";
        String humidity = "湿度";
        String maxTempStr = "温度上限";
        String minTempStr = "温度下限";
        float tempAxisMax = tempExpressList.get(0).getTemperature();
        float tempAxisMin = tempExpressList.get(0).getTemperature();


        for (TempExpress tempExpress : tempExpressList) {
            tempDataset.addValue(tempExpress.getTemperature(), temp, Utils.SF.format(tempExpress
                    .getCreationTime()));
            if (tempAxisMax < tempExpress.getTemperature()) {
                tempAxisMax = tempExpress.getTemperature();
            }
            if (tempAxisMin > tempExpress.getTemperature()) {
                tempAxisMin = tempExpress.getTemperature();
            }

            humidityDataset.addValue(tempExpress.getHumidity(), humidity, Utils.SF.format(tempExpress
                    .getCreationTime()));
        }

        // create the chart...
        JFreeChart chart = ChartFactory.createLineChart(
                title,        // chart title
                "",               // domain axis label
                temp,                  // range axis label
                tempDataset,                 // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                false,                     // tooltips?
                false                     // URL generator?  Not required...
        );

        Font titleFont = new Font("黑体", Font.BOLD, 20);
        Font simSunFont = new Font("SimSun", Font.BOLD, 16); //

        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        if (tempExpressList.get(0).getHumidity() != 0) {
            plot.setDataset(1, humidityDataset);
            plot.mapDatasetToRangeAxis(1, 1);
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
            ValueAxis axis2 = new NumberAxis(humidity);
            plot.setRangeAxis(1, axis2);

            LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
//        renderer2.setToolTipGenerator(new StandardCategoryToolTipGenerator());
            plot.setRenderer(1, renderer2);
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        }

        // OPTIONAL CUSTOMISATION COMPLETED
        if (null != maxTemp) {
            if (maxTemp > tempAxisMax) {
                tempAxisMax = maxTemp;
            }
            ValueMarker valuemarker = new ValueMarker(maxTemp);  // 水平线的值
            valuemarker.setLabelOffsetType(LengthAdjustmentType.NO_CHANGE);
            valuemarker.setPaint(Color.red);  //线条颜色
            valuemarker.setStroke(new BasicStroke(2.0F, 1, 1, 1.0F, new float[]{25F, 25F}, 0.0F));  //粗细
            valuemarker.setLabel(maxTempStr);   //线条上显示的文本
            valuemarker.setLabelFont(simSunFont); //文本格式
            valuemarker.setLabelPaint(Color.red);
            valuemarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
            valuemarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
            plot.addRangeMarker(valuemarker);
        }
        if (null != minTemp) {
            if (minTemp < tempAxisMin) {
                tempAxisMin = minTemp;
            }
            ValueMarker valuemarker = new ValueMarker(minTemp);  // 水平线的值
            valuemarker.setLabelOffsetType(LengthAdjustmentType.NO_CHANGE);
            valuemarker.setPaint(Color.red);  //线条颜色
            valuemarker.setStroke(new BasicStroke(1.5F, 1, 1, 1.0F, new float[]{25F, 25F}, 0.0F));  //粗细
            valuemarker.setLabel(minTempStr);   //线条上显示的文本
            valuemarker.setLabelFont(simSunFont); //文本格式
            valuemarker.setLabelPaint(Color.red);
            valuemarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
            valuemarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
            plot.addRangeMarker(valuemarker);
        }

        ValueAxis valueAxis = plot.getRangeAxis();
        valueAxis.setLowerBound(tempAxisMin - 10);
        valueAxis.setUpperBound(tempAxisMax + 10);

        TextTitle textTitle = chart.getTitle();
        textTitle.setFont(titleFont);// 为标题设置上字体
        chart.getLegend().setItemFont(simSunFont);// 最下方
        plot.getRangeAxis(0).setLabelFont(simSunFont);
        //文字抗锯齿
        chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        return chart.createBufferedImage(1000, 500, BufferedImage.TYPE_INT_RGB, null);
    }
}
