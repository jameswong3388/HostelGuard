package org.example.hvvs.converters;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@FacesConverter("sqlDateConverter")
public class SqlDateConverter implements Converter<Date> {

    private static final String PATTERN = "MM/dd/yyyy";
    private final SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);

    @Override
    public Date getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // Parse as java.util.Date
            java.util.Date parsedDate = sdf.parse(value);
            
            // Convert to java.sql.Date (which only maintains the date part, not time)
            return new Date(parsedDate.getTime());
        } catch (ParseException e) {
            throw new ConverterException("Unable to parse date: " + value, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Date value) {
        if (value == null) {
            return "";
        }
        return sdf.format(value);
    }
} 