package orca.ahab.libndl.ndl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import orca.ndl.INdlAbstractDelegationModelListener;
import orca.ndl.elements.LabelSet;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Basic parser of Ads provided by an SM
 * @author ibaldin
 *
 */
public class AdLoader implements INdlAbstractDelegationModelListener {
	List<String> domains = new ArrayList<String>();
	
	//new methods for recovery branch
	public void ndlNetworkDomain(Resource dom, OntModel m, List<Resource> netServices, List<Resource> interfaces, List<LabelSet> labelSets, Map<Resource, List<LabelSet>> netLabelSets){
		// TODO
	}
	
	public void ndlNetworkDomain(Resource dom, OntModel m,
			List<Resource> netServices, List<LabelSet> labelSets,
			List<Resource> interfaces) {
		domains.add(dom.toString());
	}

	
	public void ndlInterface(Resource l, OntModel om, Resource conn,
			Resource node, String ip, String mask) {
		// TODO Auto-generated method stub

	}

	
	public void ndlNetworkConnection(Resource l, OntModel om, long bandwidth,
			long latency, List<Resource> interfaces) {
		// TODO Auto-generated method stub

	}

	
	public void ndlNode(Resource ce, OntModel om, Resource ceClass,
			List<Resource> interfaces) {
		// TODO Auto-generated method stub

	}

	
	public void ndlParseComplete() {
		// TODO Auto-generated method stub

	}
	
	public List<String> getDomains() {
		return domains;
	}

}
