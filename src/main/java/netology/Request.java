package netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Request {

    private String method;
    private String path;
    private List<NameValuePair> queryParams;
    private Map<String, String> headers;
    private String body;

    private List<List<NameValuePair>> postParams;

    public Request(String method,
                   String path,
                   List<NameValuePair> queryParams,
                   Map<String, String> headers,
                   String body,
                   List<List<NameValuePair>>postParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = body;
        this.postParams = postParams;
    }

    public List<List<NameValuePair>> getPostParams() {
        return postParams;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }


    public static Request parseRequest(BufferedReader in) {
        final String requestLine;
        try {
            requestLine = in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            throw new RuntimeException("Request line should be contains 3 elements");
        }

        String method = parts[0];
        String url = parts[1];

        List<List<NameValuePair>> postParams = null;
        String rawBody = null;

        if (!Objects.equals(method, "GET")) {
            rawBody = getRawBody(in);
            postParams = getPostParam(rawBody);
        }

        URI uri;

        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return new Request(method, uri.getPath(), getQueryParams(url), null, rawBody, postParams);

    }

    private static List<NameValuePair> getQueryParams(String url) {
        try {
            return URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getRawBody(BufferedReader in) {
        String request = in.lines().collect(Collectors.joining("\n"));
        int startRawBody = request.indexOf("\n\n");
        String rawBody = request.substring(startRawBody);
        return rawBody.trim();
    }

    private static List<List<NameValuePair>> getPostParam(String rawBody) {
        List<List<NameValuePair>> postBodyParam = new ArrayList<>();
        String[] split1 = rawBody.split("&");
        for (String s : split1) {
            List<NameValuePair> parse = URLEncodedUtils.parse(s, StandardCharsets.UTF_8);
            postBodyParam.add(parse);
        }
        return postBodyParam;
    }
}
