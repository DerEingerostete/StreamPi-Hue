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

package de.dereingerostete.hue.interal;

import com.stream_pi.util.alert.StreamPiAlertType;
import io.github.zeroone3010.yahueapi.Hue;
import io.github.zeroone3010.yahueapi.HueBridge;
import io.github.zeroone3010.yahueapi.discovery.HueBridgeDiscoveryService;
import io.github.zeroone3010.yahueapi.discovery.HueBridgeDiscoveryService.DiscoveryMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class HueStartupUtils {

    @Nullable
    public static String discoverBridgeIp() throws ExecutionException, InterruptedException, TimeoutException {
        HueBridgeDiscoveryService service = new HueBridgeDiscoveryService();
        AtomicReference<Future<?>> futureReference = new AtomicReference<>();
        AtomicReference<HueBridge> bridgeReference = new AtomicReference<>();

        Future<?> future = service.discoverBridges((foundBridge) -> {
            if (bridgeReference.get() == null) {
                bridgeReference.set(foundBridge);
                futureReference.get().cancel(true);
            }
        }, DiscoveryMethod.MDNS);
        futureReference.set(future);
        future.get(30, TimeUnit.SECONDS);

        HueBridge bridge = bridgeReference.get();
        return bridge == null ? null : bridge.getIp();
    }

    @Nullable
    public static String authenticate(@NotNull String bridgeIp, @NotNull String appName)
            throws InterruptedException, TimeoutException, ExecutionException {
        if (!isBridge(bridgeIp)) {
            Utils.showAlert(
                    "Hue connection failed",
                    "The specified IP address does not belong to a Hue bridge!",
                    StreamPiAlertType.WARNING
            );
            return null;
        }

        Utils.showAlert(
                "Authenticate with Hue",
                "Press the push-link button on the Hue bridge you want to connect to.",
                StreamPiAlertType.INFORMATION
        );

        try {
            Hue.HueBridgeConnectionBuilder builder = Hue.hueBridgeConnectionBuilder(bridgeIp);
            return builder.initializeApiConnection(appName).get(3, TimeUnit.MINUTES);
        } catch (ExecutionException exception) {
            String reason = String.valueOf(Utils.getReason(exception)).toLowerCase();
            if (reason.contains("link button not pressed")) {
                Utils.showAlert(
                        "Hue authentication failed",
                        "Failed to connect to Hue.\nThe link button was not pressed.",
                        StreamPiAlertType.WARNING
                );
                return null;
            } else throw new ExecutionException(exception);
        }
    }

    public static boolean isAuthenticated(@NotNull String bridgeIp, @NotNull String apiKey) throws IOException {
        URL url = new URL("https://" + bridgeIp + "/api/" + apiKey + "/lights");
        try (InputStream inputStream = url.openStream()) {
            String result = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().parallel().collect(Collectors.joining("\n"));
            return !result.equals("[{\"error\":{\"type\":1,\"address\":\"/lights\",\"description\":\"unauthorized user\"}}]");
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isBridge(@NotNull String ipAddress) throws ExecutionException, InterruptedException {
        Hue.HueBridgeConnectionBuilder builder = Hue.hueBridgeConnectionBuilder(ipAddress);
        return builder.isHueBridgeEndpoint().get();
    }

}
