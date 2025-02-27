package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.LinkedHashMap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.model.VisitRequests.VisitStatus;

import software.xdev.chartjs.model.charts.BarChart;
import software.xdev.chartjs.model.color.RGBAColor;
import software.xdev.chartjs.model.data.BarData;
import software.xdev.chartjs.model.dataset.BarDataset;
import software.xdev.chartjs.model.enums.IndexAxis;
import software.xdev.chartjs.model.options.BarOptions;
import software.xdev.chartjs.model.options.scale.Scales;
import software.xdev.chartjs.model.options.scale.cartesian.CartesianScaleOptions;
import software.xdev.chartjs.model.options.scale.cartesian.CartesianTickOptions;

@Named
@RequestScoped
public class ChartView implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String barModel;

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @PostConstruct
    public void init() {
        createBarModel();
    }


    /**
     * Creates a bar chart showing visit requests by status
     */
    public void createBarModel() {
        // Get counts for each visit request status
        Map<VisitStatus, Integer> statusCounts = new LinkedHashMap<>();
        for (VisitStatus status : VisitStatus.values()) {
            String query = "SELECT COUNT(v) FROM VisitRequests v WHERE v.status = :status";
            TypedQuery<Long> typedQuery = em.createQuery(query, Long.class)
                    .setParameter("status", status);
            Long count = typedQuery.getSingleResult();
            statusCounts.put(status, count.intValue());
        }

        // Create labels and data arrays
        String[] labels = new String[statusCounts.size()];
        Number[] counts = new Number[statusCounts.size()];

        int i = 0;
        for (Map.Entry<VisitStatus, Integer> entry : statusCounts.entrySet()) {
            labels[i] = formatStatus(entry.getKey().name());
            counts[i] = entry.getValue();
            i++;
        }

        barModel = new BarChart()
                .setData(new BarData()
                        .addDataset(new BarDataset()
                                .setData(counts)
                                .setLabel("All Status")
                                .setBackgroundColor(new RGBAColor(54, 162, 235, 0.5))
                                .setBorderColor(new RGBAColor(54, 162, 235))
                                .setBorderWidth(1))
                        .setLabels(labels))
                .setOptions(new BarOptions()
                        .setResponsive(true)
                        .setMaintainAspectRatio(false)
                        .setIndexAxis(IndexAxis.X)
                        .setScales(new Scales().addScale(Scales.ScaleAxis.Y, new CartesianScaleOptions()
                                .setStacked(false)
                                .setTicks(new CartesianTickOptions()
                                        .setAutoSkip(true)
                                        .setMirror(true)))
                        )
                ).toJson();
    }

    /**
     * Format status string from UPPERCASE_WITH_UNDERSCORES to Title Case With Spaces
     */
    private String formatStatus(String status) {
        String[] words = status.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase());
        }

        return formatted.toString();
    }

    public String getBarModel() {
        return barModel;
    }

    public void setBarModel(String barModel) {
        this.barModel = barModel;
    }

}