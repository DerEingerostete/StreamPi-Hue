package de.dereingerostete.hue.api;

import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.util.version.Version;
import io.github.zeroone3010.yahueapi.Hue;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.Room;
import javafx.scene.control.Button;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Data
public class HueAPI {
    public static final @NotNull Version VERSION = new Version(1, 0, 0);
    private static final @NotNull HueAPI INSTANCE = new HueAPI();
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
                .onConnected(hueInstance -> {
                    this.hue = hueInstance;
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
    public Light getLight(@NotNull String id) {
        Room room0 = Objects.requireNonNull(hue).getAllLights();
        return room0.getLights().stream()
                .filter(light -> light.getId().equals(id))
                .findAny().orElse(null);
    }

    @Nullable
    public Room getRoom(@NotNull String id) {
        Collection<Room> rooms = Objects.requireNonNull(hue).getRooms();
        return rooms.stream()
                .filter(room -> room.getId().equals(id))
                .findAny().orElse(null);
    }

    @NotNull
    public List<ListValue> getLightNames() {
        Room room0 = Objects.requireNonNull(hue).getAllLights();
        return room0.getLights().stream()
                .map(light -> new ListValue(light.getId(), light.getName()))
                .toList();
    }

    @NotNull
    public List<ListValue> getRoomNames() {
        Collection<Room> rooms = Objects.requireNonNull(hue).getRooms();
        return rooms.stream()
                .map(room -> new ListValue(room.getId(), room.getName()))
                .toList();
    }

    @NotNull
    public static HueAPI getInstance() {
        return INSTANCE;
    }
}
