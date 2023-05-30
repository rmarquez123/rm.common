package common.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
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
    URL updatedUrl = this.getUpdatedUrl();
    this.read(updatedUrl, consumer);
  }

  /**
   *
   * @return
   */
  private URL getUpdatedUrl() {
    String query = this.url.getQuery();
    String path = query == null ? this.url.toString() : this.url.toString().replace("", "");
    String newQueryParams = this.requestParams.entrySet().stream()
      .map((e) -> e.getKey() + "=" + e.getValue())
      .collect(Collectors.joining("&"));
    URL updatedUrl;
    if (!newQueryParams.isEmpty()) {
      if (query == null) {
        updatedUrl = this.toUrl(path + "?" + newQueryParams);
      } else {
        updatedUrl = this.toUrl(path + query + "&" + newQueryParams);
      }
    } else {
      updatedUrl = this.url;
    }
    return updatedUrl;
  }

  /**
   *
   * @return
   */
  private URL toUrl(String spec) {
    try {
      return new URL(spec);
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void post(URL updatedUrl, Consumer<String> consumer) {
    InputStream connInputStream = this.getInputStream(updatedUrl, "POST");
    Charset charset = Charset.forName("UTF-8");
    InputStreamReader inputStreamReader = new InputStreamReader(connInputStream, charset);
    try (BufferedReader stream = new BufferedReader(inputStreamReader)) {
      String line;
      while ((line = stream.readLine()) != null) {
        consumer.accept(line);
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   *
   * @param updatedUrl
   * @param consumer
   */
  private void read(URL updatedUrl, Consumer<String> consumer) {
    InputStream connInputStream = this.getInputStream(updatedUrl);
    Charset charset = Charset.forName("UTF-8");
    InputStreamReader inputStreamReader = new InputStreamReader(connInputStream, charset);
    try (BufferedReader stream = new BufferedReader(inputStreamReader)) {
      String line;
      while ((line = stream.readLine()) != null) {
        consumer.accept(line);
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   *
   * @param updatedUrl
   * @return
   */
  private InputStream getInputStream(URL updatedUrl) {
    return this.getInputStream(updatedUrl, "GET");
  }

  /**
   *
   * @param updatedUrl
   * @return
   */
  private InputStream getInputStream(URL updatedUrl, String requestMethod) {
    try {
      HttpURLConnection connection = (HttpURLConnection) updatedUrl.openConnection();
      connection.setRequestMethod(requestMethod);
      this.attrs.forEach((k, v) -> connection.setRequestProperty(k, v));
      InputStream connInputStream = this.getInputStream(connection);
      return connInputStream;
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   *
   * @param connection
   * @return
   */
  private InputStream getInputStream(URLConnection connection) {
    InputStream connInputStream;
    try {
      connection.connect();
      String redirect = connection.getHeaderField("Location");
      if (redirect == null) {
        connInputStream = connection.getInputStream();
      } else {
        URLConnection redirectConn = this.toUrl(redirect).openConnection();
        connInputStream = this.getInputStream(redirectConn);
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return connInputStream;
  }

  private void post() {
    try {
      String newQueryParams = this.requestParams.entrySet().stream()
        .map((e) -> e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining("&"));
      HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      try (OutputStream os = connection.getOutputStream()) {
        os.write(newQueryParams.getBytes());
        os.flush();
      }
      connection.connect();
      int code = connection.getResponseCode();
      connection.getResponseMessage();
      if (code != 200) {
        InputStream stream = connection.getErrorStream();
        String error = IOUtils.readLines(stream, Charset.defaultCharset()) //
          .stream() //
          .collect(Collectors.joining("\n"));
        throw new RuntimeException(error);
      }
      System.out.println("code = " + code);
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
     * @param consumer
     */
    public <T> List<T> readTo(Function<String, T> consumer) {
      RmHttpReader reader = this.create();
      List<T> result = new ArrayList<>();
      reader.read((c) -> result.add(consumer.apply(c)));
      return result;
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
    public InputStream readStream() {
      RmHttpReader reader = this.create();
      reader.getUpdatedUrl();
      InputStream result = reader.getInputStream(url);
      return result;
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

    public void post() {
      RmHttpReader reader = this.create();
      reader.post();
    }
      
    
    /**
     * 
     * @param consumer 
     */
    public void post(Consumer<String> consumer) {
      RmHttpReader reader = this.create();
      URL updatedUrl = reader.getUpdatedUrl();
      reader.post(updatedUrl, consumer);
    }

    public JSONObject postAndReadJsonObject() {
      StringBuilder string = new StringBuilder();
      this.post(s -> string.append(s).append("\n"));
      JSONObject result;
      try {
        result = new JSONObject(string.toString());
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }
      return result;
    }

  }
}
