# Archived 12/11/2021 - replaced by https://github.com/ministryofjustice/hmpps-spring-boot-sqs/

# hmpps-tier-sqs-tool

A utility to manually trigger the recalculation of a service user's Tier score.
Also used to move messages off the dead letter queue onto the main queue, and (manually) to empty the dead letter queue 

`hmpps-tier-sqs-tool` is used to create a message or messages on the SNS instance `hmpps-tier` uses to subscribe to the Offender Events SNS and so force it to perform a calculation for the subject of the message.

# Starting an instance

**Use the deployed instances**

The tool is deployed to all `hmpps-tier` and `hmpps-tier-to-delius-update` environments, and can be port forwarded to in the normal kubernetes way. It might need to be scaled up first!

```sh
kubectl port-forward deployment/hmpps-tier-sqs-tool 8080:8080 -n NAMESPACE
```

**Run from your laptop terminal against AWS**

`
DLQ_QUEUE=DLQURL DLQ_ACCESS_KEY_ID=DLQKEY DLQ_SECRET_ACCESS_KEY=DLQSECRET MAIN_QUEUE=URL MAIN_QUEUE_ACCESS_KEY_ID=KEY MAIN_QUEUE_SECRET_ACCESS_KEY=SECRET SPRING_PROFILES_ACTIVE=dev,aws ./gradlew bootRun
`

**Run from your laptop terminal against Localstack**

Start up localstack to get the event queue and DLQ with one message on each

```sh
docker-compose up -d
SPRING_PROFILES_ACTIVE=dev,localstack ./gradlew bootRun
```

# testing

When running locally, the tests require localstack docker to be running. They will change the state of the queues

```sh
docker-compose up -d
./gradlew check
```

# Tier calculation request Endpoints
## only useful when deployed to hmpps-tier. Do not use in hmpps-tier-delius-update 

There are two endpoints `POST /file` and `POST /send`

`/file` allows you to upload a csv and create messages from that.
The format must be a single column of CRNs with 'CRN' as the header of that column.

```sh
curl http://localhost:8080/file --request POST --form 'file=@"somefile.csv"'
```

`/send` allows you to pass an array of CRNs in the body of the message instead.

```sh
curl http://localhost:8080/send --request POST -d '["X387579"]' -H "Content-Type: application/json"
```

# queue management endpoints
## useful in all environments
`GET /transfer`

Moves all messages from the dead letter queue onto the main queue
Designed to be called from a cronjob

```sh
curl http://localhost:8080/transfer
```

`GET /emptydlq`

Deletes all messages on the dead letter queue. To be used when the message can never be processed successfully  


-------
