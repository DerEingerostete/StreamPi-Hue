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

package hue.setstate;

import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.util.exception.MinorException;
import de.dereingerostete.hue.api.HueAPI;
import de.dereingerostete.hue.api.action.NormalHueAction;
import io.github.zeroone3010.yahueapi.Color;
import io.github.zeroone3010.yahueapi.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class SetState extends NormalHueAction {
    private final @NotNull List<ListValue> onValues;
    private @Nullable State state;

    public SetState() {
        setName("Set State");
        setCategory("Hue");
        setAuthor("DerEingerostete");
        setHelpLink("https://github.com/DerEingerostete/StreamPi-Hue");
        setVisibilityInServerSettingsPane(false);
        setVersion(HueAPI.VERSION);

        onValues = Arrays.asList(
                new ListValue(true, "On"),
                new ListValue(false, "Off"),
                new ListValue(null, "Keep value")
        );
    }

    @Override
    public void initProperties() throws MinorException {
        super.initProperties();
        Property onProperty = new Property("on_value", Type.LIST);
        onProperty.setDisplayName("On/Off State");
        onProperty.setListValue(onValues);
        onProperty.setDefaultValueList(2);

        Property brightnessProperty = new Property("brightness_value", Type.INTEGER);
        brightnessProperty.setDisplayName("Brightness (1 to 254, -1 for disabled)");
        brightnessProperty.setMinIntValue(1);
        brightnessProperty.setMaxIntValue(254);
        brightnessProperty.setDefaultValueInt(-1);

        Property colorProperty = new Property("hex_color", Type.STRING);
        colorProperty.setDisplayName("Hex Color");
        colorProperty.setDefaultValueStr("#FFFFFF");
        colorProperty.setCanBeBlank(true);

        Property hueProperty = new Property("hue_value", Type.INTEGER);
        hueProperty.setDisplayName("Hue Value (-1 for disabled)");
        hueProperty.setMinIntValue(-1);
        hueProperty.setMaxIntValue(65535);
        hueProperty.setDefaultValueInt(-1);

        Property temperatureProperty = new Property("color_temperature_value", Type.INTEGER);
        temperatureProperty.setDisplayName("Color Temperature in Kelvin (-1 for disabled)");
        temperatureProperty.setMinIntValue(-1);
        temperatureProperty.setMaxIntValue(10_000);
        temperatureProperty.setDefaultValueInt(-1);

        Property saturationProperty = new Property("saturation_value", Type.INTEGER);
        saturationProperty.setDisplayName("Hue Saturation (-1 for disabled)");
        saturationProperty.setMinIntValue(-1);
        saturationProperty.setMaxIntValue(65535);
        saturationProperty.setDefaultValueInt(-1);

        Property transitionProperty = new Property("transition_time", Type.DOUBLE);
        transitionProperty.setDisplayName("Transition Time (Seconds)");
        transitionProperty.setDefaultValueDouble(0);
        transitionProperty.setMinDoubleValue(0);
        transitionProperty.setMaxDoubleValue(1000);

        //Add all properties
        addClientProperties(onProperty, brightnessProperty, colorProperty,
                hueProperty, temperatureProperty,
                saturationProperty, transitionProperty);
    }

    @Nullable
    @Override
    public Runnable onClicked() throws MinorException {
        if (state == null) loadState();
        return () -> {
            if (light != null) light.setState(state);
            else if (room != null) room.setState(state);
            else getLogger().warning("Cannot toggle light: No light or room found");
        };
    }

    protected void loadState() throws MinorException {
        Boolean onValue = (Boolean) getPropertyByName("on_value").getSelectedListValue().getName();
        int brightness = getPropertyByName("brightness_value").getIntValue();

        Color color;
        try {
            String hexColor = getNullableProperty("hex_color");
            if (hexColor != null) color = Color.of(hexColor);
            else color = null;
        } catch (IllegalArgumentException exception) {
            color = null;
        }

        int kelvin = getPropertyByName("color_temperature_value").getIntValue();
        int mired = kelvin == -1 ? -1 : Math.round(1000000.0f / kelvin);

        int hue = getPropertyByName("hue_value").getIntValue();
        int saturation = getPropertyByName("saturation_value").getIntValue();

        int transitionTime;
        try {
            Property tranistionProperty = getPropertyByName("transition_time");
            transitionTime = (int) (tranistionProperty.getDoubleValue() * 10D);
        } catch (Exception exception) {
            transitionTime = 0;
        }

        State.Builder builder = (State.Builder) State.builder()
                .color(color)
                .transitionTime(transitionTime);

        if (brightness != -1) builder.brightness(brightness);
        if (kelvin != -1) builder.colorTemperatureInMireks(mired);
        if (hue != -1) builder.hue(hue);
        if (saturation != -1) builder.saturation(saturation);
        this.state = builder.on(onValue);
    }

    @Override
    public void onServerPropertiesSavedByUser() throws MinorException {
        super.onServerPropertiesSavedByUser();
        loadState();
    }

    @Override
    public void onActionSavedFromServer() throws MinorException {
        super.onActionSavedFromServer();
        loadState();
    }

}