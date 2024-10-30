package ee.jakarta.examples.dockertests;

public class Server {

	private String name;
	private String port;
	
	public Server(String name, String port) {
		super();
		this.name = name;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}
