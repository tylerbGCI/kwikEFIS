# Case in <cmt>
#

flag = 0

#echo 'abce efgh ijkl mnop' | awk '{for (i=1;i <= NF;i++) {sub(".",substr(toupper($i),1,1),$i)} print}' --> Abcd Efgh Ijkl Mnop
#echo 'aBcD EfGh ijkl MNOP' | tr [A-Z] [a-z] | awk '{for (i=1;i <= NF;i++) {sub(".",substr(toupper($i),1,1),$i)} print}' --> Abcd Efgh Ijkl Mnop    

$0 ~ /<cmt>/ {
  stemp = $0
	
  split(stemp, stbl, ",")
  #print toupper(substr(stbl[1],0,1))tolower(substr(stbl[1],2)) 
  #print "<cmt>"toupper(substr(stbl[1],6,1))tolower(substr(stbl[1],7)) "," stbl[2]  "," stbl[3] "," stbl[4]
  { 
    s1 = tolower(stbl[1])
    split(s1, slin, " ")

    #print slin[1] "--"  substr(toupper(slin[2]),1,1) substr(slin[2],2,128) "--"slin[3] "--" slin[4]  "," stbl[2]
    for (i=1;i <= 5;i++) {
      slin[i] = substr(toupper(slin[i]),1,1) substr(slin[i],2,128) 
    } 
    
    sub(/<cmt>./,substr(toupper(slin[1]),6,1),slin[1])
    
    print "    <cmt>" slin[1] " " slin[2] " " slin[3] " " slin[4] " " slin[5]  "," stbl[2]
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



