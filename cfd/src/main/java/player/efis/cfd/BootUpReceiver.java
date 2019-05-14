/*
 * Copyright (C) 2016 Player One
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


package player.efis.cfd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//-----------------------------------------------------------------------------
// This is the mechanism to austo start the kwik EFIS application when the
// device boots up.


public class BootUpReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) 
    {
            Intent i = new Intent(context, CFDMainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);  
    }
}

/*
This section must be added to he AndroidManifest file

////////////// AUTOSTART CODE
<receiver android:enabled="true" android:name=".BootUpReceiver"
        android:permission="android.permission.RECEIVE_BOOT_COMPLETED">

        <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <category android:name="android.intent.category.DEFAULT" />
        </intent-filter>
</receiver>
[..]
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
[..]

public class BootUpReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
                Intent i = new Intent(context, MyActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
        }

}
////////////// AUTOSTART CODE
*/
