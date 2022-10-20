package com.harbor.calendly.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "user_settings")
public class UserSettings {
    @Id
    private String id;

    private List<DOWSlot> dowSlots;
    private List<DaySlot> daySlots;
    private boolean state;
    private String userId;
}
