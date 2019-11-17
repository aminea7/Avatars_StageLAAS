package fr.insa.laas.HTTP;

public class Response {

	String representation;
	int statusCode;

	public Response() {
		representation = "";
		statusCode = 0;
	}

	public Response(String rep, int status) {
		this.representation = rep;
		this.statusCode = status;
	}

	public String getRepresentation() {
		return representation;
	}

	public void setRepresentation(String representation) {
		this.representation = representation;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public String toString() {
		return "Response [representation=" + representation + ", statusCode="
				+ statusCode + "]";
	}

}