package fr.insa.laas.HTTP;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class Client implements ClientInterface {

	private static final String ORIGIN_HEADER = "X-M2M-Origin";
	private static final String CT_HEADER = "Content-Type";
	private static final String ACCEPT = "accept";
	private static final String XML = "application/xml";

	public Response retrieve(String url, String originator) throws IOException {
		Response response = new Response();
		// Instantiate a new Client
		//CloseableHttpClient client = HttpClients.createDefault();
		HttpClient client = new DefaultHttpClient();		// Instantiate the correct Http Method
		HttpGet get = new HttpGet(url);
		// add headers
		get.addHeader(ORIGIN_HEADER, originator);
		get.addHeader(ACCEPT, XML);
		try {
			// send request
			//CloseableHttpResponse reqResp = client.execute(get);
			HttpResponse reqResp = client.execute(get);
			//reqResp.
			response.setStatusCode(reqResp.getStatusLine().getStatusCode());
			response.setRepresentation(IOUtils.toString(reqResp.getEntity().getContent(), "UTF-8"));
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			//client.close();
		}
		// return response
		return response;
	}

	public Response create(String url, String representation,String originator, String type) throws IOException {
		
		Response response = new Response();
		// Instantiate a new Client
		//CloseableHttpClient client = HttpClients.createDefault();
		//HttpClient client = new DefaultHttpClient();
		HttpClient client = HttpClientBuilder.create().build();
				
		// Instantiate the correct Http Method
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(representation));
		// add headers
		post.addHeader(ORIGIN_HEADER, originator);
		post.addHeader(CT_HEADER, XML + ";ty=" + type);
		post.addHeader(ACCEPT, XML);

		try {
			// send request
			//CloseableHttpResponse reqResp = client.execute(post);
			HttpResponse reqResp = client.execute(post);
			response.setStatusCode(reqResp.getStatusLine().getStatusCode());
			response.setRepresentation(IOUtils.toString(reqResp.getEntity()
					.getContent(), "UTF-8"));
			//System.out.println(response.getRepresentation());
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			//client.close();
		}
		return response;
	}

	public Response update(String url, String representation, String originator) throws IOException {
		Response response = new Response();
		// Instantiate a new Client
		//CloseableHttpClient client = HttpClients.createDefault();
		HttpClient client = new DefaultHttpClient();
		// Instantiate the correct Http Method
		HttpPut put = new HttpPut(url);
		put.setEntity(new StringEntity(representation));
		// add headers
		put.addHeader(ORIGIN_HEADER, originator);
		put.addHeader(CT_HEADER, XML);
		put.addHeader(ACCEPT, XML);

		try {
			// send request
			//CloseableHttpResponse reqResp = client.execute(put);
			HttpResponse reqResp = client.execute(put);
			response.setStatusCode(reqResp.getStatusLine().getStatusCode());
			response.setRepresentation(IOUtils.toString(reqResp.getEntity()
					.getContent(), "UTF-8"));
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			//client.close();
		}
		return response;
	}

	public Response delete(String url, String originator) throws IOException {
		
		Response response = new Response();

		try {
			//Instantiate a new Client
			//CloseableHttpClient client = HttpClients.createDefault();
			//HttpClient client = new DefaultHttpClient();
			HttpClient client = HttpClientBuilder.create().build();
	
			// Instantiate the correct Http Method
			HttpDelete delete = new HttpDelete(url);
			// add headers
			delete.addHeader(ORIGIN_HEADER, originator);
			delete.addHeader(ACCEPT, XML);
		
			// send request
			//CloseableHttpResponse reqResp = client.execute(delete);
			HttpResponse reqResp = client.execute(delete);

			response.setStatusCode(reqResp.getStatusLine().getStatusCode());
			response.setRepresentation(IOUtils.toString(reqResp.getEntity()
					.getContent(), "UTF-8"));
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			//client.close();
		}
		// return response
		return response;
	}
	
	public Response request(String url, String originator, String xmlContent) throws IOException {
		Response response = new Response();
		// Instantiate a new Client
		//CloseableHttpClient client = HttpClients.createDefault();
		HttpClient client = new DefaultHttpClient();		// Instantiate the correct Http Method
		HttpPost req = new HttpPost(url);
		
		// add headers
		req.addHeader(ORIGIN_HEADER, originator);
		req.addHeader(ACCEPT, XML);
		
		//ControllerMessage message = new ControllerMessage("AvatarX", "contentC", "conversation n1200", "0115742");
		
		HttpEntity entity = new ByteArrayEntity(xmlContent.getBytes("UTF-8"));
		req.setEntity(entity);
		
		try {
			// send request
			//CloseableHttpResponse reqResp = client.execute(get);
			HttpResponse reqResp = client.execute(req);
			//reqResp.
			response.setStatusCode(reqResp.getStatusLine().getStatusCode());
			response.setRepresentation(IOUtils.toString(reqResp.getEntity().getContent(), "UTF-8"));
			//IOUtils.toString("x");
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			//client.close();
		}
		// return response
		return response;
	}

}
