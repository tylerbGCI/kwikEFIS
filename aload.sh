#!/bin/bash

#pskill java
#pskill adb

cd ./apk

if [ ! -z $1 ] && [ $1 == '-w' ] 
then
    echo 'wifi nexus 7'
    adb devices
    
    #wifi nexus 7
    adb -s 015d2ea4a467ec11 uninstall player.efis.pfd
    adb -s 015d2ea4a467ec11 uninstall player.efis.mfd
    
    adb -s 015d2ea4a467ec11 install -r ./kwik-efis.apk
    adb -s 015d2ea4a467ec11 install -r ./kwik-dmap.apk
    
    #adb -s 015d2ea4a467ec11 install -r ./kwik-efis-datapac-zar.aus.apk
fi

if [ ! -z $1 ] && [ $1 == '-g' ] 
then
    echo 'gsm nexus 7'
    adb devices
    
    #gsm nexus 7
    adb -s 015d3249295c160b uninstall player.efis.pfd
    adb -s 015d3249295c160b uninstall player.efis.mfd
    
    adb -s 015d3249295c160b install -r ./kwik-efis.apk
    adb -s 015d3249295c160b install -r ./kwik-dmap.apk
    
    #adb -s 015d3249295c160b install -r ./kwik-efis-datapac-zar.aus.apk
    #adb -s 015d3249295c160b install -r ./kwik-efis-datapac-usa.can.apk
    #adb -s 015d3249295c160b install -r ./kwik-efis-datapac-eur.rus.apk
fi


if [ ! -z $1 ] && [ $1 == '-s' ] 
then
    echo 'samsung g5'
    adb devices
    
    #gsm nexus 7
    adb -s 758f9cc3 uninstall player.efis.pfd
    adb -s 758f9cc3 uninstall player.efis.mfd
    
    adb -s 758f9cc3 install -r ./kwik-efis.apk
    adb -s 758f9cc3 install -r ./kwik-dmap.apk
    
    #adb -s 758f9cc3 install -r ./kwik-efis-datapac-zar.aus.apk
    #adb -s 758f9cc3 install -r ./kwik-efis-datapac-usa.can.apk
    #adb -s 758f9cc3 install -r ./kwik-efis-datapac-eur.rus.apk
fi


#pskill java
#pskill adb
