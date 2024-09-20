package com.kaps.service;

import com.kaps.datastore.UserDataStore;
import com.kaps.model.Slot;
import com.kaps.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    UserDataStore userDataStore;

    public UserService() {
        userDataStore = UserDataStore.getInstance();
    }

    public User createUser(User user) throws Exception {
        return userDataStore.createUser(user);
    }

    public void deleteUser(String userName) throws Exception {
        userDataStore.deleteUser(userName);
    }

    public User addAvailableSlot(String userName, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        return userDataStore.addAvailableSlot(userName, startTime, endTime);
    }

    public List<User> getUsers() {
        return userDataStore.getUsers();
    }

    public User getUserByName(String userName) throws Exception {
        return userDataStore.getUserByName(userName);
    }

    public boolean isSlotAvailable(String userName, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        for (Slot slot: userDataStore.getAvailableSlots(userName)) {
            if (slot.getStartTime().equals(startTime) && slot.getEndTime().equals(endTime))
                return true;
        }

        return false;
    }

    public String getSlotId(String userName, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        for (Slot slot: userDataStore.getAvailableSlots(userName)) {
            if (slot.getStartTime().equals(startTime) && slot.getEndTime().equals(endTime))
                return slot.getId();
        }

        return "";
    }

    public void removeSlot(String userName, String slotId) throws Exception {
        userDataStore.removeSlot(userName, slotId);
    }

    public Slot getOverlappingSlots(String userName, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        List<Slot> slots = userDataStore.getAvailableSlots(userName);

        for (Slot slot: slots) {


            if (slot.getStartTime().equals(startTime) && slot.getEndTime().equals(endTime))
                return slot;
            else if (slot.getStartTime().isBefore(startTime) && (slot.getEndTime().equals(endTime) || slot.getEndTime().isAfter(endTime)))
                return slot;
        }

        return new Slot();
    }
}
