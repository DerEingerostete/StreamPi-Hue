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

package de.dereingerostete.hue.api;

import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.util.version.Version;
import io.github.zeroone3010.yahueapi.Hue;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.Room;
import io.github.zeroone3010.yahueapi.domain.Root;
import io.github.zeroone3010.yahueapi.domain.Scene;
import javafx.scene.control.Button;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Data
public class HueAPI {
    public static final @NotNull Version VERSION = new Version(1, 0, 0);
    private static final @NotNull HueAPI INSTANCE = new HueAPI();
    private final @NotNull LightRefresher refresher;
    private @Nullable ExternalPlugin plugin;
    private @Nullable Button connectionButton;
    private @Nullable HueConnector connector;
    private @Nullable Logger logger;
    private @Nullable Hue hue;

    //Set by HueMaster
    private @Nullable String bridgeIp, apiKey, appName;
    private int refreshTime;

    public HueAPI() {
        this.refresher = new LightRefresher();
    }

    public void connect(@NotNull Runnable onConnected) {
        if (logger == null) throw new IllegalStateException("Logger is null");
        if (connector != null && connector.isConnecting()) {
            logger.warning("Already connecting to Hue bridge");
            return;
        }

        HueConnector.Builder builder = HueConnector.builder()
                .bridgeIp(bridgeIp)
                .apiKey(apiKey)
                .appName(appName)
                .logger(logger)
                .onConnected(result -> {
                    this.hue = result.getInstance();
                    result.updateProperties("api_key", "ip");
                    refresher.start(refreshTime, TimeUnit.SECONDS);
                    onConnected.run();
                });
        connector = builder.build();
        connector.runAsync();
    }

    public void disconnect() {
        refresher.stop();
        this.hue = null;
    }

    public void setProperties(@Nullable String bridgeIp, @Nullable String apiKey,
                              @Nullable String appName, int refreshTimer) {
        this.bridgeIp = bridgeIp;
        this.apiKey = apiKey;
        this.appName = appName;
        this.refreshTime = refreshTimer;
    }

    public boolean isConnected() {
        return hue != null;
    }

    @Nullable
    public Light getLight(@NotNull String name) {
        if (hue == null) throw new IllegalStateException("Hue is null");
        hue.refresh();
        Room room0 = hue.getAllLights();
        return room0.getLightByName(name).orElse(null);
    }

    @Nullable
    public Room getRoom(@NotNull String name) {
        if (hue == null) throw new IllegalStateException("Hue is null");
        return hue.getRoomByName(name).orElse(null);
    }

    @Nullable
    public String getSceneId(@NotNull String name) {
        if (hue == null) throw new IllegalStateException("Hue is null");
        hue.refresh();
        Root root = Objects.requireNonNull(hue).getRaw();
        if (root == null) {
            hue.refresh();
            root = hue.getRaw();
            if (root == null) return null;
        }

        Map.Entry<String, Scene> entry = root.getScenes().entrySet()
                .stream()
                .filter(current -> current.getValue().getName().equalsIgnoreCase(name))
                .findAny().orElse(null);
        return entry == null ? null : entry.getKey();
    }

    @NotNull
    public Room getAllLightsRoom() {
        if (hue == null) throw new IllegalStateException("Hue is null");
        hue.refresh();
        return hue.getAllLights();
    }

    public void refreshToggles(@Nullable String exemptId) {
        refresher.refresh(exemptId);
    }

    @NotNull
    public static HueAPI getInstance() {
        return INSTANCE;
    }

}
