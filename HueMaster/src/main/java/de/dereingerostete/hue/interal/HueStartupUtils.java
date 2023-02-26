package de.dereingerostete.hue.interal;

import com.stream_pi.util.alert.StreamPiAlertType;
import io.github.zeroone3010.yahueapi.Hue;
import io.github.zeroone3010.yahueapi.HueBridge;
import io.github.zeroone3010.yahueapi.discovery.HueBridgeDiscoveryService;
import io.github.zeroone3010.yahueapi.discovery.HueBridgeDiscoveryService.DiscoveryMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

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
        }, DiscoveryMethod.MDNS, DiscoveryMethod.NUPNP);
        futureReference.set(future);
        future.get(30, TimeUnit.SECONDS);

        HueBridge bridge = bridgeReference.get();
        return bridge == null ? null : bridge.getIp();
    }

    @Nullable
    public static String authenticate(@NotNull String bridgeIp, @NotNull String appName)
            throws InterruptedException, TimeoutException, ExecutionException {
        Hue.HueBridgeConnectionBuilder builder = Hue.hueBridgeConnectionBuilder(bridgeIp);
        if (builder.isHueBridgeEndpoint().get()) {
            Utils.showAlert(
                    "The specified IP address does not belong to a Hue bridge!",
                    StreamPiAlertType.WARNING
            );
            return null;
        }

        Utils.showAlert(
                "Press the push-link button on the Hue bridge you want to connect to.",
                StreamPiAlertType.INFORMATION
        );

        try {
            return builder.initializeApiConnection(appName).get(3, TimeUnit.MINUTES);
        } catch (ExecutionException exception) {
            String reason = String.valueOf(Utils.getReason(exception)).toLowerCase();
            if (reason.contains("link button not pressed")) {
                Utils.showAlert("Failed to connect to Hue.\nThe link button was not pressed.", StreamPiAlertType.WARNING);
                return null;
            } else throw new ExecutionException(exception);
        }
    }

}
