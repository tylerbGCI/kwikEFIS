#!/bin/bash

#for file in *.html
#do
#  echo "Processing ..." $file
#  sed s/.png\"/.jpg\"/gi $file > $file.a
#done

echo Start

#echo "<html><body>" > index.html
echo "<body bgcolor=\"#FFFFFF\" link=\"#0000FF\" vlink=\"#800080\"> " > index.html

for file in A*.html
do
  echo $file
  echo "<br><a href=\"$file\">" >> index.html
  awk -f cvx.5.awk $file        >> index.html
done

for file in Y*.html
do
  echo $file
  echo "<br><a href=\"$file\">" >> index.html
  awk -f cvx.5.awk $file        >> index.html
done



echo "</html></body>"           >> index.html

echo Done
