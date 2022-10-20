package com.harbor.calendly.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserInformation {
    private String name;
    private String email;
    private String mobileNumber;
}
