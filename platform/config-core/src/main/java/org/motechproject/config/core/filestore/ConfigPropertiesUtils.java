package org.motechproject.config.core.filestore;

import org.motechproject.config.core.MotechConfigurationException;
import org.motechproject.config.core.domain.ConfigLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

/**
 * A utility class for loading properties.
 */
public final class ConfigPropertiesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigPropertiesUtils.class);

    /**
     * Loads the properties from given {@code File}.
     *
     * @param file  the file with properties
     * @return the loaded properties
     * @throws IOException if I/O error occurred
     */
    public static Properties getPropertiesFromFile(File file) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            properties.load(fileInputStream);
        }
        return properties;
    }

    /**
     * Saves properties to the given {@code File}.
     *
     * @param file the file
     * @param properties
     */
    public static void saveConfig(File file, Properties properties) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (Writer writer = new FileWriter(file)) {
                properties.store(writer, String.format("MOTECH %s properties.", file.getName()));
            }
        } catch (IOException e) {
            throw new MotechConfigurationException(String.format("Error saving %s properties to file.", file.getName()), e);
        }
    }

    /**
     * Returns default config file location.
     *
     * @param accessType the access required for returned File object
     * @param configLocations the config locations specified by MOTECH in config-locations.properties
     * @param fileName the file name
     * @return
     */
    public static File getDefaultPropertiesFile(ConfigLocation.FileAccessType accessType, Iterable<ConfigLocation> configLocations,
                                                String fileName) {
        StringBuilder sb = new StringBuilder("");

        for (ConfigLocation configLocation : configLocations) {
            sb.append(configLocation.getLocation()).append(' ');
            try {
                return configLocation.getFile(fileName, accessType);
            } catch (MotechConfigurationException e) {
                LOGGER.warn(e.getMessage());
            }
        }

        throw new MotechConfigurationException(String.format("%s file is not %s from any of the default locations. Searched directories: %s.", fileName, accessType.toString(), sb));
    }

    /**
     * Loads the properties from given {@code String}.
     * The format of this {@code String} should be "key1=value1;key2=value2;key3=value3;...".
     *
     * @param string  the string with properties
     * @return the loaded properties
     */
    public static Properties getPropertiesFromSystemVarString(String string) {
        if (string == null) {
            return null;
        }

        Properties properties = new Properties();
        String stringToParse = string;

        // The properties should look like "key1=value1;key2=value2;key3=value3;..."
        while (!stringToParse.isEmpty()) {
            String property;
            int endOfProperty = stringToParse.indexOf(';');

            if (endOfProperty >= 0) {
                property = stringToParse.substring(0, endOfProperty);
            } else {
                property = stringToParse;
                endOfProperty = stringToParse.length() - 1;
            }

            int endOfKey = property.indexOf('=');

            String key = property.substring(0, endOfKey).trim();
            String value = property.substring(endOfKey + 1).trim();
            stringToParse = stringToParse.substring(endOfProperty + 1);

            properties.setProperty(key, value);
        }
        return properties.isEmpty() ? null : properties;
    }

    /**
     * This is an utility class and should not be initiated.
     */
    private ConfigPropertiesUtils() {
    }
}
