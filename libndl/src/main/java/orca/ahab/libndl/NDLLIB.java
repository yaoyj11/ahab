/*
 * Copyright (c) 2011 RENCI/UNC Chapel Hill 
 *
 * @author Ilia Baldine
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and/or hardware specification (the "Work") to deal in the Work without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Work, and to permit persons to whom the Work is furnished to do so, subject to 
 * the following conditions:  
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Work.  
 *
 * THE WORK IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE WORK OR THE USE OR OTHER DEALINGS 
 * IN THE WORK.
 */

package orca.ahab.libndl;

import java.io.File;
import java.util.Properties;

import orca.ndl.NdlCommons;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class NDLLIB {

	//private static final String FLUKES_HELP_WIKI = "https://geni-orca.renci.org/trac/wiki/flukes";
	public static final String buildVersion = NDLLIB.class.getPackage()
			.getImplementationVersion();
//	public static final String aboutText = "ORCA FLUKES "
//			+ (buildVersion == null ? "Eclipse build" : buildVersion)
//			+ "\nNDL-OWL network editor for ORCA (Open Resource Control Architecture)"
//			+ "\nDeveloped using Jena Semantic Web Framework, JUNG Java Universal Network/Graph Framework and Kiwi Swing toolkit. \nSplitButton adopted from implementation by Edward Scholl (edscholl@atwistedweb.com)"
//			+ "\n\nCopyright 2011-2013 RENCI/UNC Chapel Hill";

	protected static Logger logger;
	//private String[] controllerUrls;
	//private String selectedControllerUrl;

	private Properties prefProperties;
	//private static NDLLIB instance = null;



//	public static final Set<String> NDL_EXTENSIONS = new HashSet<String>();
//	static {
//		NDL_EXTENSIONS.add("ndl");
//		NDL_EXTENSIONS.add("rdf");
//		NDL_EXTENSIONS.add("n3");
//	}



	public Logger getLogger() {
		return logger;
	}

	public static Logger logger() {
		return logger;
	}



	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

	}

	/**
	 * Launch the non-GUI application.
	 */
	public static void non_gui_main() {
		// Jena stuff needs to be set up early
		NdlCommons.setGlobalJenaRedirections();
		
		//NDLLIB gui = NDLLIB.getInstance();

	}

	public static void non_gui_save(File f) {

//		Request.getInstance().setSaveFile(f);
//
//		if (Request.getInstance().getSaveFile() != null) {
//			RequestSaver.getInstance().saveGraph(
//					Request.getInstance().getSaveFile(),
//					Request.getInstance().g,
//					Request.getInstance().nsGuid);
//		} 
	}

	/**
	 * Create the application.
	 */
	private NDLLIB() {
		logger = Logger.getLogger(NDLLIB.class.getCanonicalName());
		logger.setLevel(Level.DEBUG);
		// UIChangeManager.setDefaultTexture(null);
	}

