package utils;

public class GetIP {

    public static String getMyIp() {
        String forced = System.getenv("MQTT_CLIENT_IP");
        if (forced != null && !forced.isBlank()) return forced;

        try {
            var interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                var iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                var addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    var addr = addresses.nextElement();
                    if (addr.isLoopbackAddress()) continue;

                    String ip = addr.getHostAddress();
                    if (ip.contains(".")) return ip;
                }
            }
        } catch (Exception e) { }

        return "UNKNOWN";
    }
}
