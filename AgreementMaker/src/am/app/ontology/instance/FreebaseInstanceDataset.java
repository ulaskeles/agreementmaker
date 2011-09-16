package am.app.ontology.instance;

import java.util.List;

import am.AMException;
import am.app.ontology.instance.endpoint.FreebaseEndpoint;

/**
 * An instance dataset for a Freebase endpoint.
 */
public class FreebaseInstanceDataset implements InstanceDataset {

	private FreebaseEndpoint instanceSource;
	
	public FreebaseInstanceDataset(FreebaseEndpoint endpoint) {
		instanceSource = endpoint;
	}
	
	@Override
	public boolean isIterable() { return false; }

	@Override
	public List<Instance> getInstances(String type, int limit)
			throws AMException {
		try {
			return instanceSource.listInstances( type, limit );
		} catch( Exception e ) {
			e.printStackTrace();
			throw new AMException(e.getMessage());
		}
	}

	@Override
	public List<Instance> getCandidateInstances(String searchTerm, String type)
			throws AMException {
		try {
			return instanceSource.freeTextQuery(searchTerm, type);
		} catch( Exception e ) {
			e.printStackTrace();
			throw new AMException(e.getMessage());
		}
	}

	@Override
	public List<Instance> getInstances() throws AMException {
		throw new AMException("This functionality is not available for an endpoint.");
	}

	@Override
	public Instance getInstance(String uri) throws AMException {
		throw new AMException("This feature is not yet implemented.");
	}

}