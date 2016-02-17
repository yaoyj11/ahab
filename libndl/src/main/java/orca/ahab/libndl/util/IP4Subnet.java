package orca.ahab.libndl.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.BitSet;

import com.google.common.net.InetAddresses;
/**
 * Class to manage an ip subnet
 * @author pruth 
 *
 */
public class IP4Subnet {
	Inet4Address start_ip;
	int mask_length;  //cidr mask length
	int mask;


	
	BitSet allocatedIPs;

	public IP4Subnet() {}

	public IP4Subnet(Inet4Address anIp, int aMaskLength) {
		mask_length = aMaskLength;
		allocatedIPs = new BitSet();
		mask = 0xFFFFFFFF << (32 - mask_length);
		start_ip = InetAddresses.fromInteger(InetAddresses.coerceToInteger(anIp) & mask);
	}

	public IP4Subnet(Inet4Address anIp, int aMaskLength, BitSet anAllocatedIPs) {
		mask_length = aMaskLength;
		allocatedIPs = (BitSet) anAllocatedIPs.clone();
		mask = 0xFFFFFFFF << (32 - mask_length);
		start_ip = InetAddresses.fromInteger(InetAddresses.coerceToInteger(anIp) & mask);
	}

	public static int getSizeFromMask(int mask_length){
		return 1 << (32 - mask_length);
	}

	public static int getMaskFromSize(int size){
		int size_curr = size;
		int cnt = 0;
		while (size_curr > 0){
			size_curr = size_curr >> 1;
		cnt++;
		}

		if ((1 << cnt-1) == size)
			cnt--;

		return  32 - cnt;
	}

	public int getMaskLength(){
		return mask_length;
	}
	
	public Inet4Address getStartIP(){
		return start_ip;
	}

	public int getSize(){
		return getSizeFromMask(mask_length);
	}

	public boolean doesOverlap(IP4Subnet subnet){
		return doesOverlap(subnet.start_ip,subnet.mask_length);
	}

	public boolean doesOverlap(Inet4Address test_ip, int test_mask_length){
		int this_start_ip_int = InetAddresses.coerceToInteger(start_ip);
		int this_end_ip_int = this_start_ip_int + getSizeFromMask(mask_length);
		int test_start_ip_int = InetAddresses.coerceToInteger(test_ip);
		int test_end_ip_int = test_start_ip_int + getSizeFromMask(test_mask_length);
		int test_mask = 0xFFFFFFFF << (32 - test_mask_length);

		//test endpoints in this endpoints
		if ((test_start_ip_int & mask) == (this_start_ip_int & mask)  || (test_end_ip_int & mask) == (this_start_ip_int & mask) ) 
			return true;

		//this endpoints in test endpoints
		if ((this_start_ip_int & test_mask) == (test_start_ip_int & test_mask)  || (this_end_ip_int & test_mask) == (test_start_ip_int & test_mask) )
			return true;


		return false;
	}

	public boolean isInSubnet(Inet4Address ip){
		int start_ip_int = InetAddresses.coerceToInteger(start_ip);
		int ip_int = InetAddresses.coerceToInteger(ip);

		if ((ip_int & mask) == (start_ip_int & mask))
			return true;

		return false;
	}

	private int getOffset(Inet4Address ip){
		int start_ip_int = InetAddresses.coerceToInteger(start_ip);
		int ip_int = InetAddresses.coerceToInteger(ip);

		return ip_int - start_ip_int;
	}

	public Inet4Address getFreeIP(){
		int new_offset = allocatedIPs.nextClearBit(0);
		if (new_offset < getSizeFromMask(mask_length)){
			allocatedIPs.set(new_offset);
			return (Inet4Address)InetAddresses.fromInteger(InetAddresses.coerceToInteger(start_ip)+new_offset);
		}
		return null;
	}


	//Gets a contiguous block of IPs or return null
	public Inet4Address getFreeIPs(int count){
		//enforce minimum count so groups can grow
		//probably should be a user defined max count for a group
		//if (count < 256) count = 256;
		
		int start_offset = 0;
		int max_offset = getSizeFromMask(mask_length);
		int next_set = 0;

		while (start_offset < max_offset){
			start_offset = allocatedIPs.nextClearBit(next_set);

			next_set = allocatedIPs.nextSetBit(start_offset); 

			//if there are no higher used ips, are there enough ips remaining to fullfill request?
			if(next_set == -1 && start_offset + count < max_offset)
				break;

			//if ther is a higher used ip, is the block before it large enough?
			if (next_set > start_offset + count)
				break;    
		}

		if(start_offset >= max_offset){
			System.out.println("Could not allocate contiguous IPs.  No block of IPs is large enough.");
			return null;
		}

		allocatedIPs.set(start_offset,start_offset + count);
		return (Inet4Address)InetAddresses.fromInteger(InetAddresses.coerceToInteger(start_ip)+start_offset);
	}

	public void markAllIPsFree(){
		allocatedIPs.clear();
	}
	
	public void markAllIPsUsed(){
		if(allocatedIPs != null){
			System.out.println("markAllIPsUsed: marking");
			allocatedIPs.set(0, (1 << (32 - mask_length))-1);
		} else {
			System.out.println("markAllIPsUsed: skipping allocatedIPs.length(): " + allocatedIPs.length());
		}
	}
	
