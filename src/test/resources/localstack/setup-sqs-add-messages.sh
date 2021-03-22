#!/usr/bin/env bash
set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foobar
export AWS_SECRET_ACCESS_KEY=foobar
export AWS_DEFAULT_REGION=eu-west-2
export PAGER=

# example of how to send messages to queues from command line

#aws sqs send-message \
#    --queue-url http://localhost:4576/000000000000/Digital-Prison-Services-dev-hmpps_tier_offender_events_queue \
#    --message-body '{"Type": "Notification", "MessageId": "e855eadc-839c-4594-bfa5-91f3e7581651", "Token": null, "TopicArn": "arn:aws:sns:eu-west-2:000000000000:offender_events", "Message": "{\"offenderId\":2500468261,\"crn\":\"XMAIN55\",\"sourceId\":11174,\"eventDatetime\":\"2021-01-20T13:34:59\"}", "SubscribeURL": null, "Timestamp": "2021-02-22T07:12:59.119Z", "SignatureVersion": "1", "Signature": "EXAMPLEpH+..", "SigningCertURL": "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-0000000000000000000000.pem", "MessageAttributes": {"eventType": {"Type": "String", "Value": "OFFENDER_MANAGEMENT_TIER_CALCULATION_REQUIRED"}, "source": {"Type": "String", "Value": "delius"}, "id": {"Type": "String", "Value": "fcf89ef7-f6e8-ee95-326f-8ce87d3b8ea0"}, "contentType": {"Type": "String", "Value": "text/plain;charset=UTF-8"}, "timestamp": {"Type": "Number", "Value": "1611149702333"}}}'  --endpoint-url=http://localhost:4576
#
#aws sqs send-message \
#    --queue-url http://localhost:4576/000000000000/Digital-Prison-Services-dev-hmpps_tier_offender_events_queue_dl \
#    --message-body '{"Type": "Notification", "MessageId": "e855eadc-839c-4594-bfa5-91f3e7581651", "Token": null, "TopicArn": "arn:aws:sns:eu-west-2:000000000000:offender_events", "Message": "{\"offenderId\":2500468261,\"crn\":\"XDLQ555\",\"sourceId\":11174,\"eventDatetime\":\"2021-01-20T13:34:59\"}", "SubscribeURL": null, "Timestamp": "2021-02-22T07:12:59.119Z", "SignatureVersion": "1", "Signature": "EXAMPLEpH+..", "SigningCertURL": "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-0000000000000000000000.pem", "MessageAttributes": {"eventType": {"Type": "String", "Value": "OFFENDER_MANAGEMENT_TIER_CALCULATION_REQUIRED"}, "source": {"Type": "String", "Value": "delius"}, "id": {"Type": "String", "Value": "fcf89ef7-f6e8-ee95-326f-8ce87d3b8ea0"}, "contentType": {"Type": "String", "Value": "text/plain;charset=UTF-8"}, "timestamp": {"Type": "Number", "Value": "1611149702333"}}}'  --endpoint-url=http://localhost:4576
