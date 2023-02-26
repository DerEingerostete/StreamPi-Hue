package de.dereingerostete.hue.api;

import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import de.dereingerostete.hue.interal.HueStartupUtils;
import de.dereingerostete.hue.interal.Utils;
import io.github.zeroone3010.yahueapi.Hue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
public class HueConnector implements Runnable {
    private final @Nullable Consumer<Hue> onConnected;
    private final @Nullable Runnable onConnectFail;
    private final @NotNull Logger logger;
    private @Nullable String bridgeIp, apiKey, appName;
    private boolean connecting;

    public void runAsync() {
        Thread thread = new Thread(this);
        thread.setName("HueConnector");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            if (bridgeIp == null) {
                bridgeIp = HueStartupUtils.discoverBridgeIp();
                if (bridgeIp == null) {
                    Utils.showAlert(
                            "No Hue Bridge was discovered!\nPlease manually enter one.",
                            StreamPiAlertType.INFORMATION
                    );
                    connecting = false;
                    if (onConnectFail != null) onConnectFail.run();
                    return;
                }
            }
        } catch (Exception exception) {
            connecting = false;
            logWarning("Failed to discover Hue Bridge IP",
                    "Please manually enter one.", false, exception);
            if (onConnectFail != null) onConnectFail.run();
            return;
        }

        try {
            if (apiKey == null) {
                if (appName == null) throw new IllegalStateException("No app name is defined");
                apiKey = HueStartupUtils.authenticate(bridgeIp, appName);
                if (apiKey == null) {
                    getLogger().warning("Failed to authorize with Hue Bridge: Returned null as ApiKey");
                    Utils.showAlert("Failed to authorize with Hue Bridge", StreamPiAlertType.WARNING);
                    connecting = false;
                    if (onConnectFail != null) onConnectFail.run();
                    return;
                }
            }
        } catch (Exception exception) {
            connecting = false;
            logWarning("Failed to authorize with Hue Bridge", null, true, exception);
            if (onConnectFail != null) onConnectFail.run();
            return;
        }

        Hue hue = new Hue(bridgeIp, apiKey);
        if (onConnected != null) onConnected.accept(hue);
        connecting = false;
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

}
