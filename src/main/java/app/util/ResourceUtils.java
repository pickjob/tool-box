package app.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author pickjob@126.com
 * @date 2020-12-14
 */
public class ResourceUtils {
    private static final Logger logger = LogManager.getLogger(ResourceUtils.class);

    public static URI loadClasspathResourceAsURI(String path) {
        try {
            return Thread.currentThread()
                    .getContextClassLoader()
                    .getResource(path)
                    .toURI();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static URL loadClasspathResourceAsURL(String path) {
        try {
            return loadClasspathResourceAsURI(path).toURL();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static String loadClasspathResourceAsString(String path) {
        URI uri = loadClasspathResourceAsURI(path);
        if (uri != null) {
            return uri.toString();
        }
        return "";
    }

    public static List<Path> loadClasspathResourceAsPaths(String directory) {
        List<Path> locations = new ArrayList<>();
        try {
            URI uri = loadClasspathResourceAsURI(directory);
            if (uri == null) {
                return locations;
            }
            Path directoryPath = null;
            if (Constants.JAR_SCHEMA.equals(uri.getScheme())) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
                directoryPath = fileSystem.getPath(directory);
            } else {
                directoryPath = Paths.get(uri);
            }
            Stream<Path> stream = Files.walk(directoryPath, Constants.FXML_MAX_DEEPTH);
            stream.sorted((p1, p2) -> {
                return p1.getFileName().compareTo(p2.getFileName());
            }).forEach(p -> {
                if (!Files.isDirectory(p)) {
                    locations.add(p);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return locations;
    }

    public static File loadDefaultIniConfigFile() {
        Object userHome = System.getProperties().get("user.home");
        if (userHome != null) {
            try {
                File configFile = new File(userHome + File.separator + Constants.DEFAULT_INI_FILE);
                if (!configFile.exists()) {
                    configFile.getParentFile().mkdirs();
                    configFile.createNewFile();
                }
                return configFile;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }
}
