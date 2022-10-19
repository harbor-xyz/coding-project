package com.harbor.calendly.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "users")
@Getter
@Setter
@ToString
public class User {
    @Id
    private String userId;

    private String name;
    private String email;
    private String mobileNumber;
    private Boolean active;
    private Long lastAccessDate;
    private Long createdDate;

    public User() {
        this.active = Boolean.TRUE;
        this.createdDate = System.currentTimeMillis();
    }
}
