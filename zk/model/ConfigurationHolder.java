package com.han.startup.zk.model;

import com.google.common.collect.Lists;
import com.ubisoft.hfx.zk.discovery.LocalIpFilter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by shan2 on 7/20/2017.
 */
@Data
@Getter
@Setter
@Component
public class ConfigurationHolder {
	public static final AtomicReference<LocalIpFilter> localIpFilter = new AtomicReference<LocalIpFilter>
			(
					(nif, adr) -> (adr != null) && !adr.isLoopbackAddress() && (nif.isPointToPoint() || !adr.isLinkLocalAddress())
			);

	String hostname;
	AtomicBoolean maintenance = new AtomicBoolean(false);
	AtomicBoolean acquireAvailable = new AtomicBoolean(true);
	AtomicBoolean deleteAvailable = new AtomicBoolean(true);
	AtomicInteger messageQueueLimit = new AtomicInteger(256);

	public static Collection<InetAddress> getAllLocalIPs() throws SocketException {
		List<InetAddress> listAdr = Lists.newArrayList();
		Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
		if (nifs == null)
			return listAdr;

		while (nifs.hasMoreElements()) {
			NetworkInterface nif = nifs.nextElement();

			Enumeration<InetAddress> adrs = nif.getInetAddresses();
			while (adrs.hasMoreElements()) {
				InetAddress adr = adrs.nextElement();
				if (localIpFilter.get().use(nif, adr)) {
					listAdr.add(adr);
				}
			}
		}
		return listAdr;
	}
}
