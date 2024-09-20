package com.kaps.controller;

import com.kaps.model.Meeting;
import com.kaps.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/meeting")
public class MeetingController {
    @Autowired MeetingService meetingService;

    @ResponseBody
    @PostMapping("schedule")
    public Meeting scheduleMeeting(@RequestParam("requestor") String requestor,
                                   @RequestParam("requestee") String requestee,
                                   @RequestParam("startTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startTime,
                                   @RequestParam("endTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endTime) throws Exception {
        return meetingService.scheduleMeeting(requestor, requestee, startTime, endTime);
    }
}
