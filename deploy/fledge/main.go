package main

import (
	"context"
	dapr "github.com/dapr/go-sdk/client"
	"github.com/gin-gonic/gin"
	_ "github.com/marmotedu/gopractise-demo/swagger/docs"
	"github.com/robfig/cron"
	"github.com/runmark/fledge/api"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/schema"
	"log"
	"net/http"
)

const (
	COMPONENT_PUBSUB_NAME      = "pubsub"
	COMPONENT_STATE_STORE_NAME = "statestore"
	DEFAULT_NAMESPACE          = "default"
	DEFAULT_PARTY_ID           = "0000"
	POD_NAMESPACE              = "POD_NAMESPACE"
)

var DB *gorm.DB

func init() {

	var err error
	log.SetFlags(log.Lshortfile)

	fateFlowDsn := "fate:fate_dev@tcp(127.0.0.1:3307)/fate_flow?charset=utf8mb4&parseTime=True&loc=Local"
	DB, err = gorm.Open(mysql.Open(fateFlowDsn), &gorm.Config{
		PrepareStmt: true,
		//Logger: logger.Default.LogMode(logger.Info),
		NamingStrategy: schema.NamingStrategy{
			SingularTable: true,
		},
	})
	if err != nil {
		log.Panicln("failed to connect database: ", DB)
	}
}



func SetupRouter(daprClient dapr.Client) *gin.Engine {

	r := gin.Default()
	r.Use(daprer(daprClient))

	r.GET("/ping", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"message": "pong"})
	})

	r.POST("/edge-data-summary", api.EdgeDataSummary)

	return r
}

func daprer(client dapr.Client) gin.HandlerFunc {

	return func(c *gin.Context) {
		c.Set("dapr", client)
		c.Next()
	}
}

func main() {

	daprClient, err := dapr.NewClient()
	if err != nil {
		panic(err)
	}
	defer daprClient.Close()

	ctx := context.Background()

	c := cron.New()

	// err = c.AddFunc("0,30 * * * * *", func() {
	// 	NewFlParty().pub(ctx, daprClient)
	// })
	// if err != nil {
	// 	panic(err)
	// }

	err = c.AddFunc("15,45 * * * * *", func() {
		pubUpdatedModels(ctx, daprClient)
	})
	if err != nil {
		panic(err)
	}

	err = c.AddFunc("15,45 * * * * *", func() {
		pubUpdatedJobs(ctx, daprClient)
	})
	if err != nil {
		panic(err)
	}

	c.Start()

	r := SetupRouter(daprClient)
	r.Run("0.0.0.0:50010")

	//select {}
	//for {}
}

// 主要测试：
// 1. statestore 中的 key 为空时，udpate_time 是否为 0
// 2. 整个逻辑是否正确，即从数据库获取未更新的model info；发送到dapr事件管道中；；；；；Spring能否接收到正常的数据并解析后存储
