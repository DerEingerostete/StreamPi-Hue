import hue.setcolor.SetColorAction;

module de.dereingerostete.hue.setcolor {
    requires com.stream_pi.action_api;
    requires de.dereignerostete.hue.master;

    provides com.stream_pi.action_api.externalplugin.ExternalPlugin with SetColorAction;
}