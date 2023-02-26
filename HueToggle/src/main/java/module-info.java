import de.dereingerostete.hue.toggle.HueToggleAction;

module de.dereingerostete.hue.toggle {
    requires com.stream_pi.action_api;
    requires de.dereignerostete.hue.master;

    provides com.stream_pi.action_api.externalplugin.ExternalPlugin with HueToggleAction;
}