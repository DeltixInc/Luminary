package deltix.luminary;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileSystemLocation implements Location {
    private final Path path;

    public FileSystemLocation(Path path) {
        this.path = path.toAbsolutePath().normalize();
    }

    public FileSystemLocation(String path) {
        this(Paths.get(path));
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public boolean equals(FileSystemLocation that) {
        return that != null && path.equals(that.path);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileSystemLocation && equals((FileSystemLocation) obj);
    }

    @Override
    public String toString() {
        return path.toString();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public Location getParent() {
        return new FileSystemLocation(path.getParent());
    }

    @Override
    public String getFileName() {
        return path.getFileName().toString();
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public InputStream readAsStream() throws FileNotFoundException {
        return new FileInputStream(path.toFile());
    }

    private void listFilesIn(File directory, List<Location> files) {
        final File[] fileList = directory.listFiles();
        if (fileList == null)
            return;

        for (File file : fileList) {
            files.add(new FileSystemLocation(file.getPath()));
            if (file.isDirectory())
                listFilesIn(file, files);
        }
    }

    @Override
    public List<Location> listFiles() {
        final List<Location> files = new ArrayList<>();
        final File rootDirectory = path.toFile();
        if (!rootDirectory.exists() || !rootDirectory.isDirectory())
            return files;
        listFilesIn(rootDirectory, files);
        return files;
    }

    @Override
    public Location resolve(String relativePath) {
        return new FileSystemLocation(path.resolve(relativePath));
    }

    @Override
    public String relativize(Location location) throws IOException {
        if (location instanceof FileSystemLocation)
            return path.relativize(((FileSystemLocation)location).path).toString();
        throw new IllegalArgumentException("Cannot relativize give location against another location of different kind.");
    }
}
