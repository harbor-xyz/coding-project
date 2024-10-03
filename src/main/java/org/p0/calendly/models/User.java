package org.p0.calendly.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class User {
    private String id;
    @Setter
    private String firstName;
    @Setter
    private String lastName;
    @Setter
    private String email;

    public void setUniqueId()
    {
        this.id = UUID.randomUUID().toString();
    }
}