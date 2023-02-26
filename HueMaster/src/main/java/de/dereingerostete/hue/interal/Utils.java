package de.dereingerostete.hue.interal;

import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils {

    public static void showAlert(@NotNull String message, @NotNull StreamPiAlertType type) {
        StreamPiAlert alert = new StreamPiAlert(message, type);
        alert.show();
    }

    @Nullable
    public static String getReason(@NotNull Throwable throwable) {
        String reason;
        Throwable currentThrowable = throwable;
        do {
            reason = currentThrowable.getMessage();
            currentThrowable = currentThrowable.getCause();
        } while (currentThrowable != null);
        return reason;
    }

}
