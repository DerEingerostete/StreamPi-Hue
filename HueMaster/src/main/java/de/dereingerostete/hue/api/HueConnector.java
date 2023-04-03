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

import com.stream_pi.action_api.actionproperty.ServerProperties;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import de.dereingerostete.hue.interal.HueStartupUtils;
import de.dereingerostete.hue.interal.Utils;
import io.github.zeroone3010.yahueapi.Hue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
public class HueConnector implements Runnable {
    private final @Nullable Consumer<ConnectResult> onConnected;
    private final @Nullable Runnable onConnectFail;
    private final @NotNull Logger logger;
    private @Nullable String bridgeIp, apiKey, appName;
    private boolean connecting;

    public void runAsync() {
        Thread thread = new Thread(this);
        thread.setName("HueConnector");
        thread.setDaemon(false); //Should be true but check for errors
        thread.start();
    }

    @Override
    public void run() {
        ConnectResult result = connect();
        if (result != null && onConnected != null) onConnected.accept(result);
        else if (result == null && onConnectFail != null) onConnectFail.run();
        connecting = false;
    }

    @Nullable
    public ConnectResult connect() {
        getLogger().info("Connecting to Hue");
        boolean verifyIp = true;
        try {
            if (bridgeIp == null) {
                getLogger().info("Discovering bridge...");
                bridgeIp = HueStartupUtils.discoverBridgeIp();
                if (bridgeIp == null) {
                    getLogger().warning("No Hue bridge was discovered");
                    Utils.showAlert(
                            "Hue connection failed",
                            "No Hue Bridge was discovered!\nPlease manually enter one.",
                            StreamPiAlertType.INFORMATION
                    );
                    return null;
                }

                getLogger().info("Bridge discovered: " + bridgeIp);
                verifyIp = false;
            }
        } catch (Exception exception) {
            logWarning("Failed to discover Hue Bridge IP",
                    "Please manually enter one.", false, exception);
            return null;
        }

        try {
            if (verifyIp && !HueStartupUtils.isBridge(bridgeIp)) {
                getLogger().log(Level.WARNING, "IP address does not belong to a bridge");
                Utils.showAlert(
                        "Hue connection failed",
                        "The specified IP address does not belong to a Hue bridge!",
                        StreamPiAlertType.WARNING
                );
                return null;
            }
        } catch (ExecutionException | InterruptedException exception) {
            logWarning("Failed to validate IP address", null, true, exception);
            return null;
        }

        boolean verifyKey = true;
        try {
            if (apiKey == null) {
                getLogger().info("No api key was configured");
                if (appName == null) throw new IllegalStateException("No app name is defined");

                apiKey = HueStartupUtils.authenticate(Objects.requireNonNull(bridgeIp), appName);
                if (apiKey == null) {
                    getLogger().warning("Failed to authorize with Hue Bridge: Returned null as ApiKey");
                    Utils.showAlert("Hue connection failed", "Failed to authorize with Hue Bridge", StreamPiAlertType.WARNING);
                    return null;
                }
                verifyKey = false;
            }
        } catch (Exception exception) {
            logWarning("Failed to authorize with Hue Bridge", null, true, exception);
            return null;
        }

        try {
            if (verifyKey && !HueStartupUtils.isAuthenticated(Objects.requireNonNull(bridgeIp), apiKey)) {
                getLogger().log(Level.WARNING, "Hue api key is invalid");
                Utils.showAlert(
                        "Authentication failed",
                        "The entered api key is not valid for this bridge",
                        StreamPiAlertType.WARNING
                );
                return null;
            }
        } catch (IOException exception) {
            logWarning("Failed to validate api key", null, true, exception);
            return null;
        }

        Hue hue = new Hue(bridgeIp, apiKey);
        hue.setCaching(true);

        String bridgeName;
        try {
            bridgeName = hue.getRaw().getConfig().getName();
        } catch (RuntimeException exception) {
            bridgeName = "Unknown";
        }

        getLogger().info("Connected to bridge: " + bridgeIp + '/' + bridgeName);
        return new ConnectResult(hue, Objects.requireNonNull(apiKey),
                Objects.requireNonNull(bridgeIp), !verifyKey, !verifyIp);
    }

    private void logWarning(@NotNull String message, @Nullable String alertAppend,
                            boolean appendReason, @NotNull Throwable throwable) {
        getLogger().log(Level.WARNING, message, throwable);

        StringBuilder builder = new StringBuilder(message);
        if (alertAppend != null) builder.append('\n').append(alertAppend);
        if (appendReason) builder.append('\n').append(Utils.getReason(throwable));
        String context = builder.toString();

        StreamPiAlert alert = new StreamPiAlert(context, StreamPiAlertType.WARNING);
        alert.show();
    }

    @Data
    public static class ConnectResult {
        private final @NotNull Hue instance;
        private final @NotNull String apiKey;
        private final @NotNull String bridgeIp;
        private final boolean updatedKey, updatedIp;

        public void updateProperties(@NotNull String apiKeyName, @NotNull String bridgeIpName) {
            ExternalPlugin plugin = Objects.requireNonNull(HueAPI.getInstance().getPlugin());
            try {
                ServerProperties properties = plugin.getServerProperties();
                Property apiKeyProperty = properties.getSingleProperty(apiKeyName);
                if (updatedKey) apiKeyProperty.setStringValue(apiKey);

                Property bridgeIpProperty = properties.getSingleProperty(bridgeIpName);
                if (updatedIp) bridgeIpProperty.setStringValue(bridgeIp);
                if (updatedIp || updatedKey) plugin.saveServerProperties();
            } catch (MinorException exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to save ApiKey", exception);
            }
        }

    }

}
