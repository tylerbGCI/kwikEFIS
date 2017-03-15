# Non Table version
# Reformat the html adding CXML tags
#

flag = 0
previousline

# ICAO
$0  ~ /^<!--ICAO-->/ {
  stemp = $0
  split(stemp, stbl, "<!--ICAO-->");
  print "<!--ICAO-->";
  print stbl[2];
  print "<!--/ICAO-->";
  flag = 1
}

# INFO
$0  ~ /^<!--INFO-->/ {
  stemp = $0
  split(stemp, stbl, "<!--INFO-->");
  print "<!--INFO-->";
  print stbl[2];
  print "<!--/INFO-->";
  flag = 1
}

# REGION
$0  ~ /^<!--REGION-->/ {
  stemp = $0
  split(stemp, stbl, "<!--REGION-->");
  print "<!--COUNTRY-->";
  print stbl[2];
  print "<!--/COUNTRY-->";
  print ", ";
  print "<!--REGION-->";
  if (stbl[2] == "Australia")
    print "Oceania";
  if (stbl[2] == "South Africa")
   print "Africa";
  print "<!--/REGION-->";
  flag = 1
}

# TITLE
$0  ~ /^<hr><table/ {
  stemp = $0
  print "<!--TITLE-->";
  print stemp;
  flag = 1
}

# SPATIAL
$0  ~ /<\/table><hr>/ {
  stemp = $0
  print stemp;
  #print "<table width=\"100%\">";
  #print "<td align=\"left\">";
  print "<!--SPATIAL-->";
  flag = 1
}

# FREQ
$0  ~ /^<p><b>Frequencies/ {
  stemp = $0
  print "<!--/SPATIAL-->";
  print stemp;
  print "<!--FREQ-->";
  flag = 1
}

# RWY
$0  ~ /^<p><b>Runways/ {
  stemp = $0
  print "<!--/FREQ-->";
  print stemp;
  print "<!--RWY-->";
  flag = 1
}

# COMMENT
$0  ~ /^<p><b>Comments/ {
  stemp = $0
  print "<!--/RWY-->";
  print stemp;
  print "<!--COMMENT-->";
  flag = 1
}

# FUEL
$0  ~ /^<p><b>Fuel/ {
  stemp = $0
  print "<!--/COMMENT-->";
  print stemp;
  print "<!--FUEL-->";
  flag = 1
}

# OPERATOR
$0  ~ /^<p><b>Operator/ {
  stemp = $0
  print "<!--/FUEL-->";
  print stemp;
  print "<!--OPERATOR-->";
  flag = 1
}

# IMAGE
$0  ~ /^<br><a href=".\/img\// {
  if (img == 0) {
    stemp = $0
    print "<!--/OPERATOR-->";
    #print "<td align=\"right\">";
    print "<!--IMAGE-->";
    print stemp;
    flag = 1
    img = 1
  }
}


$0  ~ /^<\/body/ {
  stemp = $0
  #print "</table>";
  print "<!--/IMAGE-->";
  print stemp;
  flag = 1
}




#
# Simply print the line if it has not been handled already
#
{
  if (flag == 0) {
    print $0
  }
}



