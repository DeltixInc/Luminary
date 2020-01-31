package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Represents the location of a file or directory within the (ZIP) archive.
 */
public class ZipArchiveLocation implements Location {
    @NotNull
    private final ZipFile file;
    @Nullable
    private final ZipEntry entry;

    public ZipArchiveLocation(@NotNull ZipFile file, @Nullable String relativePath) throws IOException {
        this.file = file;
        if (relativePath != null && !relativePath.isEmpty()) {
            this.entry = locateEntry(file, relativePath);
            if (this.entry == null)
                throw new IOException("File or directory '" + relativePath + "' is not found within the archive.");
        } else
            this.entry = null;
    }

    public ZipArchiveLocation(@NotNull ZipFile file) throws IOException {
        this(file, null);
    }

    public ZipArchiveLocation(@NotNull String filePath, @Nullable String relativePath) throws IOException {
        this(new ZipFile(filePath), relativePath);
    }

    public ZipArchiveLocation(@NotNull String filePath) throws IOException {
        this(filePath, null);
    }

    public ZipFile getArchive() {
        return file;
    }

    public String getPath() {
        return entry != null ? "/" + entry.getName() : "/";
    }

    public boolean isDirectory() {
        return entry == null || entry.isDirectory();
    }

    public Location getParent() throws IOException {
        if (entry == null)
            return new FileSystemLocation(Paths.get(file.getName()).getParent());

        final String path = entry.getName().endsWith("/")
            ? entry.getName().substring(0, entry.getName().length() - 1)
            : entry.getName();
        final int index = path.lastIndexOf('/');
        if (index <= 0)
            return new ZipArchiveLocation(file, null);
        return new ZipArchiveLocation(file, path.substring(0, index + 1));
    }

    public String getFileName() {
        if (entry == null)
            return Paths.get(file.getName()).getFileName().toString();

        final String path = entry.getName().endsWith("/")
            ? entry.getName().substring(0, entry.getName().length() - 1)
            : entry.getName();
        final int index = path.lastIndexOf('/');
        return path.substring(index + 1);
    }

    public ZipArchiveLocation resolve(@NotNull String path) throws IOException {
        final String relativePath = path.replace('\\', '/');
        if (relativePath.startsWith("/"))
            return new ZipArchiveLocation(file, relativePath);

        String currentPath = entry != null ? entry.getName() : "/";
        for (int i = 0; i < relativePath.length(); ) {
            int j = i + 1;
            while (j < relativePath.length() && relativePath.charAt(j) != '/')
                j += 1;
            if (j == i) {
                i = j + 1;
                continue;
            }

            final String component = relativePath.substring(i, j);
            if (".".equals(component)) {
                i = j + 1;
                continue;
            }
            if ("..".equals(component)) {
                if ("/".equals(currentPath))
                    throw new IOException("Relative path cannot point outside of the archive.");
                final int k = currentPath.lastIndexOf('/');
                currentPath = k < 0 ? "/" : currentPath.substring(0, k + 1);
            } else {
                if (!currentPath.endsWith("/"))
                    currentPath += "/";
                currentPath += component;
            }
            i = j + 1;
        }

        return new ZipArchiveLocation(file, currentPath.startsWith("/") ? currentPath.substring(1) : currentPath);
    }

    @Override
    public String relativize(Location location) throws IOException {
        if (!(location instanceof ZipArchiveLocation))
            throw new IllegalArgumentException("Cannot relativize against non-ZIP location.");
        final ZipArchiveLocation that = (ZipArchiveLocation) location;
        if (!Files.isSameFile(Paths.get(file.getName()), Paths.get(that.file.getName())))
            throw new IllegalArgumentException("Cannot relativize against different ZIP archive.");
        return Paths.get(getPath()).relativize(Paths.get(that.getPath())).toString().replace('\\', '/');
    }

    public List<Location> listFiles() throws IOException {
        if (!isDirectory()) {
            assert entry != null;
            throw new NotDirectoryException(entry.getName());
        }

        final List<Location> files = new ArrayList<>();
        final Enumeration<? extends ZipEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry nextEntry = entries.nextElement();
            if (entry == null || isPrefix(entry.getName(), nextEntry.getName()))
                files.add(new ZipArchiveLocation(file, nextEntry.getName()));
        }
        return files;
    }

    public InputStream readAsStream() throws IOException {
        if (entry == null)
            throw new IOException("Cannot open a directory as a stream.");
        return file.getInputStream(entry);
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof ZipArchiveLocation && equals((ZipArchiveLocation) that);
    }

    public boolean equals(ZipArchiveLocation that) {
        try {
            return that != null
                && Files.isSameFile(Paths.get(file.getName()), Paths.get(that.file.getName()))
                && (entry != null
                    ? entry.getName().equals(that.entry != null ? that.entry.getName() : null)
                    : that.entry == null);
        } catch (IOException exception) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return file.getName().hashCode() ^ (entry != null ? entry.getName().hashCode() : 42);
    }

    @Override
    public String toString() {
        return file.getName() + getPath();
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    @Nullable
    private static ZipEntry locateEntry(@NotNull ZipFile zipFile, @NotNull String entryName) {
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (matches(entryName, entry.getName()))
                return entry;
        }
        return null;
    }

    private static boolean isPrefix(@NotNull CharSequence prefix, @NotNull CharSequence path) {
        if (prefix.length() > path.length())
            return false;
        return areEqual(prefix, path, prefix.length());
    }

    private static boolean matches(@NotNull CharSequence expected, @NotNull CharSequence actual) {
        if (expected.length() != actual.length())
            return false;
        return areEqual(expected, actual, expected.length());
    }

    private static boolean areEqual(@NotNull CharSequence path1, @NotNull CharSequence path2, int length) {
        for (int i = 0; i < length; i += 1) {
            final char e = path1.charAt(i);
            final char a = path2.charAt(i);
            if (isSeparator(e) && isSeparator(a))
                continue;
            if (e != a)
                return false;
        }
        return true;
    }

    private static boolean isSeparator(char c) {
        return c == '/' || c == '\\';
    }
}
