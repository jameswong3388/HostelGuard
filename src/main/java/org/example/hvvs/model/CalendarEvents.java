package org.example.hvvs.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "calendar_events")
@NamedQueries({
        @NamedQuery(name = "CalendarEvents.findAll", query = "SELECT c FROM CalendarEvents c"),
        @NamedQuery(name = "CalendarEvents.findByUser", query = "SELECT c FROM CalendarEvents c WHERE c.user = :user"),
        @NamedQuery(name = "CalendarEvents.findByTitle", query = "SELECT c FROM CalendarEvents c WHERE c.title = :title"),
        @NamedQuery(name = "CalendarEvents.findByStartDate", query = "SELECT c FROM CalendarEvents c WHERE c.startDate = :startDate"),
        @NamedQuery(name = "CalendarEvents.findByEndDate", query = "SELECT c FROM CalendarEvents c WHERE c.endDate = :endDate"),
        @NamedQuery(name = "CalendarEvents.findByStatus", query = "SELECT c FROM CalendarEvents c WHERE c.status = :status"),
        @NamedQuery(name = "CalendarEvents.findByCreatedAt", query = "SELECT c FROM CalendarEvents c WHERE c.createdAt = :createdAt")
})
public class CalendarEvents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "all_day")
    private Boolean allDay = false;

    private String url;

    @Column(name = "border_color", length = 20)
    private String borderColor;

    @Column(name = "background_color", length = 20)
    private String backgroundColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_mode")
    private DisplayMode displayMode = DisplayMode.NORMAL;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern")
    private RecurrencePattern recurrencePattern = RecurrencePattern.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_event_id")
    private CalendarEvents parentEvent;

    @Column(name = "time_zone", nullable = false)
    private String timeZone = ZoneId.systemDefault().getId();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CalendarEvents() {
        super();
    }

    public CalendarEvents(Users user, String title, String description, LocalDateTime startDate,
                         LocalDateTime endDate, Boolean allDay, String url, String borderColor,
                         String backgroundColor, DisplayMode displayMode, Status status,
                         RecurrencePattern recurrencePattern, CalendarEvents parentEvent,
                         String timeZone) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allDay = allDay;
        this.url = url;
        this.borderColor = borderColor;
        this.backgroundColor = backgroundColor;
        this.displayMode = displayMode;
        this.status = status;
        this.recurrencePattern = recurrencePattern;
        this.parentEvent = parentEvent;
        this.timeZone = timeZone;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum for display mode
    public enum DisplayMode {
        BACKGROUND, INVERSE, NORMAL
    }

    // Enum for status
    public enum Status {
        ACTIVE, CANCELLED, COMPLETED
    }

    // Enum for recurrence pattern
    public enum RecurrencePattern {
        NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public CalendarEvents getParentEvent() {
        return parentEvent;
    }

    public void setParentEvent(CalendarEvents parentEvent) {
        this.parentEvent = parentEvent;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 