package net.ivpn.client.ui.network.rules;

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

import androidx.databinding.ObservableBoolean;
import android.widget.CompoundButton.OnCheckedChangeListener;

import net.ivpn.client.common.prefs.SettingsPreference;
import net.ivpn.client.vpn.local.NetworkController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class NetworkRuleViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkRuleViewModel.class);

    public final ObservableBoolean isConnectToVpnRuleApplied = new ObservableBoolean();
    public final ObservableBoolean isEnableKillSwitchRuleApplied = new ObservableBoolean();
    public final ObservableBoolean isDisconnectFromVpnRuleApplied = new ObservableBoolean();
    public final ObservableBoolean isDisableKillSwitchRuleApplied = new ObservableBoolean();

    private SettingsPreference settingsPreference;
    private NetworkController networkController;

    public final OnCheckedChangeListener connectToVpnRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Enable connect to VPN rule: " + isChecked);
        networkController.changeConnectToVpnRule(isChecked);
    };
    public final OnCheckedChangeListener enableKillSwitchRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Enable kill-switch rule: " + isChecked);
        networkController.changeEnableKillSwitchRule(isChecked);
    };
    public final OnCheckedChangeListener disconnectFromVpnRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Enable disconnect from VPN rule: " + isChecked);
        networkController.changeDisconnectFromVpnRule(isChecked);
    };
    public final OnCheckedChangeListener disableKillSwitchRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Disable kill-switch rule: " + isChecked);
        networkController.changeDisableKillSwitchRule(isChecked);
    };

    @Inject
    NetworkRuleViewModel(SettingsPreference settingsPreference, NetworkController networkController) {
        this.settingsPreference = settingsPreference;
        this.networkController = networkController;

        init();
    }

    private void init() {
        isConnectToVpnRuleApplied.set(settingsPreference.getRuleConnectToVpn());
        isEnableKillSwitchRuleApplied.set(settingsPreference.getRuleEnableKillSwitch());
        isDisconnectFromVpnRuleApplied.set(settingsPreference.getRuleDisconnectFromVpn());
        isDisableKillSwitchRuleApplied.set(settingsPreference.getRuleDisableKillSwitch());
    }
}
