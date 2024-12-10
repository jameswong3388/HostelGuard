package org.example.hvvs.model;

import org.eclipse.persistence.jpa.jpql.parser.DateTime;

public class ResidentProfile {
    private int user_id;
    private String room_number;
    private String block_number;
    private DateTime updated_at;
    private DateTime created_at;
}
