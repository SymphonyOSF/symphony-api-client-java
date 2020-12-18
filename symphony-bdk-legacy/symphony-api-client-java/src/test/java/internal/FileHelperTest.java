package internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class FileHelperTest {

  @Test
  public void should_read_file_from_classpath() throws FileNotFoundException {
    assertNotNull(FileHelper.readFile("/avatar.png"));
    assertNotNull(FileHelper.readFile("classpath:/avatar.png"));
  }

  @Test
  public void should_read_file_from_system() throws IOException {
    final Path file = Files.createTempFile(UUID.randomUUID().toString(), ".txt");
    assertNotNull(FileHelper.readFile(file.toAbsolutePath().toString()));
    Files.delete(file);
  }

  @Test(expected = FileNotFoundException.class)
  public void fail_to_read_file() throws FileNotFoundException {
    FileHelper.readFile(UUID.randomUUID().toString() + ".abc");
  }

  @Test
  public void classpath_path_with_slash_on_first() {
    assertEquals("classpath:/path/to/file.pem", FileHelper.path("classpath:/path/to/", "file.pem"));
  }

  @Test
  public void classpath_path_with_slash_on_second() {
    assertEquals("classpath:/path/to/file.pem", FileHelper.path("classpath:/path/to", "/file.pem"));
  }

  @Test
  public void classpath_path_with_two_slashes() {
    assertEquals("classpath:/path/to/file.pem", FileHelper.path("classpath:/path/to/", "/file.pem"));
  }

  @Test
  public void classpath_path_with_no_slashes() {
    assertEquals("classpath:/path/to/file.pem", FileHelper.path("classpath:/path/to", "file.pem"));
  }

  @Test
  public void only_classpath_on_first_slash_on_first() {
    assertEquals("classpath:/file.pem", FileHelper.path("classpath:/", "file.pem"));
  }

  @Test
  public void only_classpath_on_first_slash_on_second() {
    assertEquals("classpath:/file.pem", FileHelper.path("classpath:", "/file.pem"));
  }

  @Test
  public void only_classpath_first_with_no_slash() {
    assertEquals("classpath:/file.pem", FileHelper.path("classpath:", "file.pem"));
  }

}
