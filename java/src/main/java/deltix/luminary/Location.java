package deltix.luminary;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Describes the abstract location of a file possibly within a ZIP archive.
 */
public interface Location extends Closeable {
    Location getParent() throws IOException;

    String getFileName();

    boolean isDirectory();

    InputStream readAsStream() throws IOException;

    List<Location> listFiles() throws IOException;

    Location resolve(String relativePath) throws IOException;

    String relativize(Location location) throws IOException;
}
