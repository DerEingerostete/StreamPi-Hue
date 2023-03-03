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

import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils {

    public static void showAlert(@NotNull String title, @NotNull String message, @NotNull StreamPiAlertType type) {
        StreamPiAlert alert = new StreamPiAlert(title, message, type);
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
