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

import org.jetbrains.annotations.NotNull;

public class EnumUtils {

    @NotNull
    public static String formatName(@NotNull Enum<?> anEnum) {
        String name = anEnum.name();
        String[] parts = name.split("_");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.length() == 0) {
                builder.append(' ');
                break;
            }

            String lowercase = part.toLowerCase();
            builder.append(part.charAt(0));
            builder.append(lowercase.substring(1));
            if (i + 1 < parts.length) builder.append(' ');
        }
        return builder.toString();
    }

}
