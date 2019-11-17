package fr.insa.laas.HTTP;

import java.io.IOException;

/**
 * Interface for the client part
 *
 */
public interface ClientInterface {
	public Response retrieve(String url, String originator) throws IOException;

	public Response create(String url, String representation, String originator, String type)
			throws IOException;

	public Response update(String url, String representation, String originator)
			throws IOException;

	public Response delete(String url, String originator) throws IOException;

	public Response request(String url, String originator, String content)
			throws IOException;

}
