package org.renci.ahab.libndl.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.net.InetAddresses;

/**
 * Class to manage ip address assignments
 * 
 * @author pruth
 * 
 */
public class IP4Assign {
	
	private TreeMap<Integer, Integer> managedSubnetTreeMap; // for searching if
															// subnet overlapps
															// with any managed
															// network
	private TreeMap<Integer, Integer> allocatedSubnetTreeMap; // for searching
																// for conficts

	private HashMap<Object, IP4Subnet> allocatedSubnetMap; // for getting subnet
															// of specific
															// link/crossconnect

	private ArrayList<ArrayList<IP4Subnet>> availableSubnets;

	private static Logger logger;

	public IP4Assign() {
		if (logger == null)	{
			logger = Logger.getLogger(IP4Assign.class.getCanonicalName());
			logger.setLevel(Level.DEBUG);
		}
		
		allocatedSubnetTreeMap = new TreeMap<Integer, Integer>();
		allocatedSubnetMap = new HashMap<Object, IP4Subnet>();

		// create a list of IP4Subnet for each mask length
		availableSubnets = new ArrayList<ArrayList<IP4Subnet>>(32);
		for (int i = 0; i < 32; i++) {
			availableSubnets.add(i, new ArrayList<IP4Subnet>());
		}

		// Hardcoded pool of available subnets
		try {
			// 172.16.0.0/12
			availableSubnets.get(20).add(
					new IP4Subnet((Inet4Address) InetAddress
							.getByName("172.16.0.0"), 12));
		} catch (UnknownHostException e) {
			System.out.println("Exception: can not add subnet 172.16.0.0/12 to free list");
		}
		// 192.168.0.0/16
		// availableSubnets.get(16).add(new IP4Subnet("192.168.0.0",16));

		// 10.0.0.0/8
		// availableSubnets.get(16).add(new IP4Subnet("10.0.0.0",8));

	}

	private IP4Subnet getSubnetContainingSubnet(Inet4Address ip, int mask_length) {
		// Crappy data structure for this, but will work for now.
		for (int i = 0; i < 32; i++) {
			for (IP4Subnet s : availableSubnets.get(i)) {
				if (s.isInSubnet(ip)
						&& s.isInSubnet((Inet4Address) InetAddresses
								.fromInteger(InetAddresses.coerceToInteger(ip)
										+ s.getSizeFromMask(mask_length) - 1))) {
					availableSubnets.get(i).remove(s);
					return s;
				}

			}
		}
		return null;
	}

	private ArrayList<IP4Subnet> getAllOverlappingSubnets(Inet4Address ip,
			int mask_length) {
		// Crappy data structure for this, but will work for now.
		ArrayList<IP4Subnet> subnets = new ArrayList<IP4Subnet>();

		for (int i = 0; i < 32; i++) {
			ArrayList<IP4Subnet> remove_list = new ArrayList<IP4Subnet>();
			Iterator<IP4Subnet> iter = availableSubnets.get(i).iterator();
			while (iter.hasNext()) {
				IP4Subnet s = iter.next();
				if (s.doesOverlap(ip, mask_length)) {
					// availableSubnets.get(i).remove(s);
					remove_list.add(s);
					subnets.add(s);
				}

			}
			// remove from main list
			for (IP4Subnet s : remove_list) {
				availableSubnets.get(i).remove(s);
			}

		}

		return subnets;
	}

	public IP4Subnet getSubnet(Inet4Address ip, int mask_length) {
		logger.debug("getSubnet 1: " + ip + ", mask_lengh: " + mask_length);
		if (!isAvailable(ip, mask_length)) {
			// Should throw exception
			return null;
		}

		ArrayList<IP4Subnet> overlapping_subnets = getAllOverlappingSubnets(ip,
				mask_length);

		for (IP4Subnet s : overlapping_subnets) {
			logger.debug("getSubnet 50: removing " + s);
			availableSubnets.get(s.getMaskLength()).remove(s);

			if (!s.isInSubnet(ip))
				continue;

			// if partial overlap then find free portion and replace.
			while (s.getMaskLength() < mask_length) {
				logger.debug("getSubnet 200: " + s.getMaskLength());
				IP4Subnet s_split = s.split();
				if (s.isInSubnet(ip)) {
					logger.debug("getSubnet 210: ");
					availableSubnets.get(s_split.getMaskLength()).add(s_split);
				} else {
					logger.debug("getSubnet 220: ");
					availableSubnets.get(s.getMaskLength()).add(s);
					s = s_split;
				}
			}

		}

		return new IP4Subnet(ip, mask_length);

	}

	public IP4Subnet getAvailableSubnet(int count) {
		int mask_length = IP4Subnet.getMaskFromSize(count);

		// find smallest subnet bigger than count
		int i = mask_length;
		while (i > 0 && availableSubnets.get(i).isEmpty())
			i--;

		if (i <= 0) {
			logger.info("unable to getAvailableSubnet: i == " + i);
			return null;
		}

		// get subnet
		IP4Subnet s = availableSubnets.get(i).remove(0);

		// split subnet until is correct size
		while (i != mask_length) {
			availableSubnets.get(++i).add(s.split());
		}

		return s;

	}


	public boolean isAvailable(Inet4Address ip, int mask_length) {
		int size = IP4Subnet.getSizeFromMask(mask_length);
		int ip_int = InetAddresses.coerceToInteger(ip);

		Entry<Integer, Integer> prev = allocatedSubnetTreeMap
				.floorEntry(ip_int);
		Entry<Integer, Integer> next = allocatedSubnetTreeMap
				.higherEntry(ip_int);

		logger.info("prev = " + prev);
		logger.info("next = " + next);
		logger.info("ip_int = " + ip_int + ", size = " + size);

		if (prev != null && prev.getKey() + prev.getValue() > ip_int) {
			logger.debug("prev != null && prev.getKey()+prev.getValue() >= ip_int");
			return false;
		}

		if (next != null && next.getKey() < ip_int + size) {
			logger.debug("next !=  null &&  next.getKey() <= ip_int + size");
			return false;
		}

		return true;
	}

	// is a managed subnet part of the proposed subnet?
	public boolean isManagedSubnet(Inet4Address ip, int mask_length) {
		int ip_int = InetAddresses.coerceToInteger(ip);

		Entry<Integer, Integer> prev = managedSubnetTreeMap.floorEntry(ip_int);
		Entry<Integer, Integer> next = managedSubnetTreeMap.higherEntry(ip_int);

		if (prev.getKey() + prev.getValue() < ip_int
				&& next.getKey() > ip_int
						+ IP4Subnet.getSizeFromMask(mask_length))
			return false;

		return true;
	}

	public String toString() {
		String rtnStr = "";

		rtnStr += "allocatedSubnetTreeMap: \n";
		for (Entry<Integer, Integer> e : allocatedSubnetTreeMap.entrySet()) {
			rtnStr += e + "\n";
		}

		rtnStr += "allocatedSubnetMap: \n";
		for (Entry<java.lang.Object, IP4Subnet> s : allocatedSubnetMap
				.entrySet()) {
			rtnStr += s + "\n";
		}

		rtnStr += "availableSubnets: \n";
		for (int i = 0; i < 32; i++) {
			rtnStr += "size " + i + ": \n";
			for (IP4Subnet s : availableSubnets.get(i)) {

				rtnStr += "\t" + s + "\n";
			}
		}
		return rtnStr;
	}





}