package org.example.hvvs.converters;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@FacesConverter("timestampConverter")
public class TimestampConverter implements Converter<Timestamp> {

    private static final String PATTERN = "MM/dd/yy HH:mm";
    private final SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);

    @Override
    public Timestamp getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // parse as java.util.Date
            java.util.Date parsedDate = sdf.parse(value);
            // convert to java.sql.Timestamp
            return new Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            throw new ConverterException("Unable to parse date/time: " + value, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Timestamp value) {
        if (value == null) {
            return "";
        }
        return sdf.format(value);
    }
}
