package org.example.hvvs.model;

import org.eclipse.persistence.jpa.jpql.parser.DateTime;

public class SecurityStaffProfile {
    private int user_id;
    private String badge_number;
    private String shift;
    private DateTime updated_at;
    private DateTime created_at;
}
