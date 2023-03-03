package de.dereingerostete.hue.api;

import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.util.version.Version;
import io.github.zeroone3010.yahueapi.Hue;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.Room;
import javafx.scene.control.Button;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.logging.Logger;

@Data
public class HueAPI {
    public static final @NotNull Version VERSION = new Version(1, 0, 0);
    private static final @NotNull HueAPI INSTANCE = new HueAPI();
    private @Nullable ExternalPlugin plugin;
    private @Nullable String bridgeIp, apiKey, appName;
    private @Nullable Button connectionButton;
    private @Nullable HueConnector connector;
    private @Nullable Logger logger;
    private @Nullable Hue hue;

    public void connect(@NotNull Runnable onConnected) {
        if (connector != null && connector.isConnecting()) return;
        HueConnector.Builder builder = HueConnector.builder()
                .bridgeIp(bridgeIp)
                .apiKey(apiKey)
                .appName(appName)
                .logger(Objects.requireNonNull(logger))
                .onConnected(result -> {
                    this.hue = result.getInstance();
                    result.updateProperties("api_key", "ip");
                    onConnected.run();
                });
        connector = builder.build();
        connector.runAsync();
    }

    public void setProperties(@Nullable String bridgeIp, @Nullable String apiKey, @Nullable String appName) {
        this.bridgeIp = bridgeIp;
        this.apiKey = apiKey;
        this.appName = appName;
    }

    public boolean isConnected() {
        return hue != null;
    }

    @Nullable
    public Light getLight(@NotNull String name) {
        Room room0 = Objects.requireNonNull(hue).getAllLights();
        return room0.getLightByName(name).orElse(null);
    }

    @Nullable
    public Room getRoom(@NotNull String name) {
        return Objects.requireNonNull(hue).getRoomByName(name).orElse(null);
    }

    @NotNull
    public static HueAPI getInstance() {
        return INSTANCE;
    }

}