	public void markIPFree(Inet4Address ip){
		if (isInSubnet(ip))
			allocatedIPs.clear(getOffset(ip));
			
	}

	public void markIPUsed(String ipStr){
		try {
			markIPUsed((Inet4Address)InetAddress.getByName(ipStr));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void markIPUsed(Inet4Address ip){
		if (isInSubnet(ip))
			allocatedIPs.set(getOffset(ip));
	}

	public void markIPsFree(String ipStr, int count){
		try {
			markIPsFree((Inet4Address)InetAddress.getByName(ipStr),count);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void markIPsFree(Inet4Address ip, int count){
		if (isInSubnet(ip)){
			int offset = getOffset(ip);
			int size = getSizeFromMask(mask_length);
			if (offset + count > size)
				count = size - offset;
			allocatedIPs.clear(offset,offset+count);
		}
	}


	public void markIPsUsed(Inet4Address ip, int count){
		if (isInSubnet(ip)){
			int offset = getOffset(ip);
			int size = getSizeFromMask(mask_length);
			if (offset + count > size) 
				count = size - offset;
			allocatedIPs.set(offset,offset+count);
		}
	}

	public IP4Subnet split(){
		int old_size = getSizeFromMask(mask_length);
		int old_mask_length = mask_length;
		BitSet old_allocatedIPs = allocatedIPs;

		int size = old_size >> 1;  //must be power of 2
		mask_length = old_mask_length + 1;
		allocatedIPs = old_allocatedIPs.get(0,size-1);

		int new_subnet_size = size;
		int new_subnet_mask_length = mask_length;
		BitSet new_subnet_allocatedIPs = old_allocatedIPs.get(size,old_size-1);
		Inet4Address new_subnet_start_ip = (Inet4Address)InetAddresses.fromInteger(InetAddresses.coerceToInteger(start_ip)+size);

		return new IP4Subnet(new_subnet_start_ip, new_subnet_mask_length, new_subnet_allocatedIPs);
	}

	public String toString(){
		//return "in subnet toString";
		return "start_ip: " + start_ip.getHostAddress() + ", mask_length: " + mask_length + ", mask: 0x" + Integer.toHexString(mask) + ", size: " + getSizeFromMask(mask_length) + " " + allocatedIPs;
	}


	public static void main(String[] argv) throws UnknownHostException{
		//For testing ....

		IP4Subnet s = null;

		s = new IP4Subnet((Inet4Address)InetAddress.getByName("172.16.1.0"),25);

		System.out.println(s);

		s.markIPsUsed((Inet4Address)InetAddress.getByName("172.16.1.0"),256);

		Inet4Address ip1 = s.getFreeIP();
		System.out.println("new ip: " + ip1);

		s.markIPsUsed((Inet4Address)InetAddress.getByName("172.16.1.125"),4);

		System.out.println(s);

		System.out.println(InetAddresses.coerceToInteger((Inet4Address)InetAddress.getByName("172.16.1.42")));
		System.out.println(InetAddresses.coerceToInteger((Inet4Address)InetAddress.getByName("172.16.1.43")));

		IP4Subnet s2 = s.split();

		System.out.println("s : " + s);
		System.out.println("s2: " + s2);

		//Inet4Address ip100 = (Inet4Address)InetAddress.getByName("172.16.1.42");
		//Inet4Address ip101 = (Inet4Address)InetAddress.getByName("172.16.2.43");
		//System.out.println(InetAddresses.coerceToInteger(ip101) - InetAddresses.coerceToInteger(ip100));
	}

	
	/********************************** netmask convertion ****************************/
	
	// converting to netmask
	private static final String[] netmaskConverter = {
		"128.0.0.0", "192.0.0.0", "224.0.0.0", "240.0.0.0", "248.0.0.0", "252.0.0.0", "254.0.0.0", "255.0.0.0",
		"255.128.0.0", "255.192.0.0", "255.224.0.0", "255.240.0.0", "255.248.0.0", "255.252.0.0", "255.254.0.0", "255.255.0.0",
		"255.255.128.0", "255.255.192.0", "255.255.224.0", "255.255.240.0", "255.255.248.0", "255.255.252.0", "255.255.254.0", "255.255.255.0",
		"255.255.255.128", "255.255.255.192", "255.255.255.224", "255.255.255.240", "255.255.255.248", "255.255.255.252", "255.255.255.254", "255.255.255.255"
	};
	
	/**
	 * Convert netmask string to an integer (24-bit returned if no match)
	 * @param nm
	 * @return
	 */
	public static int netmaskStringToInt(String nm) {
		int i = 1;
		for(String s: netmaskConverter) {
			if (s.equals(nm))
				return i;
			i++;
		}
		return 24;
	}
	
	/**
	 * Convert netmask int to string (255.255.255.0 returned if nm > 32 or nm < 1)
	 * @param nm
	 * @return
	 */
	public static String netmaskIntToString(int nm) {
		if ((nm > 32) || (nm < 1)) 
			return "255.255.255.0";
		else
			return netmaskConverter[nm - 1];
	}

}

