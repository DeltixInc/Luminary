package deltix.luminary;

import java.io.*;

public class Helper {
    public static File extractFileFromResources(String path) throws IOException {
        return extractFileFromResources(null, path);
    }

    public static File extractFileFromResources(File fileDir, String path) throws IOException {
        if (!path.startsWith("/"))
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");

        // Obtain filename from path.
        String[] parts = path.split("/");
        final String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

        // Split filename to prefix and suffix (extension).
        String prefix = "";
        String suffix = null;
        if (filename != null) {
            parts = filename.split("\\.", 2);
            prefix = parts[0];
            suffix = (parts.length > 1) ? "." + parts[parts.length - 1] : null; // Thanks, davs! :-)
        }

        // Check if the filename is okay.
        if (filename == null || prefix.length() < 3)
            throw new IllegalArgumentException("The filename has to be at least 3 characters long.");

        // Prepare temporary file.
        final File temp = File.createTempFile(prefix, suffix, fileDir);
        if (!temp.exists())
            throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");

        // Open and check input stream.
        final InputStream is = Helper.class.getResourceAsStream(path);
        if (is == null)
            throw new FileNotFoundException("File " + path + " was not found inside JAR.");

        try {
            // Prepare buffer for data copying.
            byte[] buffer = new byte[64 * 1024];
            int readBytes;

            // Open output stream and copy data between source file in JAR and the temporary file.
            final OutputStream os = new FileOutputStream(temp);
            try {
                while ((readBytes = is.read(buffer)) != -1)
                    os.write(buffer, 0, readBytes);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }

        return temp;
    }
}
