#!/bin/bash

#pskill java
#pskill adb

# ./gradlew clean
rm ./apk/kwik-efis.apk
rm ./apk/kwik-dmap.apk
rm ./apk/kwik-comp.apk
rm ./apk/kwik-efis-datapac-zar.aus.apk
rm ./apk/kwik-efis-datapac-usa.can.apk
rm ./apk/kwik-efis-datapac-eur.rus.apk
rm ./apk/kwik-efis-datapac-sah.jap.apk
rm ./apk/kwik-efis-datapac-pan.arg.apk

#./gradlew build
./gradlew assemble

cp ./pfd/build/outputs/apk/pfd-debug.apk ./apk/kwik-efis.apk
cp ./mfd/build/outputs/apk/mfd-debug.apk ./apk/kwik-dmap.apk
cp ./cfd/build/outputs/apk/cfd-debug.apk ./apk/kwik-comp.apk
cp ./data.zar.aus/build/outputs/apk/data.zar.aus-debug.apk ./apk/kwik-efis-datapac-zar.aus.apk
cp ./data.usa.can/build/outputs/apk/data.usa.can-debug.apk ./apk/kwik-efis-datapac-usa.can.apk
cp ./data.eur.rus/build/outputs/apk/data.eur.rus-debug.apk ./apk/kwik-efis-datapac-eur.rus.apk
cp ./data.sah.jap/build/outputs/apk/data.sah.jap-debug.apk ./apk/kwik-efis-datapac-sah.jap.apk
cp ./data.pan.arg/build/outputs/apk/data.pan.arg-debug.apk ./apk/kwik-efis-datapac-pan.arg.apk
cp ./CHANGELOG.md ./apk/CHANGELOG.md

exit

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



pskill java
pskill adb
