package model

import (
	"time"

	"github.com/jinzhu/gorm"
	_ "github.com/jinzhu/gorm/dialects/mysql"
)

type Slot struct {
	gorm.Model
	UserId    uint       `gorm:"index" json:"user_id"`
	StartTime *time.Time `gorm:"default:null" json:"start_time"`
	EndTime   *time.Time `gorm:"default:null" json:"end_time"`
}

type OverlapRequest struct {
	UserId []uint `json:"user_ids"`
}

type OverlapResponse struct {
	AvailableSlots []string `json:"available_slots"`
}

// DBMigrate will create and migrate the tables, and then make the some relationships if necessary
func DBMigrate(db *gorm.DB) *gorm.DB {
	db.AutoMigrate(&Slot{})
	return db
}
