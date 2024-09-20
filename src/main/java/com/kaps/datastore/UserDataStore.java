package com.kaps.datastore;

import com.kaps.Utility;
import com.kaps.model.Slot;
import com.kaps.model.User;

import java.time.LocalDateTime;
import java.util.*;

public class UserDataStore {
    Map<String, User> userNameToUserMap;

    public static UserDataStore getInstance() {
        return new UserDataStore();
    }

    private UserDataStore() {
        userNameToUserMap = new HashMap<>();
    }

    public User createUser(User user) throws Exception {
        checkIfUserAlreadyExists(user.getName());
        user.setId(Utility.generateUUID());
        userNameToUserMap.put(user.getName(), user);

        return userNameToUserMap.get(user.getName());
    }

    public void deleteUser(String userName) throws Exception {
        checkIfUserExists(userName);
        userNameToUserMap.remove(userName);
    }

    public User addAvailableSlot(String userName, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        checkIfUserExists(userName);
        performSanity(startTime, endTime);

        User user = userNameToUserMap.get(userName);
        user.getAvailableSlots().add(
                new Slot(UUID.randomUUID().toString().replace("-", ""), startTime, endTime));

        return userNameToUserMap.get(userName);
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();

        for (String userName: userNameToUserMap.keySet()) {
            users.add(userNameToUserMap.get(userName));
        }

        return users;
    }

    public User getUserByName(String userName) throws Exception {
        checkIfUserExists(userName);

        return userNameToUserMap.get(userName);
    }

    public List<Slot> getAvailableSlots(String userName) throws Exception {
        checkIfUserExists(userName);

        return new ArrayList<>(userNameToUserMap.get(userName).getAvailableSlots());
    }

    public void removeSlot(String userName, String slotId) throws Exception {
        Set<Slot> slots = userNameToUserMap.get(userName).getAvailableSlots();
        Set<Slot> newSlots = new HashSet<>();

        for (Slot slot: slots) {
            if (!slot.getId().equals(slotId))
                newSlots.add(slot);
        }

        userNameToUserMap.get(userName).setAvailableSlots(newSlots);
    }

    private void checkIfUserExists(String userName) throws Exception {
        if (!userNameToUserMap.containsKey(userName)) {
            throw new Exception("No user exists with userName: " + userName);
        }
    }

    private void checkIfUserAlreadyExists(String userName) throws Exception {
        if (userNameToUserMap.containsKey(userName)) {
            throw new Exception("User already exists with userName: " + userName);
        }
    }

    public void performSanity(LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        if (startTime.isAfter(endTime)) {
            throw new Exception("Meeting start time cannot be after meeting end time!");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new Exception("Cannot schedule an expired meeting!");
        }
    }
}
