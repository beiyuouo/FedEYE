package api

import (
	"context"
	"encoding/json"
	dapr "github.com/dapr/go-sdk/client"
	"github.com/gin-gonic/gin"
	"log"
	"net/http"
)

type DataSummary struct {
	CreateParty string `json:"create_party"`
	Path        string `json:"path"`
	ColumnType  string `json:"column_type"`
	Columns     string `json:"columns"`
}

func EdgeDataSummary(c *gin.Context) {

	var newDataSummary DataSummary
	err := c.BindJSON(&newDataSummary)
	if err != nil {
		log.Fatal(err)
		c.JSON(http.StatusBadRequest, err.Error())
		return
	}

	// send to cloud use dapr
	daprClient := c.MustGet("dapr").(dapr.Client)
	datas, err := json.Marshal(newDataSummary)
	if err != nil {
		log.Panic(err)
	}

	err = daprClient.PublishEvent(context.Background(), "pubsub", "edge-data-summary", datas)
	if err != nil {
		log.Panic(err)
	}

	//log.Println(newDataSummary)
	c.JSON(http.StatusOK, newDataSummary)
}