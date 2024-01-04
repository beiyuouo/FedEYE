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

const FL_JOB_TOPIC = "fl-job"
const FL_JOB_UPDATE_TIME = "fl-job-update-time"

type TJob struct {
	FCreateTime           int64     `gorm:"column:f_create_time" db:"f_create_time" json:"f_create_time" form:"f_create_time"`
	FCreateDate           time.Time `gorm:"column:f_create_date" db:"f_create_date" json:"f_create_date" form:"f_create_date"`
	FUpdateTime           int64     `gorm:"column:f_update_time" db:"f_update_time" json:"f_update_time" form:"f_update_time"`
	FUpdateDate           time.Time `gorm:"column:f_update_date" db:"f_update_date" json:"f_update_date" form:"f_update_date"`
	FUserId               string    `gorm:"column:f_user_id" db:"f_user_id" json:"f_user_id" form:"f_user_id"`
	FJobId                string    `gorm:"column:f_job_id" db:"f_job_id" json:"f_job_id" form:"f_job_id"`
	FName                 string    `gorm:"column:f_name" db:"f_name" json:"f_name" form:"f_name"`
	FDescription          string    `gorm:"column:f_description" db:"f_description" json:"f_description" form:"f_description"`
	FTag                  string    `gorm:"column:f_tag" db:"f_tag" json:"f_tag" form:"f_tag"`
	FDsl                  string    `gorm:"column:f_dsl" db:"f_dsl" json:"f_dsl" form:"f_dsl"`
	FRuntimeConf          string    `gorm:"column:f_runtime_conf" db:"f_runtime_conf" json:"f_runtime_conf" form:"f_runtime_conf"`
	FRuntimeConfOnParty   string    `gorm:"column:f_runtime_conf_on_party" db:"f_runtime_conf_on_party" json:"f_runtime_conf_on_party" form:"f_runtime_conf_on_party"`
	FTrainRuntimeConf     string    `gorm:"column:f_train_runtime_conf" db:"f_train_runtime_conf" json:"f_train_runtime_conf" form:"f_train_runtime_conf"`
	FRoles                string    `gorm:"column:f_roles" db:"f_roles" json:"f_roles" form:"f_roles"`
	FWorkMode             int       `gorm:"column:f_work_mode" db:"f_work_mode" json:"f_work_mode" form:"f_work_mode"`
	FInitiatorRole        string    `gorm:"column:f_initiator_role" db:"f_initiator_role" json:"f_initiator_role" form:"f_initiator_role"`
	FInitiatorPartyId     string    `gorm:"column:f_initiator_party_id" db:"f_initiator_party_id" json:"f_initiator_party_id" form:"f_initiator_party_id"`
	FStatus               string    `gorm:"column:f_status" db:"f_status" json:"f_status" form:"f_status"`
	FStatusCode           int       `gorm:"column:f_status_code" db:"f_status_code" json:"f_status_code" form:"f_status_code"`
	FRole                 string    `gorm:"column:f_role" db:"f_role" json:"f_role" form:"f_role"`
	FPartyId              string    `gorm:"column:f_party_id" db:"f_party_id" json:"f_party_id" form:"f_party_id"`
	FIsInitiator          int8      `gorm:"column:f_is_initiator" db:"f_is_initiator" json:"f_is_initiator" form:"f_is_initiator"`
	FProgress             int       `gorm:"column:f_progress" db:"f_progress" json:"f_progress" form:"f_progress"`
	FReadySignal          int8      `gorm:"column:f_ready_signal" db:"f_ready_signal" json:"f_ready_signal" form:"f_ready_signal"`
	FReadyTime            int64     `gorm:"column:f_ready_time" db:"f_ready_time" json:"f_ready_time" form:"f_ready_time"`
	FCancelSignal         int8      `gorm:"column:f_cancel_signal" db:"f_cancel_signal" json:"f_cancel_signal" form:"f_cancel_signal"`
	FCancelTime           int64     `gorm:"column:f_cancel_time" db:"f_cancel_time" json:"f_cancel_time" form:"f_cancel_time"`
	FRerunSignal          int8      `gorm:"column:f_rerun_signal" db:"f_rerun_signal" json:"f_rerun_signal" form:"f_rerun_signal"`
	FEndSchedulingUpdates int       `gorm:"column:f_end_scheduling_updates" db:"f_end_scheduling_updates" json:"f_end_scheduling_updates" form:"f_end_scheduling_updates"`
	FEngineName           string    `gorm:"column:f_engine_name" db:"f_engine_name" json:"f_engine_name" form:"f_engine_name"`
	FEngineType           string    `gorm:"column:f_engine_type" db:"f_engine_type" json:"f_engine_type" form:"f_engine_type"`
	FCores                int       `gorm:"column:f_cores" db:"f_cores" json:"f_cores" form:"f_cores"`
	FMemory               int       `gorm:"column:f_memory" db:"f_memory" json:"f_memory" form:"f_memory"`
	FRemainingCores       int       `gorm:"column:f_remaining_cores" db:"f_remaining_cores" json:"f_remaining_cores" form:"f_remaining_cores"`
	FRemainingMemory      int       `gorm:"column:f_remaining_memory" db:"f_remaining_memory" json:"f_remaining_memory" form:"f_remaining_memory"`
	FResourceInUse        int8      `gorm:"column:f_resource_in_use" db:"f_resource_in_use" json:"f_resource_in_use" form:"f_resource_in_use"`
	FApplyResourceTime    int64     `gorm:"column:f_apply_resource_time" db:"f_apply_resource_time" json:"f_apply_resource_time" form:"f_apply_resource_time"`
	FReturnResourceTime   int64     `gorm:"column:f_return_resource_time" db:"f_return_resource_time" json:"f_return_resource_time" form:"f_return_resource_time"`
	FStartTime            int64     `gorm:"column:f_start_time" db:"f_start_time" json:"f_start_time" form:"f_start_time"`
	FStartDate            time.Time `gorm:"column:f_start_date" db:"f_start_date" json:"f_start_date" form:"f_start_date"`
	FEndTime              int64     `gorm:"column:f_end_time" db:"f_end_time" json:"f_end_time" form:"f_end_time"`
	FEndDate              time.Time `gorm:"column:f_end_date" db:"f_end_date" json:"f_end_date" form:"f_end_date"`
	FElapsed              int64     `gorm:"column:f_elapsed" db:"f_elapsed" json:"f_elapsed" form:"f_elapsed"`
}

func pubUpdatedJobs(ctx context.Context, client dapr.Client) {

	models, stateKey, nowTime := updatedJobs(ctx, client)

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

	err = client.PublishEvent(ctx, COMPONENT_PUBSUB_NAME, FL_JOB_TOPIC, data)
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

func updatedJobs(ctx context.Context, client dapr.Client) (models []*TJob, stateKey string, nowTime int64) {

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

	stateKey = partyName + "::" + FL_JOB_UPDATE_TIME

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
	DB.Table("t_job").Where(
		"f_update_time >= ? AND f_update_time <= ?", lastUpdateTime, nowTime,
	).Find(&models)
	//log.Printf("result: %v--------------------models: %v\n", result, models)

	return models, stateKey, nowTime
}
