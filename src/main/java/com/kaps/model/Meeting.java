package com.kaps.model;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Meeting {
    @Id private String id;
    private String requestor;
    private String requestee;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
