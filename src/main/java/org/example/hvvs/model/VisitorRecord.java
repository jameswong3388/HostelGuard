package org.example.hvvs.model;

import org.eclipse.persistence.jpa.jpql.parser.DateTime;

public class VisitorRecord {
    private int record_id;
    private int request_id;
    private int security_staff_id;
    private String visitor_name;
    private String visitor_ic;
    private String visitor_phone;
    private DateTime check_in_time;
    private DateTime check_out_time;
    private String remarks;
    private DateTime created_at;
    private DateTime updated_at;
}
