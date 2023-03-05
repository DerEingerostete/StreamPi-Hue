package hue.setalert;

import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.util.exception.MinorException;
import de.dereingerostete.hue.api.EnumUtils;
import de.dereingerostete.hue.api.HueAPI;
import de.dereingerostete.hue.api.action.NormalHueAction;
import io.github.zeroone3010.yahueapi.AlertType;
import io.github.zeroone3010.yahueapi.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SetAlertAction extends NormalHueAction {
    private final @NotNull List<ListValue> alertValues;
    private @Nullable State state;

    public SetAlertAction() {
        setName("Set Alert");
        setCategory("Hue");
        setAuthor("DerEingerostete");
        setHelpLink("https://github.com/DerEingerostete/StreamPi-Hue");
        setVisibilityInServerSettingsPane(false);
        setVersion(HueAPI.VERSION);

        alertValues = new ArrayList<>();
        for (AlertType alertType : AlertType.values()) {
            if (alertType == AlertType.UNKNOWN) break;
            String name = EnumUtils.formatName(alertType);
            alertValues.add(new ListValue(alertType, name));
        }
    }

    @Override
    public void initProperties() throws MinorException {
        super.initProperties();
        Property onProperty = new Property("alert_type", Type.LIST);
        onProperty.setDisplayName("Alert Type");
        onProperty.setListValue(alertValues);
        onProperty.setDefaultValueList(0);
        addClientProperties(onProperty);
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
        Property property = getPropertyByName("alert_type");
        AlertType type = (AlertType) property.getSelectedListValue().getName();
        this.state = State.builder().alert(type);
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