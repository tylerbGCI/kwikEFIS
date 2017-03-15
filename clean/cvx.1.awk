
flag = 0
previousline

$0  ~ /^<hr><table/ {
  stemp = $0
  print "<!--TITLE-->" stemp;
  flag = 1
}

$0  ~ /<\/table><hr>/ {
  stemp = $0
  print stemp;
  print "<!--SPATIAL-->";
  flag = 1
}

$0  ~ /^<p><b>Frequencies/ {
  stemp = $0
  print "<!--FREQ-->" stemp;
  flag = 1
}

$0  ~ /^<p><b>Runways/ {
  stemp = $0
  print "<!--RWY-->" stemp;
  flag = 1
}

$0  ~ /^<p><b>Comments/ {
  stemp = $0
  print "<!--COMMENT-->" stemp;
  flag = 1
}

$0  ~ /^<p><b>Fuel/ {
  stemp = $0
  print "<!--FUEL-->" stemp;
  flag = 1
}

$0  ~ /^<p><b>Operator/ {
  stemp = $0
  print "<!--OPERATOR-->" stemp;
  flag = 1
}



$0  ~ /^<br><a href=".\/img\// {
  if (img == 0) {
    stemp = $0
    print "<!--IMAGE-->" stemp;
    flag = 1
    img = 1
  }
}



#
# Simply print the line if it has not been handled already
#
{
  if (flag == 0) {
    print $0
  }
}



