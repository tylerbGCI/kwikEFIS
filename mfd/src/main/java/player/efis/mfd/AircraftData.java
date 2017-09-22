/*
 * Copyright (C) 2017 Player One
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package player.efis.mfd;

enum AircraftModel
{
    GENERIC,
    AZTEC,
    CRICRI,
    CRUZ,
    J160,
    LGEZ,
    M20J,
    PA28,
    RV6,
    RV7,
    RV8,
    W10
}

public class AircraftData
{
    static AircraftModel mAircraftModel = AircraftModel.RV8;

    static int Vs0 = 20;  // Stall, flap extended
    static int Vs1 = 30;  // Stall, flap retracted
    static int Vx  = 40;  // Best angle climb
    static int Vy  = 50;  // Best rate climb
    static int Vfe = 60;  // Flaps extension
    static int Va  = 70;  // Maneuvering
    static int Vno = 80;  // Max structural cruise
    static int Vne = 90;  // Never exceed

    //-------------------------------------------------------------------------
    // Define the various built-in aircraft definitions
    //
    public static void setAircraftData(String model)
    {
        try {
            mAircraftModel = AircraftModel.valueOf(model);
        }
        //catch (IllegalArgumentException e) {
        catch (Exception e) {
            mAircraftModel = AircraftModel.RV8;
        }

        // Vs0  Stall, flap extended
        // Vs1  Stall, flap retracted
        // Vx   Best angle climb
        // Vy   Best rate climb
        // Vfe  Flaps extension
        // Va   Maneuvering
        // Vno  Max structural cruise
        // Vne  Never exceed
        //
        // White Arc  Vs0 - Vfe
        // Green Arc  Vs1 - Vno
        // Yellow Arc Vno - Vne

        switch (mAircraftModel) {
            // V Speeds for various aircraft models
            case GENERIC:
                // Ultralight
                Vs0 = 20;  // Stall, flap extended
                Vs1 = 30;  // Stall, flap retracted
                Vx = 40;   // Best angle climb
                Vy = 50;   // Best rate climb
                Vfe = 60;  // Flaps extension
                Va = 70;   // Maneuvering
                Vno = 80;  // Max structural cruise
                Vne = 90;  // Never exceed
                break;

            case AZTEC:
                // Colomban CriCri
                Vs0 = 61;   // Stall, flap extended
                Vs1 = 66;   // Stall, flap retracted
                Vx = 93;    // Best angle climb
                Vy = 102;   // Best rate climb
                Vfe = 140;  // Flaps extension
                Va = 129;   // Maneuvering
                Vno = 172;  // Max structural cruise
                Vne = 216;  // Never exceed
                break;

            case CRICRI:
                // Colomban CriCri
                Vs0 = 39;  // Stall, flap extended
                Vs1 = 49;  // Stall, flap retracted
                Vx = 56;   // Best angle climb
                Vy = 68;   // Best rate climb
                Vfe = 70;  // Flaps extension
                Va = 85;   // Maneuvering
                Vno = 100; // Max structural cruise
                Vne = 140; // Never exceed
                break;

            case CRUZ:
                // PiperSport Cruzer
                Vs0 = 32;  // Stall, flap extended
                Vs1 = 39;  // Stall, flap retracted
                Vx = 56;   // Best angle climb
                Vy = 62;   // Best rate climb
                Vfe = 75;  // Flaps extension
                Va = 88;   // Maneuvering
                Vno = 108; // Max structural cruise
                Vne = 138; // Never exceed
                break;

            case J160:
                // Jabiru J160-C
                Vs0 = 40;   // Stall, flap extended
                Vs1 = 45;   // Stall, flap retracted
                Vx = 65;    // Best angle climb
                Vy = 68;    // Best rate climb
                Vfe = 80;   // Flaps extension
                Va = 90;    // Maneuvering
                Vno = 108;  // Max structural cruise
                Vne = 140;  // Never exceed
                break;

            case LGEZ:
                // RV-8A
                Vs0 = 56;   // Stall, flap extended
                Vs1 = 56;   // Stall, flap retracted
                Vx = 72;    // Best angle climb
                Vy = 90;    // Best rate climb
                Vfe = 85;   // Flaps extension
                Va = 120;   // Maneuvering
                Vno = 161;  // Max structural cruise
                Vne = 200;  // Never exceed
                break;

            case M20J:
                Vs0 = 53;   // Stall, flap extended
                Vs1 = 53;   // Stall, flap retracted
                Vx = 66;    // Best angle climb
                Vy = 85;    // Best rate climb
                Vfe = 115;  // Flaps extension
                Va = 120;   // Maneuvering
                Vno = 152;  // Max structural cruise
                Vne = 174;  // Never exceed
                break;

            case PA28:
                // Piper PA28 Archer II
                Vs0 = 49;   // Stall, flap extended
                Vs1 = 55;   // Stall, flap retracted
                Vx = 64;    // Best angle climb
                Vy = 76;    // Best rate climb
                Vfe = 102;  // Flaps extension
                Va = 89;    // Maneuvering
                Vno = 125;  // Max structural cruise
                Vne = 154;  // Never exceed
                break;

            case RV6:
            case RV7:
            case RV8:
                // RV-6,7,8
                Vs0 = 51;    // Stall, flap extended
                Vs1 = 56;    // Stall, flap retracted
                Vx = 72;     // Best angle climb
                Vy = 90;     // Best rate climb
                Vfe = 85;    // Flaps extension
                Va = 120;    // Maneuvering
                Vno = 165;   // Max structural cruise
                Vne = 200;   // Never exceed
                break;

            case W10:
                // Witttman Tailwind
                Vs0 = 48;  // Stall, flap extended
                Vs1 = 55;  // Stall, flap retracted
                Vx = 90;   // Best angle climb - tbd
                Vy = 104;  // Best rate climb
                Vfe = 91;  // Flaps extension
                Va = 130;  // Maneuvering
                Vno = 155; // Max structural cruise - tbd
                Vne = 174; // Never exceed
                break;

            default:
                // RV-8A
                Vs0 = 51;    // Stall, flap extended
                Vs1 = 56;    // Stall, flap retracted
                Vx = 72;     // Best angle climb
                Vy = 90;     // Best rate climb
                Vfe = 85;    // Flaps extension
                Va = 120;    // Maneuvering
                Vno = 165;   // Max structural cruise
                Vne = 200;   // Never exceed
                break;
        }
    }
}