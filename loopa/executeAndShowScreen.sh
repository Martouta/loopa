#!/bin/bash
gradle build && java -jar build/libs/loopa.jar 185 "Hello" 30 1 29 10 2 25 | cat
