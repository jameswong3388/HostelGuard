package org.example.hvvs.model;

import org.eclipse.persistence.jpa.jpql.parser.DateTime;

public class VisitRequest {
    private String visit_request_id;
    private String user_id;
    private String verification_code;
    private String visit_date;
    private String visit_time;
    private String purpose;
    private DateTime updated_at;
    private DateTime created_at;
}
