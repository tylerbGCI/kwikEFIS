# Non Table version
# Reformat the html adding CXML tags
#

flag = 0

# ICAO

previousline ~ /<!--ICAO-->/ {
  #print previousline;
  print $0;
  #flag = 1;
}

previousline ~ /<!--INFO-->/ {
  #print previousline;
  print $0;
  #flag = 1;
}




#
# Simply print the line if it has not been handled already
#
{
  previousline = $0
  #if (flag == 0) {
  #  print $0
  #}
}



