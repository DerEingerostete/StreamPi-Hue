module de.dereignerostete.hue.master {
    requires com.stream_pi.action_api;
    requires com.stream_pi.util;

    requires transitive org.jetbrains.annotations;
    requires transitive lombok;
    requires transitive yetanotherhueapi;

    exports de.dereingerostete.hue.api;
    provides com.stream_pi.action_api.externalplugin.ExternalPlugin with de.dereingerostete.hue.HueMaster;
}