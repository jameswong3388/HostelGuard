package org.example.hvvs.model;

import org.eclipse.persistence.jpa.jpql.parser.DateTime;

public class RequestStatus {
    private int status_id;
    private int request_id;
    private int updated_by_user_id;
    private String status;
    private String remarks;
    private DateTime updated_at;
    private DateTime created_at;
}
