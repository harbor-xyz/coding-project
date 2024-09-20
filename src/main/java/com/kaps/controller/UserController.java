package com.kaps.controller;

import com.kaps.model.User;
import com.kaps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired UserService userService;

    @ResponseBody
    @PostMapping
    public User createUser(@RequestBody User user) throws Exception {
        return userService.createUser(user);
    }

    @ResponseBody
    @GetMapping("/allusers")
    public List<User> getUsers() throws Exception {
        return userService.getUsers();
    }

    @ResponseBody
    @GetMapping
    public User getUser(@RequestParam("userName") String userName) throws Exception {
        return userService.getUserByName(userName);
    }

    @ResponseBody
    @DeleteMapping
    public void deleteUser(@RequestParam("userName") String userName) throws Exception {
        userService.deleteUser(userName);
    }

    @ResponseBody
    @PutMapping
    public User addAvailableSlot(@RequestParam("userName") String userName,
                                  @RequestParam("startTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startTime,
                                  @RequestParam("endTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endTime) throws Exception {
        return userService.addAvailableSlot(userName, startTime, endTime);
    }
}
