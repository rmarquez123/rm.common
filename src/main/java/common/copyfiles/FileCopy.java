package common.copyfiles;

import common.types.Progress;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 *
 * @author Ricardo Marquez
 */
public final class FileCopy {

  private final Property<Progress> progress = new SimpleObjectProperty<>();

  /**
   *
   * @return
   */
  public Property<Progress> progress() {
    return this.progress;
  }

  /**
   *
   * @param src
   * @param dest
   * @throws IOException
   */
  public void copyFolder(Path src, Path dest) throws IOException {
    this.progress.setValue(new Progress(0, 
      String.format("getting files count for source folder '%s' ....", src)));
    int total = this.getFileCount(src);
    this.progress.setValue(null);
    try (Stream<Path> stream = Files.walk(src)) {
      MutableObject<Integer> counter = new MutableObject<>(0);
      stream.forEach(source -> {
        this.copy(source, dest.resolve(src.relativize(source)));
        this.updateProgress(total, counter, source);
      });
    }
  }
  
  /**
   *
   * @param src
   * @return
   */
  private int getFileCount(Path src) {
    MutableObject<Integer> counter = new MutableObject<>(0);
    try (Stream<Path> stream = Files.walk(src)) {
      stream.forEach(source -> {
        count(source, counter);
      });
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return counter.getValue();
  }

  /**
   *
   * @param total
   * @param counter
   * @param source
   */
  private synchronized void updateProgress( //
    int total, MutableObject<Integer> counter, Path source) {
    if (source.toFile().isFile()) {
      counter.setValue(counter.getValue() + 1);
      double percent = counter.getValue() / (double) total * 100;
      String message = source.toFile().getAbsolutePath();
      Progress p = new Progress(percent, message);
      this.progress.setValue(p);
    }
  }

  /**
   *
   * @param source
   * @param dest
   */
  private void count(Path source, MutableObject<Integer> count) {
    try {
      File[] listFiles = source.toFile().listFiles((f) -> f.isFile());
      int numfiles = listFiles == null ? 0 : listFiles.length;
      int newvalue = count.getValue() + numfiles;
      count.setValue(newvalue);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   *
   * @param source
   * @param dest
   */
  private void copy(Path source, Path dest) {
    if (this.shouldCopy(source, dest)) {
      this.doCopy(source, dest);
    }
  }
  
  /**
   * 
   * @param source
   * @param dest
   * @throws RuntimeException 
   */
  private void doCopy(Path source, Path dest) {
    try {
      if (dest.toFile().exists()) {
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
      } else {
        if (!dest.getParent().toFile().exists()) {
          dest.getParent().toFile().mkdirs();
        }
        Files.copy(source, dest, StandardCopyOption.COPY_ATTRIBUTES);
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   *
   * @param source
   * @param dest
   * @return
   */
  private boolean shouldCopy(Path source, Path dest) {
    boolean result;
    if (source.toFile().isDirectory()) {
      result = this.isDirectoryEmpty(dest);
    } else {
      if (dest.toFile().exists()) {
        result = this.filesNotEqual(dest, source);
      } else {
        result = true;
      }
    }
    return result;
  }
  
  /**
   * 
   * @param dest
   * @param source
   * @return 
   */
  private boolean filesNotEqual(Path dest, Path source) {
    return !(dest.toFile().lastModified() == source.toFile().lastModified()
      && dest.toFile().length() == source.toFile().length());
  }
  
  /**
   * 
   * @param dest
   * @return 
   */
  private boolean isDirectoryEmpty(Path dest) {
    return dest.toFile().listFiles() == null;
  }
}
