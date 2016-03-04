package org.motechproject.mds.domain;

import org.apache.commons.lang.StringUtils;
import org.motechproject.mds.dto.EntityDto;

import java.util.Arrays;

/**
 * Represents a class name and its byte code.
 */
public class ClassData {
    private final String className;
    private final String module;
    private final String namespace;
    private final byte[] bytecode;
    private final boolean interfaceClass;
    private final EntityType type;
    private final boolean enumClassData;

    public ClassData(String className, byte[] bytecode) {
        this(className, bytecode, false);
    }

    public ClassData(String className, byte[] bytecode, boolean interfaceClass) {
        this(className, null, null, bytecode, interfaceClass);
    }

    public ClassData(Entity entity, byte[] bytecode) {
        this(entity, bytecode, false);
    }

    public ClassData(EntityDto entity, byte[] bytecode) {
        this(entity.getClassName(), entity.getModule(), entity.getNamespace(), bytecode);
    }

    public ClassData(Entity entity, byte[] bytecode, boolean interfaceClass) {
        this(
                entity.getClassName(), entity.getModule(), entity.getNamespace(),
                bytecode, interfaceClass
        );
    }

    public ClassData(String className, String module, String namespace, byte[] bytecode) {
        this(className, module, namespace, bytecode, false);
    }

    public ClassData(String className, String module, String namespace, byte[] bytecode, EntityType type) {
        this(className, module, namespace, bytecode, false, type, false);
    }

    public ClassData(String className, String module, String namespace, byte[] bytecode,
                     boolean interfaceClass) {
        this(className, module, namespace, bytecode, interfaceClass, EntityType.STANDARD, false);
    }

    public ClassData(String className, String module, String namespace, byte[] bytecode,
                     boolean interfaceClass, EntityType type, boolean enumClassData) {
        this.className = className;
        this.module = module;
        this.namespace = namespace;
        this.bytecode = Arrays.copyOf(bytecode, bytecode.length);
        this.interfaceClass = interfaceClass;
        this.type = type;
        this.enumClassData = enumClassData;
    }

    public String getClassName() {
        return className;
    }

    public String getModule() {
        return module;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean isInterfaceClass() {
        return interfaceClass;
    }

    public boolean isDDE() {
        return StringUtils.isNotBlank(module);
    }

    public byte[] getBytecode() {
        return Arrays.copyOf(bytecode, bytecode.length);
    }

    public EntityType getType() {
        return type;
    }

    public boolean isEnumClassData() {
        return enumClassData;
    }

    @Override
    public String toString() {
        return String.format(
                "ClassData{className='%s', module='%s', namespace='%s', interfaceClass=%s}",
                className, module, namespace, interfaceClass
        );
    }
}
