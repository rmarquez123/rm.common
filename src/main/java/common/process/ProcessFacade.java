package common.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
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

  /**
   *
   * @param statement
   */
  private ProcessFacade(String statement, Consumer<String> outputProcessor) {
    this.statement = statement;
    this.outputProcessor = outputProcessor;
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
    String[] args = this.statement.split("\\s+(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
    ProcessBuilder pb = new ProcessBuilder(args);
    pb.redirectErrorStream(true);
    if (this.directory != null) {
      pb.directory(this.directory);
    }
    List<String> outputLines;
    try {
      Process process = pb.start();
      process.waitFor(5, TimeUnit.SECONDS);
      Charset utf8 = Charset.forName("UTF-8");
      this.throwExceptionIfAnyErrors(process, utf8);
      outputLines = IOUtils.readLines(process.getInputStream(), utf8);
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
    String[] args = this.statement.split("\\s+(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
    ProcessBuilder pb = new ProcessBuilder(args);
    pb.redirectErrorStream(true);
    if (this.directory != null) {
      pb.directory(this.directory);
    }
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

    /**
     *
     * @return
     */
    public ProcessFacade build() {
      Objects.requireNonNull(this.statement);
      Objects.requireNonNull(this.outputProcessor);
      ProcessFacade instance = new ProcessFacade(statement, outputProcessor);
      instance.directory(this.rundirectory);
      return instance;
    }

    /**
     *
     */
    public void run() {
      this.build().run();
    }

    /**
     *
     * @param onDone
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
