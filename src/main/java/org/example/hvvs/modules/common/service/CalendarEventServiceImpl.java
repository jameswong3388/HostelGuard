package org.example.hvvs.modules.common.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.example.hvvs.model.CalendarEvents;
import org.example.hvvs.model.CalendarEventsFacade;
import org.example.hvvs.model.Users;

/**
 * Implementation of the CalendarEventService interface.
 * Provides methods for other classes to manage calendar events programmatically.
 */
@Stateless
public class CalendarEventServiceImpl implements CalendarEventService {

    @Inject
    private CalendarEventsFacade eventsFacade;

    @Override
    public CalendarEvents addEvent(Users user, String title, LocalDateTime startDate, LocalDateTime endDate,
                                   String description, boolean allDay) {
        return addEvent(user, title, startDate, endDate, description, allDay, null, null, null);
    }

    @Override
    public CalendarEvents addEvent(Users user, String title, LocalDateTime startDate, LocalDateTime endDate,
                                   String description, boolean allDay, String backgroundColor,
                                   String borderColor, String url) {
        if (user == null || title == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("User, title, start date, and end date are required");
        }

        // Create a new event
        CalendarEvents event = new CalendarEvents();
        event.setUser(user);
        event.setTitle(title);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setDescription(description);
        event.setAllDay(allDay);
        event.setBackgroundColor(backgroundColor);
        event.setBorderColor(borderColor);
        event.setUrl(url);
        event.setTimeZone(ZoneId.systemDefault().toString());
        event.setStatus(CalendarEvents.Status.ACTIVE);

        // Save the event to the database
        eventsFacade.create(event);

        return event;
    }

    @Override
    public CalendarEvents updateEvent(Integer eventId, String title, LocalDateTime startDate,
                                      LocalDateTime endDate, String description, Boolean allDay) {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID is required");
        }

        CalendarEvents event = eventsFacade.find(eventId);
        if (event == null) {
            return null;
        }

        // Update only the fields that are provided (not null)
        if (title != null) {
            event.setTitle(title);
        }

        if (startDate != null) {
            event.setStartDate(startDate);
        }

        if (endDate != null) {
            event.setEndDate(endDate);
        }

        if (description != null) {
            event.setDescription(description);
        }

        if (allDay != null) {
            event.setAllDay(allDay);
        }

        // Update the event in the database
        eventsFacade.edit(event);

        return event;
    }

    @Override
    public boolean deleteEvent(Integer eventId) {
        if (eventId == null) {
            return false;
        }

        CalendarEvents event = eventsFacade.find(eventId);
        if (event == null) {
            return false;
        }

        // Delete the event from the database
        eventsFacade.remove(event);

        return true;
    }

    @Override
    public CalendarEvents findEvent(Integer eventId) {
        if (eventId == null) {
            return null;
        }

        return eventsFacade.find(eventId);
    }

    @Override
    public List<CalendarEvents> getEventsByUser(Users user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        return eventsFacade.findByNamedQuery("CalendarEvents.findByUser", "user", user);
    }

    @Override
    public List<CalendarEvents> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }

        // This would ideally use a custom query in the facade
        // For now, we'll get all events and filter them
        List<CalendarEvents> allEvents = eventsFacade.findAll();

        return allEvents.stream()
                .filter(event -> isEventInRange(event, start, end))
                .toList();
    }

    /**
     * Checks if an event falls within the specified date range
     */
    private boolean isEventInRange(CalendarEvents event, LocalDateTime start, LocalDateTime end) {
        return (event.getStartDate().isEqual(start) || event.getStartDate().isAfter(start)) &&
                (event.getEndDate().isEqual(end) || event.getEndDate().isBefore(end)) ||
                (event.getStartDate().isBefore(start) && event.getEndDate().isAfter(start));
    }
}