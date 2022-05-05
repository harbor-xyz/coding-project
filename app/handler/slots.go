package handler

import (
	"encoding/json"
	"log"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"
	"github.com/jinzhu/gorm"
	"github.com/nikl85/coding-project/app/model"
)

func GetAllSlots(db *gorm.DB, w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)

	userId, _ := strconv.Atoi(vars["id"]) //handle error

	slots := []model.Slot{}

	db.Find(&slots, "user_id = ?", userId)
	respondJSON(w, http.StatusOK, slots)
}

func CreateSlot(db *gorm.DB, w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)

	userId, _ := strconv.Atoi(vars["id"]) //handle error

	log.Println("user:  ", vars["id"])
	slot := model.Slot{UserId: uint(userId)}

	decoder := json.NewDecoder(r.Body)
	if err := decoder.Decode(&slot); err != nil {
		respondError(w, http.StatusBadRequest, err.Error())
		return
	}
	defer r.Body.Close()

	log.Println("data ", slot)
	if err := db.Save(&slot).Error; err != nil {
		respondError(w, http.StatusInternalServerError, err.Error())
		return
	}
	respondJSON(w, http.StatusCreated, slot)
}

func FindOverlap(db *gorm.DB, w http.ResponseWriter, r *http.Request) {

	req := model.OverlapRequest{}

	decoder := json.NewDecoder(r.Body)
	if err := decoder.Decode(&req); err != nil {
		respondError(w, http.StatusBadRequest, err.Error())
		return
	}
	defer r.Body.Close()
	//assume there are only two users in the json for now
	user1 := req.UserId[0]
	user2 := req.UserId[1]

	user1Slots := []model.Slot{}

	user2Slots := []model.Slot{}

	db.Find(&user1Slots, "user_id = ?", user1)
	db.Find(&user2Slots, "user_id = ?", user2)

	commonSlots := findCommonSlots(user1Slots, user2Slots)

	respondJSON(w, http.StatusCreated, commonSlots)
}

func findCommonSlots(user1Slots []model.Slot, user2Slots []model.Slot) model.OverlapResponse {
	commonSlots := model.OverlapResponse{}
	commonSlots.AvailableSlots = make([]string, 0)
	count := 0
	for _, slot1 := range user1Slots {
		for _, slot2 := range user2Slots {
			slot1Start := slot1.StartTime.Unix()
			slot1End := slot1.EndTime.Unix()
			slot2Start := slot2.StartTime.Unix()
			slot2End := slot2.EndTime.Unix()
			if slot2Start >= slot1Start && slot2End > slot1Start && slot2Start <= slot1End && slot2End <= slot1End {
				output := slot2.StartTime.String() + "to" + slot2.EndTime.String()
				commonSlots.AvailableSlots = append(commonSlots.AvailableSlots, output)
				count++
			}
		}
	}
	return commonSlots
}
