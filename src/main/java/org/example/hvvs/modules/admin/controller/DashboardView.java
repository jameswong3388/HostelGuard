package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.primefaces.event.CloseEvent;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.dashboard.DashboardModel;
import org.primefaces.model.dashboard.DashboardWidget;
import org.primefaces.model.dashboard.DefaultDashboardModel;
import org.primefaces.model.dashboard.DefaultDashboardWidget;

@Named
@ViewScoped
public class DashboardView implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String RESPONSIVE_CLASS = "col-12 lg:col-6 xl:col-6";

    private DashboardModel responsiveModel;
    private DashboardModel legacyModel;

    @PostConstruct
    public void init() {
        // responsive
        responsiveModel = new DefaultDashboardModel();
        responsiveModel.addWidget(new DefaultDashboardWidget("bar", RESPONSIVE_CLASS));
        responsiveModel.addWidget(new DefaultDashboardWidget("stacked", RESPONSIVE_CLASS));
        responsiveModel.addWidget(new DefaultDashboardWidget("donut", RESPONSIVE_CLASS.replaceFirst("xl:col-\\d+", "xl:col-4")));
        responsiveModel.addWidget(new DefaultDashboardWidget("cartesian", RESPONSIVE_CLASS.replaceFirst("xl:col-\\d+", "xl:col-8")));

        // legacy
        legacyModel = new DefaultDashboardModel();
        legacyModel.addWidget(new DefaultDashboardWidget(Arrays.asList("sports", "finance")));
        legacyModel.addWidget(new DefaultDashboardWidget(Arrays.asList("lifestyle", "weather")));
        legacyModel.addWidget(new DefaultDashboardWidget(List.of("politics")));
    }

    public void handleReorder(DashboardReorderEvent event) {
        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_INFO);
        message.setSummary("Reordered: " + event.getWidgetId());
        String result = String.format("Dragged index: %d, Dropped Index: %d, Widget Index: %d",
                event.getSenderColumnIndex(), event.getColumnIndex(), event.getItemIndex());
        message.setDetail(result);

        addMessage(message);
    }

    public void handleClose(CloseEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Panel Closed",
                "Closed panel ID:'" + event.getComponent().getId() + "'");

        addMessage(message);
    }

    public void handleToggle(ToggleEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Panel Toggled",
                "Toggle panel ID:'" + event.getComponent().getId() + "' Status:" + event.getVisibility().name());

        addMessage(message);
    }

    /**
     * Dashboard panel has been resized.
     *
     * @param widget the DashboardPanel
     * @param size   the new size CSS
     */
    public void onDashboardResize(final String widget, final String size) {
        final DashboardWidget dashboard = responsiveModel.getWidget(widget);
        if (dashboard != null) {
            final String newCss = dashboard.getStyleClass().replaceFirst("xl:col-\\d+", size);
            dashboard.setStyleClass(newCss);
        }
    }

    private void addMessage(FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public DashboardModel getLegacyModel() {
        return legacyModel;
    }

    public DashboardModel getResponsiveModel() {
        return responsiveModel;
    }
}