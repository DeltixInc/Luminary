package deltix.luminary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ZipArchiveLocationTest {
    private File file;

    @Before
    public void setUp() throws IOException {
        file = Helper.extractFileFromResources("/ZipArchive.zip");
    }

    @After
    public void tearDown() {
        assertTrue(file.delete());
    }

    @Test
    public void canOpenFile1() throws IOException {
        try (final ZipArchiveLocation file1 = new ZipArchiveLocation(file.getAbsolutePath(), "File1")) {
            assertEquals("File1", file1.getFileName());
            assertFalse(file1.isDirectory());

            final Location root = file1.getParent();
            assertNotNull(root);
            assertEquals(file.getName(), root.getFileName());
            assertTrue(root.isDirectory());
        }
    }

    @Test
    public void canOpenFolder1() throws IOException {
        try (final ZipArchiveLocation folder1 = new ZipArchiveLocation(file.getAbsolutePath(), "Folder1/")) {
            assertEquals("Folder1", folder1.getFileName());
            assertTrue(folder1.isDirectory());

            final Location root = folder1.getParent();
            assertEquals(file.getName(), root.getFileName());
            assertNotNull(root);
            assertTrue(root.isDirectory());

            System.out.println();
            for (Location file : root.listFiles())
                System.out.println(file);
        }
    }

    @Test
    public void canOpenFolder1Folder2File1() throws IOException {
        try (final ZipArchiveLocation folder1Folder2File1 = new ZipArchiveLocation(file.getAbsolutePath(), "Folder1/Folder2/File1")) {
            assertEquals("File1", folder1Folder2File1.getFileName());
            assertFalse(folder1Folder2File1.isDirectory());

            final Location folder1Folder2 = folder1Folder2File1.getParent();
            assertEquals("Folder2", folder1Folder2.getFileName());
            assertNotNull(folder1Folder2);
            assertTrue(folder1Folder2.isDirectory());

            System.out.println();
            for (Location file : folder1Folder2.listFiles())
                System.out.println(file);

            final Location folder1 = folder1Folder2.getParent();
            assertEquals("Folder1", folder1.getFileName());
            assertNotNull(folder1);
            assertTrue(folder1.isDirectory());

            System.out.println();
            for (Location file : folder1.listFiles())
                System.out.println(file);

            final Location root = folder1.getParent();
            assertEquals(file.getName(), root.getFileName());
            assertNotNull(root);
            assertTrue(root.isDirectory());

            System.out.println();
            for (Location file : root.listFiles())
                System.out.println(file);
        }
    }

    @Test
    public void resolveAgainstRoot() throws IOException {
        try (final ZipArchiveLocation file = new ZipArchiveLocation(this.file.getAbsolutePath(), null)) {
            assertEquals(file, file.resolve("."));
        }
    }

    @Test
    public void resolve() throws IOException {
        try (final ZipArchiveLocation file = new ZipArchiveLocation(this.file.getAbsolutePath(), "Folder1/Folder2/File1")) {
            assertEquals("File1", file.getFileName());
            assertFalse(file.isDirectory());

            assertEquals(file, file.resolve("."));
            assertEquals(file, file.resolve("./."));
            assertEquals(file, file.resolve("././."));

            assertEquals(file.getParent(), file.resolve(".."));
            assertEquals(file.getParent(), file.resolve("../."));
            assertEquals(file.getParent(), file.resolve("./.."));
            assertEquals(file.getParent(), file.resolve(".././."));
            assertEquals(file.getParent(), file.resolve("./../."));
            assertEquals(file.getParent(), file.resolve("././.."));
            assertEquals(file.getParent(), file.resolve("./.././."));
            assertEquals(file.getParent(), file.resolve("././../."));

            assertEquals(file, file.resolve("../File1"));
            assertEquals(file, file.resolve(".././File1"));
            assertEquals(file, file.resolve("./../File1"));
            assertEquals(file, file.resolve("../././File1"));
            assertEquals(file, file.resolve("./.././File1"));
            assertEquals(file, file.resolve("././../File1"));
            assertEquals(file, file.resolve("./../././File1"));
            assertEquals(file, file.resolve("././.././File1"));

            try (final ZipArchiveLocation file3 = new ZipArchiveLocation(this.file.getAbsolutePath(), "Folder1/Folder2/File3")) {
                assertEquals(file3, file.resolve("../File3"));
                assertEquals(file3, file.resolve(".././File3"));
                assertEquals(file3, file.resolve("./../File3"));
                assertEquals(file3, file.resolve("../././File3"));
                assertEquals(file3, file.resolve("./.././File3"));
                assertEquals(file3, file.resolve("././../File3"));
                assertEquals(file3, file.resolve("./../././File3"));
                assertEquals(file3, file.resolve("././.././File3"));
            }

            try (final ZipArchiveLocation file3 = new ZipArchiveLocation(this.file.getAbsolutePath(), "Folder1/Folder2/File3")) {
                assertEquals(file3, file.resolve("../File3/./.././File3/./"));
                assertEquals(file3, file.resolve(".././File3/./.././File3/./"));
                assertEquals(file3, file.resolve("./../File3/./.././File3/./"));
                assertEquals(file3, file.resolve("../././File3/./.././File3/./"));
                assertEquals(file3, file.resolve("./.././File3/./.././File3/./"));
                assertEquals(file3, file.resolve("././../File3/./.././File3/./"));
                assertEquals(file3, file.resolve("./../././File3/./.././File3/./"));
                assertEquals(file3, file.resolve("././.././File3/./.././File3/./"));
            }
        }
    }
}
