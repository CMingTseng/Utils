package idv.neo.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
 * Created by Neo on 2017/4/18.
 */

public class Network {
    public static String HOST_NAME = null;
    public static String HOST_IPADDRESS = null;

    /**
     * Convert byte array to hex string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for (int idx = 0; idx < bytes.length; idx++) {
            int intVal = bytes[idx] & 0xff;
            if (intVal < 0x10) sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }

    /**
     * Get utf8 byte array.
     *
     * @return array of NULL if error was found
     */
    public static byte[] getUTF8Bytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Load UTF8withBOM or any ansi text file.
     */
    public static String loadFileAsString(String filename) throws java.io.IOException {
        final int BUFLEN = 1024;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
            byte[] bytes = new byte[BUFLEN];
            boolean isUTF8 = false;
            int read, count = 0;
            while ((read = is.read(bytes)) != -1) {
                if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    isUTF8 = true;
                    baos.write(bytes, 3, read - 3); // drop UTF8 bom marker
                } else {
                    baos.write(bytes, 0, read);
                }
                count += read;
            }
            return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * // test functions
     * getMACAddress("wlan0");
     * getMACAddress("eth0");
     * getIPAddress(true); // IPv4
     * getIPAddress(false); // IPv6
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "00:00:00:00:00:00";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "0.0.0.0";
    }

    public static String getThisHostName() {
        if (HOST_NAME == null) obtainHostInfo();
        return HOST_NAME;
    }

    public static String getThisIpAddress() {
        if (HOST_IPADDRESS == null) obtainHostInfo();
        return HOST_IPADDRESS;
    }

    protected static void obtainHostInfo() {
        HOST_IPADDRESS = "127.0.0.1";
        HOST_NAME = "localhost";
        try {
            InetAddress primera = InetAddress.getLocalHost();
            String hostname = InetAddress.getLocalHost().getHostName();

            if (!primera.isLoopbackAddress() &&
                    !hostname.equalsIgnoreCase("localhost") &&
                    primera.getHostAddress().indexOf(':') == -1) {
                // Got it without delay!!
                HOST_IPADDRESS = primera.getHostAddress();
                HOST_NAME = hostname;
                //System.out.println ("First try! " + HOST_NAME + " IP " + HOST_IPADDRESS);
                return;
            }
            for (Enumeration<NetworkInterface> netArr = NetworkInterface.getNetworkInterfaces(); netArr.hasMoreElements(); ) {
                NetworkInterface netInte = netArr.nextElement();
                for (Enumeration<InetAddress> addArr = netInte.getInetAddresses(); addArr.hasMoreElements(); ) {
                    InetAddress laAdd = addArr.nextElement();
                    String ipstring = laAdd.getHostAddress();
                    String hostName = laAdd.getHostName();

                    if (laAdd.isLoopbackAddress()) continue;
                    if (hostName.equalsIgnoreCase("localhost")) continue;
                    if (ipstring.indexOf(':') >= 0) continue;

                    HOST_IPADDRESS = ipstring;
                    HOST_NAME = hostName;
                    break;
                }
            }
        } catch (Exception ex) {
        }
    }

    public static String getIpAddress() {
        //this function does not return me the correct ip address (possibly its returning the router ip and not the proxy ip given for my device)
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    public static String getLocalIpAddress() {
        //this function does not return me the correct ip address (possibly its returning the router ip and not the proxy ip given for my device)
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        Log.i(TAG, "***** IP=" + ip);
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return "0.0.0.0";
    }

    public static String getLocalIpv4Address() {
        //this function does not return me the correct ip address (possibly its returning the router ip and not the proxy ip given for my device)
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "0.0.0.0";
    }

    public static String getNetworkType(ConnectivityManager cm) {
        final NetworkInfo net = cm.getActiveNetworkInfo();
        if ((null == net) || !net.isConnectedOrConnecting()) {
            return null;
        }
        return net.getTypeName();
    }

    public static boolean isWired(ConnectivityManager cm) {
        final NetworkInfo net = cm.getActiveNetworkInfo();
        if ((null == net) || net.getType() != ConnectivityManager.TYPE_ETHERNET) {
            return false;
        }
        return true;
    }

    public static String tranforIpAddressToString(int ipAddress) {
        Log.d(TAG, "use hide method : " + Formatter.formatIpAddress(ipAddress));
        Log.d(TAG, "method : " + String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }
}
