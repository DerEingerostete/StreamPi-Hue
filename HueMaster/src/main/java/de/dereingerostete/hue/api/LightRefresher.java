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

import io.github.zeroone3010.yahueapi.Hue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LightRefresher {
    private final @NotNull Set<Refreshable> actions;
    private @Nullable Timer timer;

    public LightRefresher() {
        actions = new HashSet<>();
    }

    public void start(int refreshTime, @NotNull TimeUnit unit) {
        long millis = unit.toMillis(refreshTime);
        timer = new Timer("LightRefresherTimer", true);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Hue hue = HueAPI.getInstance().getHue();
                if (hue != null) hue.refresh();
                refresh(null);
            }

        }, millis, millis);
    }

    public void stop() {
        if (timer != null) timer.cancel();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean addAction(@NotNull Refreshable refreshable) {
        return actions.add(refreshable);
    }

    public void removeAction(@NotNull Refreshable refreshable) {
        actions.remove(refreshable);
    }

    public void refresh(@Nullable String exemptId) {
        if (exemptId != null) {
            actions.stream()
                    .filter(refreshable -> !refreshable.getId().equals(exemptId))
                    .forEach(Refreshable::refresh);
        } else {
            actions.forEach(Refreshable::refresh);
        }
    }

    public interface Refreshable {

        @NotNull
        String getId();

        void refresh();

    }

}
