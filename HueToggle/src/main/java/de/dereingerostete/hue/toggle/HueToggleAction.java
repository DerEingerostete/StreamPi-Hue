package de.dereingerostete.hue.toggle;

import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.ToggleAction;
import com.stream_pi.util.exception.MinorException;
import de.dereingerostete.hue.api.HueAPI;
import io.github.zeroone3010.yahueapi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class HueToggleAction extends ToggleAction {
    private final @NotNull HueAPI hueAPI = HueAPI.getInstance();
    private final @NotNull List<ListValue> lightType;
    private @Nullable Light light;
    private @Nullable Room room;

    public HueToggleAction() {
        setName("Toggle Light");
        setCategory("Hue");
        setAuthor("DerEingerostete");
        setVisibilityInServerSettingsPane(false);
        setVersion(HueAPI.VERSION);

        lightType = Arrays.asList(
                new ListValue(1, "Light"),
                new ListValue(2, "Room")
        );
    }

    @Override
    public void initProperties() throws MinorException {
        Property typeProperty = new Property("type", Type.LIST);
        typeProperty.setDisplayName("Type");
        typeProperty.setListValue(lightType);
        typeProperty.setDefaultValueList(0);

        Property nameProperty = new Property("name", Type.LIST);
        nameProperty.setDisplayName("Light/Room Name");

        Property autoConnectProperty = new Property("auto_connect", Type.BOOLEAN);
        autoConnectProperty.setDisplayName("Auto Connect if not connected");
        autoConnectProperty.setDefaultValueBoolean(true);

        //Add all properties
        addClientProperties(nameProperty, typeProperty, autoConnectProperty);
    }

    @Override
    public void initAction() throws MinorException {
        loadLights();
    }

    @Override
    public void onActionSavedFromServer() throws MinorException {
        loadLights();
    }

    @Override
    public void onToggleOn() throws MinorException {
        setToggle(true);
    }

    @Override
    public void onToggleOff() throws MinorException {
        setToggle(false);
    }

    private void setToggle(boolean turnOn) throws MinorException {
        if (light == null && room == null) return;
        Runnable runnable = () -> {
            if (turnOn) {
                if (light != null) light.turnOn();
                else if (room != null) room.turnOn();
            } else {
                if (light != null) light.turnOff();
                else if (room != null) room.turnOff();
            }
        };

        if (hueAPI.isConnected()) {
            runnable.run();
            return;
        }

        boolean autoConnect = getPropertyByName("auto_connect").getBoolValue();
        if (autoConnect) hueAPI.connect(runnable);
    }

    private void loadLights() throws MinorException {
        Property property = getPropertyByName("name");
        int type = (int) getPropertyByName("type").getSelectedListValue().getName();

        String id;
        try {
            ListValue selected = property.getSelectedListValue();
            id = selected.getName().toString();
        } catch (RuntimeException exception) {
            id = null;
        }

        if (id != null) {
            if (type == 1) light = hueAPI.getLight(id);
            else room = hueAPI.getRoom(id);
            if (room == null || light == null) {
                getLogger().warning("Failed to load Hue item with id " + id + " and type " + type);
            }
        }

        List<ListValue> values;
        if (type == 1) values = hueAPI.getLightNames();
        else values = hueAPI.getRoomNames();
        property.setListValue(values);
    }

    @NotNull
    private Property getPropertyByName(@NotNull String name) throws MinorException {
        return getServerProperties().getSingleProperty(name);
    }

}
