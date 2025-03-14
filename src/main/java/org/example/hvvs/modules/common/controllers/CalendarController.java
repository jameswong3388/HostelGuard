package org.example.hvvs.modules.common.controllers;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.example.hvvs.model.CalendarEvents;
import org.example.hvvs.model.CalendarEventsFacade;
import org.example.hvvs.model.Users;
import org.example.hvvs.utils.CommonParam;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleDisplayMode;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

@Named
@ViewScoped
public class CalendarController implements Serializable {

    @Inject
    private CalendarEventsFacade eventsFacade;
    
    private ScheduleModel eventModel;

    private ScheduleModel lazyEventModel;

    private ScheduleEvent<?> event = new DefaultScheduleEvent<>();

    private boolean slotEventOverlap = true;
    private boolean showWeekNumbers = false;
    private boolean showHeader = true;
    private boolean draggable = false; // Set to false for read-only
    private boolean resizable = false; // Set to false for read-only
    private boolean selectable = false;
    private boolean showWeekends = true;
    private boolean tooltip = true;
    private boolean allDaySlot = true;
    private boolean rtl = false;

    private double aspectRatio = Double.MIN_VALUE;

    private String leftHeaderTemplate = "prev,next today";
    private String centerHeaderTemplate = "title";
    private String rightHeaderTemplate = "dayGridMonth,timeGridWeek,timeGridDay,listYear";
    private String nextDayThreshold = "09:00:00";
    private String weekNumberCalculation = "local";
    private String weekNumberCalculator = "date.getTime()";
    private String displayEventEnd;
    private String timeFormat;
    private String slotDuration = "00:30:00";
    private String slotLabelInterval;
    private String slotLabelFormat = "HH:mm";
    private String scrollTime = "06:00:00";
    private String minTime = "04:00:00";
    private String maxTime = "20:00:00";
    private String locale = "en";
    private String serverTimeZone = ZoneId.systemDefault().toString();
    private String timeZone = "";
    private String clientTimeZone = "local";
    private String columnHeaderFormat = "";
    private String view = "timeGridWeek";
    private String height = "auto";

    @PostConstruct
    public void init() {
        eventModel = new DefaultScheduleModel();
        
        // Initialize with database events
        loadDatabaseEvents();

        lazyEventModel = new LazyScheduleModel() {
            @Override
            public void loadEvents(LocalDateTime start, LocalDateTime end) {
                // Load events from the database that fall within the requested date range
                loadEventsFromDatabase(start, end);
            }
        };
    }
    
    /**
     * Loads all events from the database into the event model
     */
    private void loadDatabaseEvents() {
        // Get current user from session
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return; // Do not load any events if user is not authenticated
        }
        
        List<CalendarEvents> events = eventsFacade.findByUser(currentUser);
        for (CalendarEvents dbEvent : events) {
            addDatabaseEventToModel(dbEvent, eventModel);
        }
    }
    
    /**
     * Loads events from database within a specific date range for the lazy loading model
     */
    private void loadEventsFromDatabase(LocalDateTime start, LocalDateTime end) {
        // Get current user from session
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return; // Do not load any events if user is not authenticated
        }
        
        // Query database for events for the current user
        List<CalendarEvents> events = eventsFacade.findByUser(currentUser);
        
        for (CalendarEvents dbEvent : events) {
            // Only add events within the requested range
            if (isEventInRange(dbEvent, start, end)) {
                addDatabaseEventToModel(dbEvent, lazyEventModel);
            }
        }
    }
    
    /**
     * Get the current user from the session
     */
    private Users getCurrentUser() {
        return (Users) FacesContext.getCurrentInstance()
            .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
    }
    
    /**
     * Checks if an event falls within the specified date range
     */
    private boolean isEventInRange(CalendarEvents dbEvent, LocalDateTime start, LocalDateTime end) {
        return (dbEvent.getStartDate().isEqual(start) || dbEvent.getStartDate().isAfter(start)) && 
               (dbEvent.getEndDate().isEqual(end) || dbEvent.getEndDate().isBefore(end)) ||
               (dbEvent.getStartDate().isBefore(start) && dbEvent.getEndDate().isAfter(start));
    }
    
    /**
     * Converts a database event to a schedule event and adds it to the model
     */
    private void addDatabaseEventToModel(CalendarEvents dbEvent, ScheduleModel model) {
        DefaultScheduleEvent.Builder eventBuilder = DefaultScheduleEvent.builder()
                .title(dbEvent.getTitle())
                .startDate(dbEvent.getStartDate())
                .endDate(dbEvent.getEndDate())
                .description(dbEvent.getDescription())
                .allDay(dbEvent.getAllDay())
                .data(dbEvent.getId()); // Store the database ID
                
        if (dbEvent.getUrl() != null) {
            eventBuilder.url(dbEvent.getUrl());
        }
        
        if (dbEvent.getBorderColor() != null) {
            eventBuilder.borderColor(dbEvent.getBorderColor());
        }
        
        if (dbEvent.getBackgroundColor() != null) {
            eventBuilder.backgroundColor(dbEvent.getBackgroundColor());
        }
        
        if (dbEvent.getDisplayMode() != null) {
            switch (dbEvent.getDisplayMode()) {
                case BACKGROUND:
                    eventBuilder.display(ScheduleDisplayMode.BACKGROUND);
                    break;
                case INVERSE:
                    eventBuilder.display(ScheduleDisplayMode.INVERSE_BACKGROUND);
                    break;
                default:
                    eventBuilder.display(ScheduleDisplayMode.AUTO);
            }
        }
        
        // Make events non-editable for read-only view
        eventBuilder.editable(false);
        DefaultScheduleEvent<?> scheduleEvent = eventBuilder.build();
        model.addEvent(scheduleEvent);
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public ScheduleModel getLazyEventModel() {
        return lazyEventModel;
    }

    public LocalDate getInitialDate() {
        return LocalDate.now();
    }

    public ScheduleEvent<?> getEvent() {
        return event;
    }

    public void setEvent(ScheduleEvent<?> event) {
        this.event = event;
    }

    public void onEventSelect(SelectEvent<ScheduleEvent<?>> selectEvent) {
        event = selectEvent.getObject();
    }

    public void onViewChange(SelectEvent<String> selectEvent) {
        view = selectEvent.getObject();
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "View Changed", "View:" + view);
        addMessage(message);
    }

    private void addMessage(FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    // Keep only the getter methods needed for the UI properties

    public boolean isShowWeekends() {
        return showWeekends;
    }

    public boolean isSlotEventOverlap() {
        return slotEventOverlap;
    }

    public boolean isShowWeekNumbers() {
        return showWeekNumbers;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public boolean isResizable() {
        return resizable;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isTooltip() {
        return tooltip;
    }

    public boolean isRtl() {
        return rtl;
    }

    public boolean isAllDaySlot() {
        return allDaySlot;
    }

    public double getAspectRatio() {
        return aspectRatio == 0 ? Double.MIN_VALUE : aspectRatio;
    }

    public String getLeftHeaderTemplate() {
        return leftHeaderTemplate;
    }

    public String getCenterHeaderTemplate() {
        return centerHeaderTemplate;
    }

    public String getRightHeaderTemplate() {
        return rightHeaderTemplate;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getSlotDuration() {
        return slotDuration;
    }

    public String getLocale() {
        return locale;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getClientTimeZone() {
        return clientTimeZone;
    }

    public String getHeight() {
        return height;
    }

    public String getServerTimeZone() {
        return serverTimeZone;
    }
}