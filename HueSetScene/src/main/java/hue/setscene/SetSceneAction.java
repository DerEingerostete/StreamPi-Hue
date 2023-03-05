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

package hue.setscene;

import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.util.exception.MinorException;
import de.dereingerostete.hue.api.HueAPI;
import de.dereingerostete.hue.api.action.NormalHueAction;
import io.github.zeroone3010.yahueapi.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class SetSceneAction extends NormalHueAction {
    private final @NotNull List<ListValue> onValues;
    private @Nullable State state;

    public SetSceneAction() {
        setName("Set Scene");
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

        Property sceneProperty = new Property("scene_name", Type.STRING);
        sceneProperty.setDisplayName("Scene Name");
        sceneProperty.setDefaultValueStr("Name of the Scene");
        sceneProperty.setCanBeBlank(false);
        addClientProperties(onProperty, sceneProperty);
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
        String sceneId = getNullableProperty("scene_name");
        if (sceneId != null) sceneId = hueAPI.getSceneId(sceneId);
        this.state = State.builder().scene(sceneId).on(onValue);
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
