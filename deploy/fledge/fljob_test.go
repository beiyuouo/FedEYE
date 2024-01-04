package main

import (
	"encoding/binary"
	"fmt"
	"testing"
	"time"
)

func TestTimestampToBytes(t *testing.T) {

	timestampMS := time.Now().AddDate(0, 0, -1).UnixNano() / 1000_000

	buff := make([]byte, 8)
	binary.PutVarint(buff, timestampMS)

	fmt.Printf("timestamp(ms):%v, timestamp bytes: %v\n", timestampMS, buff)
}
