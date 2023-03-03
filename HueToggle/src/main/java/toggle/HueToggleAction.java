package toggle;

import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.ToggleAction;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import de.dereingerostete.hue.api.HueAPI;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.Room;
import io.github.zeroone3010.yahueapi.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class HueToggleAction extends ToggleAction {
    private final @NotNull HueAPI hueAPI = HueAPI.getInstance();
    private final @NotNull List<ListValue> lightType;
    private @Nullable Light light;
    private @Nullable Room room;

    public HueToggleAction() {
        setName("Toggle Light");
        setCategory("Hue");
        setAuthor("DerEingerostete");
        setHelpLink("https://github.com/DerEingerostete/StreamPi-Hue");
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

        Property nameProperty = new Property("name", Type.STRING);
        nameProperty.setDisplayName("Light/Room Name");
        nameProperty.setCanBeBlank(false);

        Property transitionProperty = new Property("transition_time", Type.DOUBLE);
        transitionProperty.setDisplayName("Transition Time (Seconds)");
        transitionProperty.setDefaultValueDouble(0);
        transitionProperty.setMinDoubleValue(0);
        transitionProperty.setMaxDoubleValue(1000);

        Property refreshOnLoadProperty = new Property("refresh_on_load", Type.BOOLEAN);
        refreshOnLoadProperty.setDisplayName("Load current state on start");
        refreshOnLoadProperty.setDefaultValueBoolean(true);

        Property autoConnectProperty = new Property("auto_connect", Type.BOOLEAN);
        autoConnectProperty.setDisplayName("Auto Connect if not connected");
        autoConnectProperty.setDefaultValueBoolean(true);

        //Add all properties
        addClientProperties(typeProperty, nameProperty, transitionProperty,
                refreshOnLoadProperty, autoConnectProperty);
    }

    @Override
    public void onActionSavedFromServer() throws MinorException {
        loadLight();
    }

    @Override
    public void onServerPropertiesSavedByUser() throws MinorException {
        loadLight();
    }

    @Override
    public void onClientConnected() throws MinorException {
        Property property = getPropertyByName("refresh_on_load");
        if (!property.getBoolValue()) return;

        if (light == null && room == null) {
            loadLight();
            if (light == null && room == null) {
                getLogger().warning("HueToggle: No light or room is set");
                return;
            }
        }

        boolean isOn;
        if (light != null) isOn = light.isOn();
        else isOn = room.isAnyOn();
        setCurrentStatus(!isOn);
        getLogger().info("HueToggle: Set toggle to: " + isOn);
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
        if (light == null && room == null) {
            loadLight();
            if (light == null && room == null) {
                getLogger().warning("HueToggle: No light or room is set");
                return;
            }
        }

        int transitionTime;
        try {
            Property tranistionProperty = getPropertyByName("transition_time");
            transitionTime = (int) (tranistionProperty.getDoubleValue() * 10D);
        } catch (Exception exception) {
            getLogger().log(Level.WARNING, "HueToggle: No transition time set: " + exception.getMessage(), exception);
            return;
        }

        State.Builder step = (State.Builder) State.builder();
        State state;
        if (transitionTime > 0) {
            step.transitionTime(transitionTime);
        }
        state = step.on(turnOn);

        Runnable runnable = () -> {
            if (light != null) light.setState(state);
            else if (room != null) room.setState(state);
            else getLogger().warning("Cannot toggle light: No light or room found");
        };

        if (hueAPI.isConnected()) {
            runnable.run();
            return;
        }

        boolean autoConnect = getPropertyByName("auto_connect").getBoolValue();
        if (autoConnect) hueAPI.connect(runnable);
        else getLogger().warning("Hue is not connecting. Cannot toggle");
    }

    private void loadLight() throws MinorException {
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

        Property property = getPropertyByName("name");
        String name = property.getStringValue();
        if (name == null || name.isBlank()) name = null;

        //getLogger().info("Updating light of type '" + type + "' with name '" + name + "'");
        if (name == null) {
            getLogger().warning("HueToggle: No name set");
            return;
        }

        try {
            switch (type) {
                case 1:
                    light = hueAPI.getLight(name);
                    break;
                case 2:
                    room = hueAPI.getRoom(name);
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
    private Property getPropertyByName(@NotNull String name) throws MinorException {
        return getClientProperties().getSingleProperty(name);
    }

}
