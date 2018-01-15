#!/bin/bash
gradle build && java -jar build/libs/loopa.jar $1 "Hello" 30 1 29 10 2 25 | cat
