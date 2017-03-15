# Lose the decimals in elevation
#

flag = 0

# ICAO

$0 ~ /<ele>/ {
  stemp = $0
	
  split(stemp, stbl, ".")
	print stbl[1] "</ele>"
	
	
  flag = 1
}
	


#
# Simply print the line if it has not been handled already
#
{
  previousline = $0
  if (flag == 0) {
    print $0
    #print "-->", $0
  }
}



