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

  - name: OFFENDER_EVENTS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-offender-events-sqs-instance-output
        key: access_key_id

  - name: OFFENDER_EVENTS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-offender-events-sqs-instance-output
        key: secret_access_key

  - name: OFFENDER_EVENTS_QUEUE
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-offender-events-sqs-instance-output
        key: sqs_ptpu_url

  - name: OFFENDER_EVENTS_DLQ_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-offender-events-sqs-dl-instance-output
        key: access_key_id

  - name: OFFENDER_EVENTS_DLQ_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-offender-events-sqs-dl-instance-output
        key: secret_access_key

  - name: OFFENDER_EVENTS_DLQ_QUEUE
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-offender-events-sqs-dl-instance-output
        key: sqs_ptpu_url

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: hmpps-tier
        key: APPINSIGHTS_INSTRUMENTATIONKEY
{{- end -}}