//	public static NDLLIB getInstance() {
//		if (instance == null)
//			instance = new NDLLIB();
//		return instance;
//	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {
	}

	//
	// /**
	// * Return user preferences specified in .flukes.properties or default
	// value otherwise.
	// * Never null;
	// * @return
	// */
	public String getPreference(PrefsEnum e) {
		if (prefProperties == null)
			return e.getDefaultValue();
		if (prefProperties.containsKey(e.getPropName()))
			return prefProperties.getProperty(e.getPropName());
		else
			return e.getDefaultValue();
	}

	// /**
	// * Allowed properties
	// * @author ibaldin
	// *
	// */
	public enum PrefsEnum {
		XTERM_PATH("xterm.path", "/usr/X11/bin/xterm",
				"Path to XTerm executable on your system"), PUTTY_PATH(
				"putty.path", "C:/Program Files (x86)/PuTTY/putty.exe",
				"Path to PuTTY executable on your system (Windows-specific)"), SCRIPT_COMMENT_SEPARATOR(
				"script.comment.separator", "#",
				"Default comment character used in post-boot scripts"), SSH_KEY(
				"ssh.key",
				"~/.ssh/id_dsa",
				"SSH Private Key to use to access VM instances(public will be installed into instances). You can use ~ to denote user home directory."), SSH_PUBKEY(
				"ssh.pubkey", "~/.ssh/id_dsa.pub",
				"SSH Public key to install into VM instances"), SSH_OTHER_LOGIN(
				"ssh.other.login", "root",
				"Secondary login (works with ssh.other.pubkey)"), SSH_OTHER_PUBKEY(
				"ssh.other.pubkey",
				"~/.ssh/id_rsa.pub",
				"Secondary public SSH keys (perhaps belonging to other users) that should be installed in the slice."), SSH_OTHER_SUDO(
				"ssh.other.sudo", "yes",
				"Should the secondary account have sudo privileges"), SSH_OPTIONS(
				"ssh.options",
				"-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no",
				"Options for invoking SSH (the default set turns off checking .ssh/known_hosts)"), ORCA_REGISTRY(
				"orca.registry.url", "http://geni.renci.org:12080/registry/",
				"URL of the ORCA actor registry to query"), ORCA_REGISTRY_CERT_FINGERPRINT(
				"orca.registry.certfingerprint",
				"78:B6:1A:F0:6C:F8:C7:0F:C0:05:10:13:06:79:E0:AC",
				"MD5 fingerprint of the certificate used by the registry"), USER_KEYSTORE(
				"user.keystore",
				"~/.ssl/user.jks",
				"Keystore containing your private key and certificate issued by GPO, Emulab or BEN"), USER_CERTFILE(
				"user.certfile", "~/.ssl/user.crt",
				"CRT or PEM file containing your certificate issued by GPO, Emulab or BEN"), USER_CERTKEYFILE(
				"user.certkeyfile", "~/.ssl/user.key",
				"KEY or PEM file containing your private key issued by GPO, Emulab or BEN"), ORCA_XMLRPC_CONTROLLER(
				"orca.xmlrpc.url",
				"https://some.hostname.org:11443/orca/xmlrpc",
				"Comma-separated list of URLs of the ORCA XMLRPC controllers where you can submit slice requests"), ENABLE_MODIFY(
				"enable.modify", "false",
				"Enable experimental support for slice modify operations (at your own risk!)"), ENABLE_IRODS(
				"enable.irods", "false",
				"Enable experimental support for iRods (at your own risk!)"), AUTOIP_MASK(
				"autoip.mask",
				"25",
				"Length of netmask (in bits) to use when assigning IP addresses to groups and broadcast links (simple point-to-point links always use 30 bit masks)"), IRODS_FORMAT(
				"irods.format",
				"ndl",
				"Specify the format in which requests and manifests should be saved to iRods ('ndl' or 'rspec')"), IRODS_MANIFEST_TEMPLATE(
				"irods.manifest.template",
				"${slice.name}/manifest-${date}.${irods.format}",
				"Specify the format for manifest file names (substitutions are performed, multiple directory levels are respected)"), IRODS_REQUEST_TEMPLATE(
				"irods.request.template",
				"${slice.name}/request-${date}.${irods.format}",
				"Specify the format for request file names (substitutions are performed, multiple directory levels are respected))"), IRODS_ICOMMANDS_PATH(
				"irods.icommands.path", "/usr/bin", "Path to icommands"), NDL_CONVERTER_LIST(
				"ndl.converter.list",
				"http://geni.renci.org:12080/ndl-conversion/, http://bbn-hn.exogeni.net:15080/ndl-conversion/",
				"Comma-separated list of available NDL converters"), CUSTOM_INSTANCE_LIST(
				"custom.instance.list", "",
				"Comma-separated list of custom instance sizes. For debugging only!"), IMAGE_NAME(
				"image.name",
				"Debian-6-Standard-Multi-Size-Image-v.1.0.6",
				"Name of a known image, you can add more images by adding image1.name, image2.name etc. To see defined images click on 'Client Images' button."), IMAGE_URL(
				"image.url",
				"http://geni-images.renci.org/images/standard/debian/deb6-neuca-v1.0.7.xml",
				"URL of a known image description file, you can add more images by adding image1.url, image2.url etc."), IMAGE_HASH(
				"image.hash",
				"ba15fa6f56cc00d354e505259b9cb3804e1bcb73",
				"SHA-1 hash of the image description file, you can add more images by adding image1.hash, image2.hash etc.");

		private final String propName;
		private final String defaultValue;
		private final String comment;

		PrefsEnum(String s, String d, String c) {
			propName = s;
			defaultValue = d;
			comment = c;
		}

		public String getPropName() {
			return propName;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public String getComment() {
			return comment;
		}
	}



}
