package common.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 *
 * @author Ricardo Marquez
 */
public class ProcessFacade {

  private final String statement;
  private final Consumer<String> outputProcessor;
  private File directory = null;
  private final Map<String, String> envvars;
  private final Set<File> paths;

  /**
   *
   * @param statement
   */
  private ProcessFacade(String statement, // 
    Consumer<String> outputProcessor, Map<String, String> envvars, Set<File> paths) {
    this.statement = statement;
    this.outputProcessor = outputProcessor;
    this.envvars = envvars;
    this.paths = paths;
  }

  /**
   *
   * @param directory
   */
  private void directory(File directory) {
    this.directory = directory;
  }

  /**
   * Runs on the same thread.
   */
  public void run() {
    ProcessBuilder pb = this.createProcessBuilder(); 
    
    List<String> outputLines = new ArrayList<>();
    try {
      Process process = pb.start();
      process.waitFor(5, TimeUnit.SECONDS);
      Charset utf8 = Charset.forName("UTF-8");
      this.throwExceptionIfAnyErrors(process, utf8);
      try (BufferedReader reader // 
        = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          outputLines.add(line); 
        }
      } catch(Exception ex) {
        throw new RuntimeException(ex);
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    outputLines.stream().forEach(this.outputProcessor);
  }

  /**
   *
   * @param onDone
   */
  public Process runOnNewThread(Runnable onDone) {
    ProcessBuilder pb = this.createProcessBuilder();
    MutableObject<Process> result = new MutableObject<>();
    Process process;
    try {
      process = pb.start();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    result.setValue(process);
    new Thread(() -> {
      try {
        StreamGobbler ingobbler = StreamGobbler //
          .create(process.getInputStream(), this.outputProcessor);
        ingobbler.run();
        int exitvalue = process.waitFor();
        onDone.run();
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }).start();
    return result.getValue();
  }
  
  /**
   * 
   * @return 
   */
  private ProcessBuilder createProcessBuilder() {
    String[] args = this.statement //
      .split("\\s+(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
    ProcessBuilder pb = new ProcessBuilder(args);
    pb.redirectErrorStream(true);
    pb.environment().putAll(this.envvars);
    if (this.directory != null) {
      pb.directory(this.directory);
    }
    String oldpath = pb.environment().get("PATH");
    StringBuilder newpath = new StringBuilder(oldpath == null ? "": oldpath);
    this.paths.forEach(f -> newpath.append(";").append(f.getAbsolutePath()));
    pb.environment().put("PATH", newpath.toString());
    return pb;
  }

  /**
   *
   * @param process
   * @param utf8
   * @throws IOException
   * @throws RuntimeException
   */
  private void throwExceptionIfAnyErrors(Process process, Charset utf8) {
    List<String> errorLines;
    try {
      errorLines = IOUtils.readLines(process.getErrorStream(), utf8);
      if (errorLines != null && !errorLines.isEmpty()) {
        throw new RuntimeException(String.join("\n", errorLines));
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);

    }
  }

  /**
   *
   */
  public static class Builder {

    private String statement;
    private Consumer<String> outputProcessor;
    private File rundirectory;
    private Map<String, String> envars = new HashMap<>();
    private final Set<File> paths = new HashSet<>();

    public Builder withStatement(String statement) {
      this.statement = statement;
      return this;
    }

    public Builder withOutputProcessor(Consumer<String> outputProcessor) {
      this.outputProcessor = outputProcessor;
      return this;
    }

    public Builder withRunDirectory(File parent) {
      this.rundirectory = parent;
      return this;
    }
    
    public Builder withEnvironmentVars(Map<String, String> envvars) {
      this.envars = envvars;
      return this;
    }
    
    /**
     * 
     * @param file
     * @return 
     */
    public Builder includePath(File file) {
      this.paths.add(file); 
      return this;
    }

    /**
     *
     * @return
     */
    public ProcessFacade build() {
      Objects.requireNonNull(this.statement, "Executable statement cannot be null");
      Objects.requireNonNull(this.outputProcessor, "Output processor cannot  be null");
      ProcessFacade instance = new ProcessFacade(statement, outputProcessor, envars, paths);
      instance.directory(this.rundirectory);
      return instance;
    }

    /**
     *
     */
    public void run() {
      MutableObject<Boolean> done= new MutableObject<>(false);
      this.build().runOnNewThread(()->{
        done.setValue(true);
      });
      this.stall(done);
    }
    
    /**
     * 
     * @param done 
     */
    private void stall(MutableObject<Boolean> done) {
      try {
        while (!done.getValue()) {
          Thread.sleep(10);
        }
      } catch (InterruptedException ex) {
        throw new RuntimeException(ex);
      }
    }

    /**
     *
     * @param onDone
     * @return 
     */
    public Process runOnNewThread(Runnable onDone) {
      return this.build().runOnNewThread(onDone);
    }
  }

  static class StreamGobbler implements Runnable {

    private final InputStream inputStream;
    private final Consumer<String> consumeInputLine;
    private BufferedReader reader;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
      this.inputStream = inputStream;
      this.consumeInputLine = consumeInputLine;
    }

    /**
     *
     * @param inputStream
     * @param consumeInputLine
     * @return
     */
    public static StreamGobbler create(InputStream inputStream, Consumer<String> consumeInputLine) {
      return new StreamGobbler(inputStream, consumeInputLine);
    }

    public void close() {
      try {
        this.reader.close();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    @Override
    public void run() {
      if (this.reader != null) {
        throw new RuntimeException();
      }
      this.reader = new BufferedReader(new InputStreamReader(inputStream));
      this.reader.lines().forEach(consumeInputLine);
    }
  }
}
