package client.handler;

public interface RequestBuilder {
    String buildRequest(String userName, String command, String body);
}
