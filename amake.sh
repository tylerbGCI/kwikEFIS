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
cp ./datapac/build/outputs/apk/eur_rus/debug/datapac-eur_rus-debug.apk ./apk/kwik-efis-datapac-eur.rus.apk
cp ./datapac/build/outputs/apk/pan_arg/debug/datapac-pan_arg-debug.apk ./apk/kwik-efis-datapac-pan.arg.apk
cp ./datapac/build/outputs/apk/sah_jap/debug/datapac-sah_jap-debug.apk ./apk/kwik-efis-datapac-sah.jap.apk
cp ./datapac/build/outputs/apk/usa_can/debug/datapac-usa_can-debug.apk ./apk/kwik-efis-datapac-usa.can.apk
cp ./datapac/build/outputs/apk/zar_aus/debug/datapac-zar_aus-debug.apk ./apk/kwik-efis-datapac-zar.aus.apk
cp ./CHANGELOG.md ./apk/CHANGELOG.md

exit


h:/src/kwikEFIS/datapac/build/outputs/apk/eur_rus/debug/datapac-eur_rus-debug.apk 