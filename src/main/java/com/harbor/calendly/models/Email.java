package com.harbor.calendly.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Email {

    // Class data members
    private String recipient;
    private String msgBody;
    private String subject;
    private String attachment;
}
