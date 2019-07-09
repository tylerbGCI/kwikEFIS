#!/bin/bash

#pskill java
#pskill adb
adb devices

cd ./apk

if [ ! -z $1 ] && [ $1 == '-nw' ] 
then
    echo 'wifi nexus 7'
	DROIDDEVICE=015d2ea4a467ec11
fi

if [ ! -z $1 ] && [ $1 == '-ng' ] 
then
    echo 'gsm nexus 7'
	DROIDDEVICE=015d3249295c160b
fi

if [ ! -z $1 ] && [ $1 == '-ss' ] 
then
    echo 'samsung g5'
	DROIDDEVICE=758f9cc3
fi

if [ ! -z $1 ] && [ $1 == '-bv' ] 
then
    echo 'blackview bv6000s'
	DROIDDEVICE=CQAA5L8S599DSWDQ
fi

if [ ! -z $1 ]
then
    adb devices

    #wifi nexus 7
    adb -s $DROIDDEVICE uninstall player.efis.pfd
    adb -s $DROIDDEVICE uninstall player.efis.mfd
    adb -s $DROIDDEVICE uninstall player.efis.cfd

    adb -s $DROIDDEVICE install -r ./kwik-efis.apk
    adb -s $DROIDDEVICE install -r ./kwik-dmap.apk
    adb -s $DROIDDEVICE install -r ./kwik-comp.apk

    #adb -s $DROIDDEVICE install -r ./kwik-efis-datapac-zar.aus.apk
    #adb -s $DROIDDEVICE install -r ./kwik-efis-datapac-usa.can.apk
    #adb -s $DROIDDEVICE install -r ./kwik-efis-datapac-eur.rus.apk
fi


#pskill java
#pskill adb
