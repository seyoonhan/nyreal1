package com.han.startup.zk.discovery;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

/**
 * Created by shan2 on 7/18/2017.
 */
public interface LocalIpFilter {
	boolean use(NetworkInterface networkInterface, InetAddress address) throws SocketException;
}
