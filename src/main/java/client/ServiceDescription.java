/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.net.Inet4Address;
import java.util.HashMap;

public final class ServiceDescription {

    private final String address;
    private final int port;
    private final HashMap<String, String> properties;

    public ServiceDescription(String address, int port) {
        this.address = address;
        this.port = port;
        properties = new HashMap<String, String>();
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

}
