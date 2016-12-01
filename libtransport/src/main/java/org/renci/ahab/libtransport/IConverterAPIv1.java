package org.renci.ahab.libtransport;

public interface IConverterAPIv1 {

	public enum ConverterCommand {
		RSPEC2_TO_NDL("requestFromRSpec2"),
		RSPEC3_TO_NDL("requestFromRSpec3"),
		MANIFEST_TO_RSPEC("manifestToRSpec3"),
		AD_TO_RSPEC("adToRSpec3"),
		ADS_TO_RSPEC("adsToRSpec3");
		
		private final String cmd;
		
		ConverterCommand(String s) {
			cmd = s;
		}
		
		public String getCmd() {
			return cmd;
		}
	}

	
	public String callConverter(ConverterCommand cmd, Object[] params) throws Exception;

}