# Duplicate/ Superflous name removal in <cmt>
#

flag = 0

# ICAO

$0 ~ /<cmt>/ {
  stemp = $0
	
  split(stemp, stbl, ",")
  # the state as well	
  # print stbl[1] "," stbl[2]  "," stbl[3] "," stbl[4]
  # sans state 
	
	if (stbl[4] != "") {
    print stbl[1] "," stbl[4]
	}
	else {
    print stbl[1] "," stbl[3]
	}
	
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



