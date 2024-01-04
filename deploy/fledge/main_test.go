package main

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	dapr "github.com/dapr/go-sdk/client"
	"github.com/gin-gonic/gin"
	"github.com/runmark/fledge/api"
	"net/http"
	"net/http/httptest"
	"testing"
)

func setup() {
	gin.SetMode(gin.TestMode)
}

func TestMain(m *testing.M) {
	setup()
	m.Run()
}

type dummyDaprClient struct {}

func (d dummyDaprClient) InvokeBinding(ctx context.Context, in *dapr.InvokeBindingRequest) (out *dapr.BindingEvent, err error) {
	panic("implement me")
}

func (d dummyDaprClient) InvokeOutputBinding(ctx context.Context, in *dapr.InvokeBindingRequest) error {
	panic("implement me")
}

func (d dummyDaprClient) InvokeMethod(ctx context.Context, appID, methodName, verb string) (out []byte, err error) {
	panic("implement me")
}

func (d dummyDaprClient) InvokeMethodWithContent(ctx context.Context, appID, methodName, verb string, content *dapr.DataContent) (out []byte, err error) {
	panic("implement me")
}

func (d dummyDaprClient) InvokeMethodWithCustomContent(ctx context.Context, appID, methodName, verb string, contentType string, content interface{}) (out []byte, err error) {
	panic("implement me")
}

func (d dummyDaprClient) PublishEvent(ctx context.Context, pubsubName, topicName string, data []byte) error {
	fmt.Printf("pubsubname=%s, topicname=%s, data=%v\n", pubsubName, topicName, data)
	return nil
}

func (d dummyDaprClient) PublishEventfromCustomContent(ctx context.Context, pubsubName, topicName string, data interface{}) error {
	panic("implement me")
}

func (d dummyDaprClient) GetSecret(ctx context.Context, storeName, key string, meta map[string]string) (data map[string]string, err error) {
	panic("implement me")
}

func (d dummyDaprClient) GetBulkSecret(ctx context.Context, storeName string, meta map[string]string) (data map[string]map[string]string, err error) {
	panic("implement me")
}

func (d dummyDaprClient) SaveState(ctx context.Context, storeName, key string, data []byte) error {
	panic("implement me")
}

func (d dummyDaprClient) SaveBulkState(ctx context.Context, storeName string, items ...*dapr.SetStateItem) error {
	panic("implement me")
}

func (d dummyDaprClient) GetState(ctx context.Context, storeName, key string) (item *dapr.StateItem, err error) {
	panic("implement me")
}

func (d dummyDaprClient) GetStateWithConsistency(ctx context.Context, storeName, key string, meta map[string]string, sc dapr.StateConsistency) (item *dapr.StateItem, err error) {
	panic("implement me")
}

func (d dummyDaprClient) GetBulkState(ctx context.Context, storeName string, keys []string, meta map[string]string, parallelism int32) ([]*dapr.BulkStateItem, error) {
	panic("implement me")
}

func (d dummyDaprClient) DeleteState(ctx context.Context, storeName, key string) error {
	panic("implement me")
}

func (d dummyDaprClient) DeleteStateWithETag(ctx context.Context, storeName, key string, etag *dapr.ETag, meta map[string]string, opts *dapr.StateOptions) error {
	panic("implement me")
}

func (d dummyDaprClient) ExecuteStateTransaction(ctx context.Context, storeName string, meta map[string]string, ops []*dapr.StateOperation) error {
	panic("implement me")
}

func (d dummyDaprClient) DeleteBulkState(ctx context.Context, storeName string, keys []string) error {
	panic("implement me")
}

func (d dummyDaprClient) DeleteBulkStateItems(ctx context.Context, storeName string, items []*dapr.DeleteStateItem) error {
	panic("implement me")
}

func (d dummyDaprClient) Shutdown(ctx context.Context) error {
	panic("implement me")
}

func (d dummyDaprClient) WithTraceID(ctx context.Context, id string) context.Context {
	panic("implement me")
}

func (d dummyDaprClient) WithAuthToken(token string) {
	panic("implement me")
}

func (d dummyDaprClient) Close() {
	panic("implement me")
}



func TestEdgeDataSummary(t *testing.T) {

	t.Run("edgeDataSummary", func(t *testing.T) {
		r := SetupRouter(dummyDaprClient{})

		var stubDataSummary = api.DataSummary{
			"sdaict", "bucket/objectname.csv", "", "id,name,age",
		}
		var body, _ = json.Marshal(stubDataSummary)

		req, err := http.NewRequest("POST", "/edge-data-summary", bytes.NewReader(body))
		if err != nil {
			t.Errorf("expected no error, but got %v", err)
		}

		w := httptest.NewRecorder()

		r.ServeHTTP(w, req)

		resp := w.Result()

		if resp.StatusCode != 200 {
			t.Errorf("expected status 200, but got %v\n", resp.Status)
		}

		gotDataSummary := api.DataSummary{}
		json.NewDecoder(resp.Body).Decode(&gotDataSummary)

		if stubDataSummary != gotDataSummary {
			t.Errorf("expected %v, but got %v", stubDataSummary, gotDataSummary)
		}

	})

}