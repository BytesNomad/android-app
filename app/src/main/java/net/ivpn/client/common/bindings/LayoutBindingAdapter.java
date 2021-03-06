package net.ivpn.client.common.bindings;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/


import android.content.res.Resources;
import androidx.databinding.BindingAdapter;
import android.widget.LinearLayout;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.vpn.model.NetworkState;

public class LayoutBindingAdapter {
    @BindingAdapter({"currentState", "defaultState"})
    public static void setBackgroundColor(LinearLayout view, NetworkState currentState, NetworkState defaultState) {
        Resources resources = IVPNApplication.getApplication().getResources();
//        view.setBackgroundColor(resources.getColor(currentState.getBackgroundColor(defaultState)));
    }
}