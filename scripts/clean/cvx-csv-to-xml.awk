# Duplicate/ Superflous name removal in <cmt>
#

flag = 0

# ICAO

$0 ~ /^M/ {
  #note ele is in ft
  stemp = $0
  split(stemp, stbl, ",")
  
  if ((stbl[8] != "US") && (stbl[2] != "heliport")) {
      print "<wpt lat=\"" stbl[5] "\" lon=\"" stbl[4] "\">"
      print "  <ele>" stbl[6] / 3.28084 "</ele>"
      print "  <name>" stbl[1]"</name>"
      print "  <cmt>" stbl[3] "," stbl[8] "</cmt>"
      print "  <type>AIRPORT</type>"
      print "</wpt>"
  }
	
  flag = 1
}

$0 ~ /^S/ {
  #note ele is in ft
  stemp = $0
  split(stemp, stbl, ",")
  
  if ((stbl[8] != "US") && (stbl[2] != "heliport")) {
      print "<wpt lat=\"" stbl[5] "\" lon=\"" stbl[4] "\">"
      print "  <ele>" stbl[6] / 3.28084 "</ele>"
      print "  <name>" stbl[1]"</name>"
      print "  <cmt>" stbl[3] "," stbl[8] "</cmt>"
      print "  <type>AIRPORT</type>"
      print "</wpt>"
  }
	
  flag = 1
}

#
# Simply print the line if it has not been handled already
#
{
  previousline = $0
  if (flag == 0) {
    #print $0
  }
}



