#!/bin/bash
curl -X POST \
  http://supersede.es.atos.net:8081/twitterAPI/configuration/ \
  -H 'Content-type: text/plain' \
  -d '{
      "SocialNetworksMonitoringConfProf": {
          "toolName": "TwitterAPI",
          "timeSlot": "30",
          "kafkaEndpoint": "147.83.192.53:9092",
          "kafkaTopic": "68d24960-5eff-4c14-8a8c-6d0c7f8ea5c3",
          "keywordExpression": "Hello"
      }
  }' \
  | cat
