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

package player.efis.pfd;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PFDPrefSettings extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}

/*
Sure, here is a short example:
EditTextPreference etp = (EditTextPreference) findPreference("the_pref_key");
etp.setSummary("New summary");

This requires that you display your preferences either from a PreferenceActivity or from a PreferenceFragment, since findPreference() is a method of these classes. You most likely do that already. 

To change the summary every time the user changes the actual preference, use a OnPreferenceChangeListener and check if the relevant key changed in the callback. After it has changed, just edit the summary like above.*/
/*


import android.os.Bundle;
import android.preference.PreferenceActivity;

public class UserSettingActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

	}
}
*/