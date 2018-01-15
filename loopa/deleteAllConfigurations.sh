#!/bin/bash
for i in {1..200}
do
   printf "\n\nRemoving configuration with idConf $i\n"
   curl -X DELETE http://supersede.es.atos.net:8081/twitterAPI/configuration/$i | cat
done
