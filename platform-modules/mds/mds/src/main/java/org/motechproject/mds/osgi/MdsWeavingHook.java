package org.motechproject.mds.osgi;

import org.motechproject.mds.domain.ClassData;
import org.motechproject.mds.javassist.MotechClassPool;
import org.motechproject.mds.util.ClassName;
import org.motechproject.mds.util.Constants;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * The MdsWeavingHook allows us to hook into the OSGi classloading process.
 * It replaces the DDE classes with their extended bytecode which we generated.
 * Moreover we dynamically add the required jdo imports.
 */
@Service
public class MdsWeavingHook implements WeavingHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(MdsWeavingHook.class);
    public static final String[] STANDARD_DYNAMIC_IMPORTS = new String[] {
            "javax.jdo", "javax.jdo.identity", "javax.jdo.spi", "org.joda.time",
            "org.apache.commons.lang", "org.springframework.transaction.support",
            "org.motechproject.mds.filter",
            "org.motechproject.mds.query", "org.motechproject.mds.util",
            "org.motechproject.commons.date.util", "org.datanucleus.enhancer", "org.datanucleus", "org.datanucleus.state",
            "org.datanucleus.asm", "org.datanucleus.exceptions", "org.datanucleus.metadata", "org.datanucleus.plugin",
            "org.datanucleus.util", "org.datanucleus.identity"
    };

    @Override
    public void weave(WovenClass wovenClass) {
        String className = wovenClass.getClassName();

        // we should omit classes from slf4j library to avoid exception about missing class
        // definition of org.slf4j.helpers.MessageFormatter (it isn't exported by slf4j bundle)
        if (className.startsWith("org.slf4j")) {
            return;
        }

        LOGGER.trace("Weaving called for: {}", className);

        ClassData enhancedClassData = MotechClassPool.getEnhancedClassData(className);

        if (enhancedClassData == null) {
            LOGGER.trace("The class doesn't have enhanced metadata: {}", className);
        } else {
            LOGGER.info("Weaving {}", className);
            // these imports will be required by the provider
            addCommonImports(wovenClass);
            // add dynamic imports to enums and mds entities
            addDynamicImports(wovenClass);
            // change the bytecode
            wovenClass.setBytes(enhancedClassData.getBytecode());
        }
    }

    private void addDynamicImports(WovenClass wovenClass) {
        List<String> dynamicImports = wovenClass.getDynamicImports();
        List<String> packages = Arrays.asList(
                ClassName.getEnumPackage(wovenClass.getClassName()),
                Constants.PackagesGenerated.ENTITY);
        for (String pkg : packages) {
            if (!dynamicImports.contains(pkg)) {
                dynamicImports.add(pkg);
            }
        }
    }

    private void addCommonImports(WovenClass wovenClass) {
        List<String> dynamicImports = wovenClass.getDynamicImports();

        for (String sdi : STANDARD_DYNAMIC_IMPORTS) {
            if (!dynamicImports.contains(sdi)) {
                dynamicImports.add(sdi);
            }
        }
    }

}
