# hmpps-tier-sqs-tool

Feed the tier calculation required SQS 

# Instructions

Run from your laptop terminal

`docker-compose up -d
AWS_OFFENDER_EVENTS_QUEUE=SOME_URL AWS_OFFENDER_EVENTS_ACCESS_KEY=SOME_KEY AWS_OFFENDER_EVENTS_SECRET_ACCESS_KEY=SOME_SECRET ./gradlew bootRun`


CURL example to trigger a single tier calculation

`curl http://localhost:8080/body/send --request POST -d '["X387579"]' -H "Content-Type: application/json"`


-------

## Renaming from HMPPS Template Kotlin - github Actions

Once the new uk.gov.justice.digital.hmpps.hmppstiersqstool.repository is deployed. Navigate to the uk.gov.justice.digital.hmpps.hmppstiersqstool.repository in github, and select the `Actions` tab.
Click the link to `Enable Actions on this uk.gov.justice.digital.hmpps.hmppstiersqstool.repository`.

Find the Action workflow named: `rename-project-create-pr` and click `Run workflow`.  This workflow will will
execute the `rename-project.bash` and create Pull Request for you to review.  Review the PR and merge.

Note: ideally this workflow would run automatically however due to a recent change github Actions are not
enabled by default on newly created repos. There is no way to enable Actions other then to click the button in the UI.
If this situation changes we will update this project so that the workflow is triggered during the bootstrap project.
Further reading: <https://github.community/t/workflow-isnt-enabled-in-repos-generated-from-template/136421>

## Manually renaming from HMPPS Template Kotlin

Run the `rename-project.bash` and create a PR.

The `rename-project.bash` script takes a single argument - the name of the project and calculates from it:
* The main class name (project name converted to pascal case) 
* The project description (class name with spaces between the words)
* The main package name (project name with hyphens removed)

It then performs a search and replace and directory renames so the project is ready to be used.
