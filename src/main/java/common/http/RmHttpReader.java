package common.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Ricardo Marquez
 */
public class RmHttpReader {

  private final URL url;
  private final Map<String, String> attrs;
  private final Map<String, String> requestParams;

  private RmHttpReader(URL url, Map<String, String> requestParams, Map<String, String> attrs) {
    this.url = url;
    this.requestParams = requestParams;
    this.attrs = attrs;
  }

  public void read(Consumer<String> consumer) {
    try {
      String query = this.url.getQuery();
      String path = query == null ? this.url.toString() : this.url.toString().replace("", "");
      String newQueryParams = this.requestParams.entrySet().stream()
        .map((e) -> e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining("&"));
      URL updatedUrl;
      if (query == null) {
        updatedUrl = new URL(path + "?" + newQueryParams);
      } else {
        updatedUrl = new URL(path + query + "&" + newQueryParams);
      }
      URLConnection connection = (HttpURLConnection) updatedUrl.openConnection();
      this.attrs.forEach((k, v) -> connection.setRequestProperty(k, v));
      connection.connect();
      Charset charset = Charset.forName("UTF-8");
      InputStream connInputStream = connection.getInputStream();
      InputStreamReader inputStreamReader = new InputStreamReader(connInputStream, charset);
      try (BufferedReader stream = new BufferedReader(inputStreamReader)) {
        String line;
        while ((line = stream.readLine()) != null) {
          consumer.accept(line);
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static class Builder {

    private final Map<RmHttpCode, Consumer<String>> onCodeDelegates = new HashMap<>();
    private final URL url;
    private final Map<String, String> attrs = new HashMap<>();
    private final Map<String, String> requestParams = new HashMap<>();

    public Builder(URL url) {
      this.url = Objects.requireNonNull(url, "url cannot be null");
    }

    /**
     *
     * @param url
     */
    public Builder(String url) {
      this(stringToUrl(Objects.requireNonNull(url, "url cannot be null")));
    }

    /**
     *
     * @param string
     * @return
     */
    private static URL stringToUrl(String string) {
      URL result;
      try {
        result = new URL(string);
      } catch (MalformedURLException ex) {
        throw new RuntimeException(ex);
      }
      return result;
    }

    /**
     *
     * @param code
     * @param delegate
     * @return
     */
    public Builder onCode(RmHttpCode code, Consumer<String> delegate) {
      this.onCodeDelegates.put(code, delegate);
      return this;
    }

    /**
     *
     * @param consumer
     */
    public void read(Consumer<String> consumer) {
      RmHttpReader reader = this.create();
      reader.read(consumer);
    }

    /**
     *
     * @return
     */
    private RmHttpReader create() {
      RmHttpReader instance = new RmHttpReader(url, this.requestParams, this.attrs);
      return instance;
    }

    /**
     *
     * @return
     */
    public JSONObject readJsonObject() {
      StringBuilder string = new StringBuilder();
      this.read(s -> string.append(s).append("\n"));
      JSONObject result;
      try {
        result = new JSONObject(string.toString());
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }
      return result;
    }
    
    /**
     *
     * @return
     */
    public JSONArray readJsonArray() {
      StringBuilder string = new StringBuilder();
      this.read(s -> string.append(s).append("\n"));
      JSONArray result;
      try {
        result = new JSONArray(string.toString());
      } catch (JSONException ex) {  
        throw new RuntimeException(ex);  
      }
      return result;
    }

    public Builder setAttribute(String key, String value) {
      this.attrs.put(key, value);
      return this;
    }

    public Builder setRequestParam(String key, String value) {
      this.requestParams.put(key, value);
      return this;
    }
  }
}
