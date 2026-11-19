package com.quizapp.client.util;

import com.quizapp.client.ClientConfig;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Collections;

/** Builds the live page link that other devices (phones on the same Wi-Fi) can open. */
public final class NetworkUtil {

    private NetworkUtil() {
    }

    /**
     * Live page address for a quiz. If the server is "localhost" (running on this laptop),
     * the laptop's Wi-Fi/LAN address is used instead, because a phone cannot open "localhost".
     */
    public static String livePageUrl(int quizId) {
        String base = ClientConfig.serverUrl();
        URI uri = URI.create(base);
        String host = uri.getHost();
        if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)) {
            String lan = lanAddress();
            if (lan != null) {
                base = uri.getScheme() + "://" + lan + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
            }
        }
        return base + "/live.html?quiz=" + quizId;
    }

    /** First private IPv4 address of an active network adapter (e.g. 192.168.1.5), or null. */
    static String lanAddress() {
        try {
            for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nic.isUp() || nic.isLoopback() || nic.isVirtual()) {
                    continue;
                }
                for (InetAddress addr : Collections.list(nic.getInetAddresses())) {
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
            // fall through
        }
        return null;
    }
}
