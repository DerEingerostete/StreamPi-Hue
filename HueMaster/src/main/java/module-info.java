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

module de.dereignerostete.hue.master {
    requires com.stream_pi.action_api;
    requires com.stream_pi.util;

    requires static lombok;
    requires transitive org.jetbrains.annotations;
    requires transitive yetanotherhueapi;

    exports de.dereingerostete.hue.api;
    provides com.stream_pi.action_api.externalplugin.ExternalPlugin with de.dereingerostete.hue.HueMaster;
}