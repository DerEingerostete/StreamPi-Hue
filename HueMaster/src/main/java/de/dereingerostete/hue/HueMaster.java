/*
 * StreamPi-Hue: A plugin for StreamPi to control your Hue lights
 * Copyright (C) 2023  DerEingerostete
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.dereingerostete.hue;

import com.stream_pi.action_api.actionproperty.property.ControlType;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import de.dereingerostete.hue.api.HueAPI;
import de.dereingerostete.hue.api.HueConnector;
import de.dereingerostete.hue.interal.Utils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class HueMaster extends NormalAction {
    private final @NotNull HueAPI hueAPI = HueAPI.getInstance();
    private final @NotNull Button connectionButton;
    private @Nullable HueConnector connector;
    private boolean firstRun;

    public HueMaster() {
        setName("Hue Plugin");
        setCategory("Hue");
        setVisibilityInPluginsPane(false);
        setAuthor("DerEingerostete");
        setHelpLink("https://github.com/DerEingerostete/StreamPi-Hue");
        setVersion(HueAPI.VERSION);

        this.firstRun = true;
        this.connectionButton = new Button("Connect");
        setServerSettingsButtonBar(connectionButton);
        hueAPI.setPlugin(this);
    }

    @Override
    public void initProperties() throws MinorException {
        Property urlProperty = new Property("ip", Type.STRING);
        urlProperty.setDisplayName("Bridge IP");
        urlProperty.setCanBeBlank(true);

        Property keyProperty = new Property("api_key", Type.STRING);
        keyProperty.setControlType(ControlType.TEXT_FIELD_MASKED);
        keyProperty.setDisplayName("API Key");
        keyProperty.setCanBeBlank(true);

        Property appNameProperty = new Property("app_name", Type.STRING);
        appNameProperty.setDisplayName("App Name");
        appNameProperty.setDefaultValueStr("StreamPi");
        appNameProperty.setCanBeBlank(true);

        Property startupProperty = new Property("connect_on_startup", Type.BOOLEAN);
        startupProperty.setDisplayName("Connect On Startup");
        startupProperty.setDefaultValueBoolean(true);

        //Add all properties
        addServerProperties(urlProperty, keyProperty, appNameProperty, startupProperty);
    }

    @Override
    public void initAction() throws MinorException {
        boolean startupRun = getPropertyByName("connect_on_startup").getBoolValue();
        if (startupRun && firstRun) {
            getLogger().info("Connecting to Hue");
            connect();
        }

        String bridgeIp = getNullableProperty("ip");
        String apiKey = getNullableProperty("api_key");
        String appName = getNullableProperty("app_name");

        hueAPI.setProperties(bridgeIp, apiKey, appName);
        hueAPI.setConnectionButton(connectionButton);
        hueAPI.setLogger(getLogger());

        connectionButton.setOnAction(event -> {
            if (connector != null && connector.isConnecting()) return;
            if (hueAPI.getHue() == null) {
                connect();
            } else {
                hueAPI.setHue(null);
                setConnectionButtonText("Connect");
            }
        });

        firstRun = false;
    }

    private void connect() {
        try {
            String apiKey = getNullableProperty("api_key");
            String bridgeIp = getNullableProperty("ip");
            String appName = getNullableProperty("app_name");

            HueConnector.Builder builder = HueConnector.builder()
                    .bridgeIp(bridgeIp)
                    .apiKey(apiKey)
                    .appName(appName)
                    .logger(getLogger())
                    .onConnectFail(() -> setConnectionButtonText("Connect"))
                    .onConnected(result -> {
                        hueAPI.setHue(result.getInstance());
                        result.updateProperties("api_key", "ip");
                        setConnectionButtonText("Disconnect");
                    });
            connector = builder.build();
            connector.runAsync();
        } catch (MinorException exception) {
            getLogger().log(Level.WARNING, "Failed to load properties", exception);
            Utils.showAlert(
                    "Hue connection failed",
                    "Failed to connect to Hue Bridge\nFailed to load properties",
                    StreamPiAlertType.WARNING
            );
        }
    }

    @Nullable
    private String getNullableProperty(@NotNull String name) throws MinorException {
        Property property = getPropertyByName(name);
        String string = property.getStringValue();
        if (string == null || string.isBlank()) return null;
        else return string;
    }

    @NotNull
    private Property getPropertyByName(@NotNull String name) throws MinorException {
        return getServerProperties().getSingleProperty(name);
    }

    private void setConnectionButtonText(@NotNull String text) {
        Platform.runLater(() -> connectionButton.setText(text));
    }

}