    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "aws,logstash,stdout"

  - name: MAIN_QUEUE_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-sqs-tool-main-queue
        key: access_key_id

  - name: MAIN_QUEUE_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-sqs-tool-main-queue
        key: secret_access_key

  - name: MAIN_QUEUE
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-sqs-tool-main-queue
        key: sqs_queue_url

  - name: DLQ_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-sqs-tool-dead-letter-queue
        key: access_key_id

  - name: DLQ_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-sqs-tool-dead-letter-queue
        key: secret_access_key

  - name: DLQ_QUEUE
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-sqs-tool-dead-letter-queue
        key: sqs_queue_url

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-sqs-tool
        key: APPINSIGHTS_INSTRUMENTATIONKEY
{{- end -}}
