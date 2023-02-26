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
        appNameProperty.setControlType(ControlType.TEXT_FIELD_MASKED);
        appNameProperty.setDisplayName("App Name");
        appNameProperty.setDefaultValueStr("StreamPi");
        appNameProperty.setCanBeBlank(true);

        Property startupProperty = new Property("connect_on_startup", Type.BOOLEAN);
        startupProperty.setDisplayName("Connect On Startup");
        startupProperty.setDefaultValueBoolean(false);

        //Add all properties
        addServerProperties(urlProperty, keyProperty, startupProperty);
    }

    @Override
    public void initAction() throws MinorException {
        boolean startupRun = getPropertyByName("connect_on_startup").getBoolValue();
        if (startupRun && firstRun) {
            connect();
        }

        String bridgeIp = getPropertyByName("ip").getStringValue();
        String apiKey = getPropertyByName("api_key").getRawValue();
        String appName = getPropertyByName("app_name").getStringValue();
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
            String bridgeIp = getPropertyByName("ip").getStringValue();
            String apiKey = getPropertyByName("api_key").getRawValue();
            String appName = getPropertyByName("app_name").getStringValue();

            HueConnector.Builder builder = HueConnector.builder()
                    .bridgeIp(bridgeIp)
                    .apiKey(apiKey)
                    .appName(appName)
                    .logger(getLogger())
                    .onConnectFail(() -> setConnectionButtonText("Connect"))
                    .onConnected(hueInstance -> {
                        hueAPI.setHue(hueInstance);
                        setConnectionButtonText("Disconnect");
                    });
            connector = builder.build();
            connector.runAsync();
        } catch (MinorException exception) {
            getLogger().log(Level.WARNING, "Failed to load properties", exception);
            Utils.showAlert("Failed to connect to Hue Bridge\nFailed to load properties", StreamPiAlertType.WARNING);
        }
    }

    @NotNull
    private Property getPropertyByName(@NotNull String name) throws MinorException {
        return getServerProperties().getSingleProperty(name);
    }

    private void setConnectionButtonText(@NotNull String text) {
        Platform.runLater(() -> connectionButton.setText(text));
    }

}