package org.motechproject.config.domain;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.motechproject.mds.annotations.Access;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.util.SecurityMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.apache.commons.io.FilenameUtils.isExtension;

/**
 * Class representing a record of a certain module properties.
 *
 * This class is exposed as an {@link org.motechproject.mds.annotations.Entity} through
 * Motech Data Services.
 *
 * @see org.motechproject.mds.annotations
 */
@Entity(recordHistory = true)
@Access(value = SecurityMode.PERMISSIONS, members = {"manageSettings"})
public class ModulePropertiesRecord {

    @Ignore
    private static final Logger LOGGER = LoggerFactory.getLogger(ModulePropertiesRecord.class);

    @Ignore
    public static final String PROPERTIES_FILE_EXTENSION = "properties";

    @Field
    private Map<String, Object> properties;

    @Field
    private String version;

    @Field(required = true)
    private String bundle;

    @Field(required = true)
    private String filename;

    @Field
    private boolean raw;

    /**
     * Default constructor.
     */
    public ModulePropertiesRecord() {
        this((Map<String, Object>) null, null, null, null, false);
    }

    /**
     * Constructor.
     *
     * @param properties  the module properties
     * @param bundle  the modules bundle symbolic name
     * @param version  the version of the module
     * @param filename  the name of the file containing module properties
     * @param raw  the flag defining whether the properties are raw or not
     */
    public ModulePropertiesRecord(Map<String, Object> properties, String bundle, String version, String filename, boolean raw) {
        this.properties = properties;
        this.version = version;
        this.bundle = bundle;
        this.filename = filename;
        this.raw = raw;
    }

    /**
     * Constructor.
     *
     * @param props  the module properties
     * @param bundle  the modules bundle symbolic name
     * @param version  the version of the module
     * @param filename  the name of the file containing modules properties
     * @param raw  the flag defining whether the properties are raw or not
     */
    public ModulePropertiesRecord(Properties props, String bundle, String version, String filename, boolean raw) {
        this.properties = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            this.properties.put(entry.getKey().toString(), entry.getValue());
        }
        this.version = version;
        this.bundle = bundle;
        this.filename = filename;
        this.raw = raw;
    }

    /**
     * Builds an instance of {@code ModulePropertiesRecord} from given file. Content of the file must match format
     * specified in {@link java.util.Properties#load(java.io.Reader) Properties.load(Reader)}. Properties files are
     * treated as raw configuration files.
     *
     * @param file  the source file, null returns null
     * @return the instance of {@code ModulePropertiesRecord}, null if error occurred
     */
    public static ModulePropertiesRecord buildFrom(File file) {
        try (InputStream inputStream = FileUtils.openInputStream(file)) {
            final String fileName = file.getName();
            boolean raw = !isExtension(fileName, PROPERTIES_FILE_EXTENSION);
            Properties properties = buildProperties(inputStream, raw);
            String bundle = raw ? file.getParentFile().getParentFile().getName() : file.getParentFile().getName();
            return new ModulePropertiesRecord(properties, bundle, "", fileName, raw);
        } catch (IOException e) {
            LOGGER.error(String.format("Error reading config file %s", file.getAbsolutePath()), e);
            return null;
        }
    }

    private static Properties buildProperties(InputStream inputStream, boolean raw) throws IOException {
        Properties properties = new Properties();
        if (raw) {
            properties.put("rawData", IOUtils.toString(inputStream));
        } else {
            properties.load(inputStream);
        }
        return properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isRaw() {
        return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    /**
     * Checks whether given object is the same as this object.
     *
     * @param dataObject  the object to be compared
     * @return true if objects are the same, false otherwise
     */
    public boolean sameAs(Object dataObject) {
        ModulePropertiesRecord record = (ModulePropertiesRecord) dataObject;
        return new EqualsBuilder()
                .append(this.version, record.version)
                .append(this.bundle, record.bundle)
                .append(this.filename, record.filename)
                .append(this.raw, record.raw)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(bundle, properties, filename, raw);
    }

    @Override
    public String toString() {
        return String.format("ModulePropertiesRecord{bundle='%s', filename='%s', properties=%s, raw='%s'}",
                bundle, filename, properties, raw);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ModulePropertiesRecord other = (ModulePropertiesRecord) obj;

        return Objects.equals(this.filename, other.filename) &&
                Objects.equals(this.version, other.version) &&
                Objects.equals(this.bundle, other.bundle) &&
                Objects.equals(this.properties, other.properties) &&
                Objects.equals(this.raw, other.raw);
    }
}
