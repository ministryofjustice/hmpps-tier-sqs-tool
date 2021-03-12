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

  - name: AWS_OFFENDER_EVENTS_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-offender-events-sqs-instance-output
        key: access_key_id

  - name: AWS_OFFENDER_EVENTS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-offender-events-sqs-instance-output
        key: secret_access_key

  - name: AWS_OFFENDER_EVENTS_QUEUE
    valueFrom:
      secretKeyRef:
        name: hmpps-tier-offender-events-sqs-instance-output
        key: sqs_ptpu_url

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: hmpps-tier
        key: APPINSIGHTS_INSTRUMENTATIONKEY
{{- end -}}
