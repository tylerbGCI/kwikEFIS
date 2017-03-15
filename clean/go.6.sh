#!/bin/bash

for file in *.html
do
  echo "Processing ..." $file
  sed s/Avbl./available/gi $file > $file.a
done
