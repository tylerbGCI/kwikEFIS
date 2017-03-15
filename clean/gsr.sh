#!/bin/bash

for file in *.html
do
  echo "Processing ..." $file
  # Replace the .png with .jpg
  # sed s/.png\"/.jpg\"/gi $file > $file.a
  # Replace Oceania with Australasia
  #sed s/Oceania/Australasia/g $file > $file.a
  #sed s/height=150//g $file > $file.a
  #sed s/"Po Box"/"PO Box"/g $file > $file.a
  sed s/NONE.jpg/NONE.png/g $file > $file.a
done
