package main

import (
	"context"
	"encoding/binary"
	"encoding/json"
	"log"
	"os"
	"time"

	dapr "github.com/dapr/go-sdk/client"
)

const FL_MODEL_INFO_TOPIC = "fl-model-info"
const FL_MODEL_UPDATE_TIME = "fl-model-update-time"

type TMachineLearningModelInfo struct {
	FCreateTime         int64     `gorm:"column:f_create_time" db:"f_create_time" json:"f_create_time" form:"f_create_time"`
	FCreateDate         time.Time `gorm:"column:f_create_date" db:"f_create_date" json:"f_create_date" form:"f_create_date"`
	FUpdateTime         int64     `gorm:"column:f_update_time" db:"f_update_time" json:"f_update_time" form:"f_update_time"`
	FUpdateDate         time.Time `gorm:"column:f_update_date" db:"f_update_date" json:"f_update_date" form:"f_update_date"`
	FRole               string    `gorm:"column:f_role" db:"f_role" json:"f_role" form:"f_role"`
	FPartyId            string    `gorm:"column:f_party_id" db:"f_party_id" json:"f_party_id" form:"f_party_id"`
	FRoles              string    `gorm:"column:f_roles" db:"f_roles" json:"f_roles" form:"f_roles"`
	FJobId              string    `gorm:"column:f_job_id" db:"f_job_id" json:"f_job_id" form:"f_job_id"`
	FModelId            string    `gorm:"column:f_model_id" db:"f_model_id" json:"f_model_id" form:"f_model_id"`
	FModelVersion       string    `gorm:"column:f_model_version" db:"f_model_version" json:"f_model_version" form:"f_model_version"`
	FLoadedTimes        int       `gorm:"column:f_loaded_times" db:"f_loaded_times" json:"f_loaded_times" form:"f_loaded_times"`
	FSize               int64     `gorm:"column:f_size" db:"f_size" json:"f_size" form:"f_size"`
	FDescription        string    `gorm:"column:f_description" db:"f_description" json:"f_description" form:"f_description"`
	FInitiatorRole      string    `gorm:"column:f_initiator_role" db:"f_initiator_role" json:"f_initiator_role" form:"f_initiator_role"`
	FInitiatorPartyId   string    `gorm:"column:f_initiator_party_id" db:"f_initiator_party_id" json:"f_initiator_party_id" form:"f_initiator_party_id"`
	FRuntimeConf        string    `gorm:"column:f_runtime_conf" db:"f_runtime_conf" json:"f_runtime_conf" form:"f_runtime_conf"`
	FWorkMode           int       `gorm:"column:f_work_mode" db:"f_work_mode" json:"f_work_mode" form:"f_work_mode"`
	FTrainDsl           string    `gorm:"column:f_train_dsl" db:"f_train_dsl" json:"f_train_dsl" form:"f_train_dsl"`
	FTrainRuntimeConf   string    `gorm:"column:f_train_runtime_conf" db:"f_train_runtime_conf" json:"f_train_runtime_conf" form:"f_train_runtime_conf"`
	FImported           int       `gorm:"column:f_imported" db:"f_imported" json:"f_imported" form:"f_imported"`
	FJobStatus          string    `gorm:"column:f_job_status" db:"f_job_status" json:"f_job_status" form:"f_job_status"`
	FRuntimeConfOnParty string    `gorm:"column:f_runtime_conf_on_party" db:"f_runtime_conf_on_party" json:"f_runtime_conf_on_party" form:"f_runtime_conf_on_party"`
	FFateVersion        string    `gorm:"column:f_fate_version" db:"f_fate_version" json:"f_fate_version" form:"f_fate_version"`
	FParent             int8      `gorm:"column:f_parent" db:"f_parent" json:"f_parent" form:"f_parent"`
	FParentInfo         string    `gorm:"column:f_parent_info" db:"f_parent_info" json:"f_parent_info" form:"f_parent_info"`
	FInferenceDsl       string    `gorm:"column:f_inference_dsl" db:"f_inference_dsl" json:"f_inference_dsl" form:"f_inference_dsl"`
}

//type MLModelsInfo []TMachineLearningModelInfo

func pubUpdatedModels(ctx context.Context, client dapr.Client) {

	models, stateKey, nowTime := updatedMLModelInfos(ctx, client)

	if len(models) == 0 {
		log.Println("no models info to pub")
		return
	}

	data, err := json.Marshal(models)
	if err != nil {
		log.Printf("error marshal %v\n", models)
		return
	}

	//log.Printf("%v marshed to %s\n", models, data)

	err = client.PublishEvent(ctx, COMPONENT_PUBSUB_NAME, FL_MODEL_INFO_TOPIC, data)
	if err != nil {
		log.Println(err)
	}

	buff := make([]byte, 8)
	binary.PutVarint(buff, nowTime)
	//log.Printf("------------buff: %v\n", buff)
	err = client.SaveState(ctx, COMPONENT_STATE_STORE_NAME, stateKey, buff)
	if err != nil {
		log.Println(err)
	}
}

func updatedMLModelInfos(ctx context.Context, client dapr.Client) (models []*TMachineLearningModelInfo, stateKey string, nowTime int64) {

	defer func() {
		err := recover()
		if err != nil {
			log.Println(err)
		}
	}()

	partyName, ok := os.LookupEnv(POD_NAMESPACE)
	if !ok {
		partyName = DEFAULT_NAMESPACE
	}

	stateKey = partyName + "::" + FL_MODEL_UPDATE_TIME

	item, err := client.GetState(ctx, COMPONENT_STATE_STORE_NAME, stateKey)
	if err != nil {
		log.Println(err)
	}

	var lastUpdateTime int64
	if item == nil {
		// 若未获取到上次更新日期，默认使用1天前作为上次更新日期
		lastUpdateTime = time.Now().AddDate(0, 0, -1).UnixNano() / 1000_000
	} else {
		lastUpdateTime, _ = binary.Varint(item.Value)
	}

	nowTime = time.Now().UnixNano() / 1000_000

	//log.Printf("----models", models)
	//log.Printf("----DB", DB)
	DB.Table("t_machine_learning_model_info").Where(
		"f_update_time >= ? AND f_update_time <= ?", lastUpdateTime, nowTime,
	).Find(&models)
	//log.Printf("result: %v--------------------models: %v\n", result, models)

	return models, stateKey, nowTime
}
