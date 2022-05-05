package main

import (
	"github.com/nikl85/coding-project/app"
	"github.com/nikl85/coding-project/config"
)

func main() {
	config := config.GetConfig()

	app := &app.App{}
	app.Initialize(config)
	app.Run(":3000")
}
