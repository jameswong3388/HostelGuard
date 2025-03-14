package org.example.hvvs.modules.common.service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.hvvs.model.CalendarEvents;
import org.example.hvvs.model.Users;

/**
 * Service for managing calendar events programmatically.
 * This allows other classes to add/update/delete events while keeping the calendar UI read-only.
 */
public interface CalendarEventService {
    
    /**
     * Adds a new calendar event
     * 
     * @param user The user associated with the event
     * @param title Event title (required)
     * @param startDate Event start date (required)
     * @param endDate Event end date (required)
     * @param description Event description (optional)
     * @param allDay Whether the event is an all-day event
     * @return The created calendar event
     */
    CalendarEvents addEvent(Users user, String title, LocalDateTime startDate, LocalDateTime endDate, 
                           String description, boolean allDay);
    
    /**
     * Adds a new calendar event with additional styling options
     * 
     * @param user The user associated with the event
     * @param title Event title
     * @param startDate Event start date
     * @param endDate Event end date
     * @param description Event description
     * @param allDay Whether the event is an all-day event
     * @param backgroundColor Background color (CSS color)
     * @param borderColor Border color (CSS color)
     * @param url Optional URL to navigate to when clicking the event
     * @return The created calendar event
     */
    CalendarEvents addEvent(Users user, String title, LocalDateTime startDate, LocalDateTime endDate, 
                           String description, boolean allDay, String backgroundColor, String borderColor, String url);
    
    /**
     * Updates an existing calendar event
     * 
     * @param eventId ID of the event to update
     * @param title New title (or null to keep existing)
     * @param startDate New start date (or null to keep existing)
     * @param endDate New end date (or null to keep existing)
     * @param description New description (or null to keep existing)
     * @param allDay New all-day flag (or null to keep existing)
     * @return The updated event, or null if event not found
     */
    CalendarEvents updateEvent(Integer eventId, String title, LocalDateTime startDate, 
                              LocalDateTime endDate, String description, Boolean allDay);
    
    /**
     * Deletes a calendar event
     * 
     * @param eventId ID of the event to delete
     * @return true if successfully deleted, false otherwise
     */
    boolean deleteEvent(Integer eventId);
    
    /**
     * Finds an event by its ID
     * 
     * @param eventId Event ID
     * @return The event or null if not found
     */
    CalendarEvents findEvent(Integer eventId);
    
    /**
     * Gets all events for a specific user
     * 
     * @param user The user
     * @return List of calendar events
     */
    List<CalendarEvents> getEventsByUser(Users user);
    
    /**
     * Gets all events within a date range
     * 
     * @param start Start date
     * @param end End date
     * @return List of calendar events in the range
     */
    List<CalendarEvents> getEventsByDateRange(LocalDateTime start, LocalDateTime end);
}
