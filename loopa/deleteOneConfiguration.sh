#!/bin/bash
curl -X DELETE http://supersede.es.atos.net:8081/twitterAPI/configuration/$1 | cat
