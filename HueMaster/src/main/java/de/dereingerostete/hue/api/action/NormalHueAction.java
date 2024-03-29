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

package de.dereingerostete.hue.api.action;

import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import de.dereingerostete.hue.api.HueAPI;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.Room;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public abstract class NormalHueAction extends NormalAction {
    protected final @NotNull HueAPI hueAPI = HueAPI.getInstance();
    protected final @NotNull List<ListValue> lightType;
    protected @Nullable Light light;
    protected @Nullable Room room;

    public NormalHueAction() {
        lightType = Arrays.asList(
                new ListValue(1, "Light"),
                new ListValue(2, "Room"),
                new ListValue(99, "All")
        );
    }

    @Override
    public void initProperties() throws MinorException {
        Property typeProperty = new Property("type", Type.LIST);
        typeProperty.setDisplayName("Type");
        typeProperty.setListValue(lightType);
        typeProperty.setDefaultValueList(0);

        Property nameProperty = new Property("name", Type.STRING);
        nameProperty.setDisplayName("Light/Room Name");
        nameProperty.setCanBeBlank(true);

        Property autoConnectProperty = new Property("auto_connect", Type.BOOLEAN);
        autoConnectProperty.setDisplayName("Auto Connect if not connected");
        autoConnectProperty.setDefaultValueBoolean(true);

        addClientProperties(typeProperty, nameProperty, autoConnectProperty);
    }

    @Override
    public void onActionClicked() throws MinorException {
        if (light == null && room == null) {
            loadLight();
            if (light == null && room == null) {
                getLogger().warning("HueToggle: No light or room is set");
                return;
            }
        }

        Runnable runnable = onClicked();
        if (runnable == null) {
            getLogger().info(getClass().getSimpleName() + ": Runnable is null. Will not execute");
            return;
        }

        if (hueAPI.isConnected()) {
            runnable.run();
            return;
        }

        boolean autoConnect = getPropertyByName("auto_connect").getBoolValue();
        if (autoConnect) hueAPI.connect(runnable);
        else getLogger().warning("Hue is not connecting. Cannot toggle");
    }

    @Nullable
    public abstract Runnable onClicked() throws MinorException;

    @Override
    public void onActionSavedFromServer() throws MinorException {
        loadLight();
    }

    protected void loadLight() throws MinorException {
        if (!hueAPI.isConnected()) {
            getLogger().warning("Could not load light: API is not connected");
            return;
        }

        int type;
        String typeName;
        try {
            Property typeProperty = getPropertyByName("type");
            ListValue selectedValue = typeProperty.getSelectedListValue();
            type = (int) selectedValue.getName();
            typeName = selectedValue.getDisplayName().toLowerCase();
        } catch (Exception exception) {
            getLogger().log(Level.WARNING, "HueToggle: No type set: " + exception.getMessage(), exception);
            return;
        }

        String name = getNullableProperty("name");
        if (name == null && type != 99) return;

        try {
            switch (type) {
                case 1:
                    light = hueAPI.getLight(name);
                    break;
                case 2:
                    room = hueAPI.getRoom(name);
                    break;
                case 99:
                    room = hueAPI.getAllLightsRoom();
                    break;
            }
        } catch (Exception exception) {
            getLogger().log(Level.WARNING, "Failed to load room or light", exception);
            return;
        }

        if (room == null && light == null) {
            getLogger().warning("Failed to load Hue item");
            StreamPiAlert alert = new StreamPiAlert(
                    "Hue",
                    "No " + typeName + " found with the name '" + name + "'",
                    StreamPiAlertType.WARNING
            );
            alert.show();
        }
    }

    @NotNull
    protected Property getPropertyByName(@NotNull String name) throws MinorException {
        return getClientProperties().getSingleProperty(name);
    }

    @Nullable
    protected String getNullableProperty(@NotNull String name) throws MinorException {
        Property property = getPropertyByName(name);
        String string = property.getStringValue();
        if (string == null || string.isBlank()) return null;
        else return string;
    }

}
