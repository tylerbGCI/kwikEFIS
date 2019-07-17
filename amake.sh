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

# ./gradlew build
./gradlew assemble

cp ./pfd/build/outputs/apk/debug/pfd-debug.apk ./apk/kwik-efis.apk
cp ./mfd/build/outputs/apk/debug/mfd-debug.apk ./apk/kwik-dmap.apk
cp ./cfd/build/outputs/apk/debug/cfd-debug.apk ./apk/kwik-comp.apk
cp ./datapac/zar.aus/build/outputs/apk/debug/zar.aus-debug.apk ./apk/kwik-efis-datapac-zar.aus.apk
cp ./datapac/usa.can/build/outputs/apk/debug/usa.can-debug.apk ./apk/kwik-efis-datapac-usa.can.apk
cp ./datapac/eur.rus/build/outputs/apk/debug/eur.rus-debug.apk ./apk/kwik-efis-datapac-eur.rus.apk
cp ./datapac/sah.jap/build/outputs/apk/debug/sah.jap-debug.apk ./apk/kwik-efis-datapac-sah.jap.apk
cp ./datapac/pan.arg/build/outputs/apk/debug/pan.arg-debug.apk ./apk/kwik-efis-datapac-pan.arg.apk
cp ./CHANGELOG.md ./apk/CHANGELOG.md

exit

