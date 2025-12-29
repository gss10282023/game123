package pacman.util;

import pacman.ConfigurationParseException;

import java.io.InputStream;
import java.net.URL;

public final class ResourceUtils {

    private ResourceUtils() {
    }

    public static String toExternalForm(String resourcePath) {
        URL url = getUrl(resourcePath);
        return url.toExternalForm();
    }

    public static InputStream getStream(String resourcePath) {
        String normalized = normalizeClasspathPath(resourcePath);
        InputStream inputStream = ResourceUtils.class.getResourceAsStream(normalized);
        if (inputStream == null) {
            throw new ConfigurationParseException("Resource not found: " + normalized);
        }
        return inputStream;
    }

    private static URL getUrl(String resourcePath) {
        String normalized = normalizeClasspathPath(resourcePath);
        URL url = ResourceUtils.class.getResource(normalized);
        if (url == null) {
            throw new ConfigurationParseException("Resource not found: " + normalized);
        }
        return url;
    }

    private static String normalizeClasspathPath(String path) {
        if (path.startsWith("/")) {
            return path;
        }
        return "/" + path;
    }
}
