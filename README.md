# hmpps-tier-sqs-tool

A utility to manually trigger the recalculation of service users's Tier score.

`hmpps-tier-sqs-tool` is used to create a message or messages on the SNS instance `hmpps-tier` uses to subscribe to the Offender Events SNS and so force it to perform a calculation for the subject of the message.

# Starting an instance

**Use the deployed instances**

The tool is deployed to `hmpps-tier-dev` and `hmpps-tier-preprod` environments, and can be port forwarded to in the normal kubernetes way. It might need to be scaled up first!

`kubectl port-forward deployment/hmpps-tier-sqs-tool 8080:8080`

**Run from your laptop terminal**

`docker-compose up -d
AWS_OFFENDER_EVENTS_QUEUE=SOME_URL AWS_OFFENDER_EVENTS_ACCESS_KEY=SOME_KEY AWS_OFFENDER_EVENTS_SECRET_ACCESS_KEY=SOME_SECRET ./gradlew bootRun`


# Available Endpoints

There are two endpoints `POST /file` and `POST /send`

`/file` allows you to upload a csv and create messages from that.
The format must be a single column of CRNs with 'CRN' as the header of that column.

`curl http://localhost:8080/file --request POST --form 'file=@"somefile.csv"'`

`/send` allows you to pass an array of CRNs in the body of the message instead.

`curl http://localhost:8080/send --request POST -d '["X387579"]' -H "Content-Type: application/json"`

-------
