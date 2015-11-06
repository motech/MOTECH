package org.motechproject.mds.builder.impl;

import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.builder.EntityMetadataBuilder;
import org.motechproject.mds.domain.ClassData;
import org.motechproject.mds.domain.ComboboxHolder;
import org.motechproject.mds.domain.Entity;
import org.motechproject.mds.domain.EntityType;
import org.motechproject.mds.domain.Field;
import org.motechproject.mds.domain.FieldSetting;
import org.motechproject.mds.domain.RelationshipHolder;
import org.motechproject.mds.domain.Type;
import org.motechproject.mds.helper.ClassTableName;
import org.motechproject.mds.javassist.MotechClassPool;
import org.motechproject.mds.reflections.ReflectionsUtil;
import org.motechproject.mds.repository.AllEntities;
import org.motechproject.mds.util.ClassName;
import org.motechproject.mds.util.Constants;
import org.motechproject.mds.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.ForeignKeyAction;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Value;
import javax.jdo.metadata.ClassMetadata;
import javax.jdo.metadata.ClassPersistenceModifier;
import javax.jdo.metadata.CollectionMetadata;
import javax.jdo.metadata.ColumnMetadata;
import javax.jdo.metadata.ElementMetadata;
import javax.jdo.metadata.FieldMetadata;
import javax.jdo.metadata.ForeignKeyMetadata;
import javax.jdo.metadata.InheritanceMetadata;
import javax.jdo.metadata.JDOMetadata;
import javax.jdo.metadata.JoinMetadata;
import javax.jdo.metadata.MapMetadata;
import javax.jdo.metadata.MemberMetadata;
import javax.jdo.metadata.PackageMetadata;
import javax.jdo.metadata.ValueMetadata;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.motechproject.mds.util.Constants.MetadataKeys.DATABASE_COLUMN_NAME;
import static org.motechproject.mds.util.Constants.MetadataKeys.MAP_KEY_TYPE;
import static org.motechproject.mds.util.Constants.MetadataKeys.MAP_VALUE_TYPE;
import static org.motechproject.mds.util.Constants.Util.CREATION_DATE_FIELD_NAME;
import static org.motechproject.mds.util.Constants.Util.CREATOR_FIELD_NAME;
import static org.motechproject.mds.util.Constants.Util.DATANUCLEUS;
import static org.motechproject.mds.util.Constants.Util.FALSE;
import static org.motechproject.mds.util.Constants.Util.ID_FIELD_NAME;
import static org.motechproject.mds.util.Constants.Util.MODIFICATION_DATE_FIELD_NAME;
import static org.motechproject.mds.util.Constants.Util.MODIFIED_BY_FIELD_NAME;
import static org.motechproject.mds.util.Constants.Util.OWNER_FIELD_NAME;
import static org.motechproject.mds.util.Constants.Util.TRUE;
import static org.motechproject.mds.util.Constants.Util.VALUE_GENERATOR;


/**
 * The <code>EntityMetadataBuilderImpl</code> class is responsible for building jdo metadata for an
 * entity class.
 */
@Component
public class EntityMetadataBuilderImpl implements EntityMetadataBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityMetadataBuilderImpl.class);

    private static final String[] FIELD_VALUE_GENERATOR = new String[]{
            CREATOR_FIELD_NAME, OWNER_FIELD_NAME, CREATION_DATE_FIELD_NAME,
            MODIFIED_BY_FIELD_NAME, MODIFICATION_DATE_FIELD_NAME
    };

    private static final String ID_SUFFIX = "_ID";

    private AllEntities allEntities;

    @Override
    public void addEntityMetadata(JDOMetadata jdoMetadata, Entity entity, Class<?> definition) {
        String className = (entity.isDDE()) ? entity.getClassName() : ClassName.getEntityName(entity.getClassName());
        String packageName = ClassName.getPackage(className);
        String tableName = ClassTableName.getTableName(entity.getClassName(), entity.getModule(), entity.getNamespace(), entity.getTableName(), null);

        PackageMetadata pmd = getPackageMetadata(jdoMetadata, packageName);
        ClassMetadata cmd = getClassMetadata(pmd, ClassName.getSimpleName(ClassName.getEntityName(entity.getClassName())));

        cmd.setTable(tableName);
        cmd.setDetachable(true);
        cmd.setIdentityType(IdentityType.APPLICATION);
        cmd.setPersistenceModifier(ClassPersistenceModifier.PERSISTENCE_CAPABLE);

        addInheritanceMetadata(cmd, definition);

        if (!entity.isSubClassOfMdsEntity()) {
            addIdField(cmd, entity);
        }

        addMetadataForFields(cmd, null, entity, EntityType.STANDARD, definition);
    }

    @Override
    public void addHelperClassMetadata(JDOMetadata jdoMetadata, ClassData classData, Entity entity,
                                       EntityType entityType, Class<?> definition) {
        String packageName = ClassName.getPackage(classData.getClassName());
        String simpleName = ClassName.getSimpleName(classData.getClassName());
        String tableName = ClassTableName.getTableName(classData.getClassName(), classData.getModule(), classData.getNamespace(),
                entity == null ? "" : entity.getTableName(), entityType);

        PackageMetadata pmd = getPackageMetadata(jdoMetadata, packageName);
        ClassMetadata cmd = getClassMetadata(pmd, simpleName);

        cmd.setTable(tableName);
        cmd.setDetachable(true);
        cmd.setIdentityType(IdentityType.APPLICATION);
        cmd.setPersistenceModifier(ClassPersistenceModifier.PERSISTENCE_CAPABLE);

        InheritanceMetadata imd = cmd.newInheritanceMetadata();
        imd.setCustomStrategy("complete-table");

        addIdField(cmd, classData.getClassName());

        if (entity != null) {
            addMetadataForFields(cmd, classData, entity, entityType, definition);
        }
    }

    @Override
    @Transactional
    public void fixEnhancerIssuesInMetadata(JDOMetadata jdoMetadata) {
        for (PackageMetadata pmd : jdoMetadata.getPackages()) {
            for (ClassMetadata cmd : pmd.getClasses()) {
                String className = String.format("%s.%s", pmd.getName(), cmd.getName());
                EntityType entityType = EntityType.forClassName(className);

                if (entityType == EntityType.STANDARD) {

                    Entity entity = allEntities.retrieveByClassName(className);

                    if (null != entity) {
                        for (MemberMetadata mmd : cmd.getMembers()) {
                            CollectionMetadata collMd = mmd.getCollectionMetadata();
                            Field field = entity.getField(mmd.getName());

                            if (null != field && field.getType().isRelationship()) {
                                fixRelationMetadata(pmd, field);
                            }

                            if (null != collMd) {
                                fixCollectionMetadata(collMd);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addBaseMetadata(JDOMetadata jdoMetadata, ClassData classData, EntityType entityType, Class<?> definition) {
        addHelperClassMetadata(jdoMetadata, classData, null, entityType, definition);
    }

    private void fixCollectionMetadata(CollectionMetadata collMd) {
        String elementType = collMd.getElementType();

        if (null != MotechClassPool.getEnhancedClassData(elementType)) {
            collMd.setEmbeddedElement(false);
        }
    }

    private void fixRelationMetadata(PackageMetadata pmd, Field field) {
        RelationshipHolder holder = new RelationshipHolder(field);

        //for bidirectional 1:1 and 1:N relationship we're letting RDBMS take care of cascade deletion
        //this must be set here cause we can't get related class metadata before metadata enhancement
        if (shouldSetCascadeDelete(holder, EntityType.STANDARD)) {

            String relatedClass = ClassName.getSimpleName(holder.getRelatedClass());
            MemberMetadata rfmd = getFieldMetadata(getClassMetadata(pmd, relatedClass), holder.getRelatedField());

            ForeignKeyMetadata rfkmd = rfmd.newForeignKeyMetadata();
            rfkmd.setDeleteAction(ForeignKeyAction.CASCADE);
        }
    }

    private void addInheritanceMetadata(ClassMetadata cmd, Class<?> definition) {
        Class<Inheritance> ann = ReflectionsUtil.getAnnotationClass(definition, Inheritance.class);
        Inheritance annotation = AnnotationUtils.findAnnotation(definition, ann);

        if (annotation == null) {
            InheritanceMetadata imd = cmd.newInheritanceMetadata();
            imd.setCustomStrategy("complete-table");
        }
    }

    private void addDefaultFetchGroupMetadata(FieldMetadata fmd, Class<?> definition) {
        java.lang.reflect.Field field = FieldUtils.getField(definition, fmd.getName(), true);

        if (field == null) {
            LOGGER.warn("Unable to retrieve field {} from class {}. Putting the field in the default fetch group by default.",
                    fmd.getName(), definition.getName());
            fmd.setDefaultFetchGroup(true);
        } else {
            Persistent persistentAnnotation = ReflectionsUtil.getAnnotationSelfOrAccessor(field, Persistent.class);

            // set to true, unless there is a JDO annotation that specifies otherwise
            if (persistentAnnotation == null || StringUtils.isBlank(persistentAnnotation.defaultFetchGroup())) {
                fmd.setDefaultFetchGroup(true);
            }
        }
    }

    private void addMetadataForFields(ClassMetadata cmd, ClassData classData, Entity entity, EntityType entityType,
                                      Class<?> definition) {
        for (Field field : entity.getFields()) {
            String fieldName = getNameForMetadata(field);

            // Metadata for ID field has been added earlier in addIdField() method
            if (!fieldName.equals(ID_FIELD_NAME)) {
                FieldMetadata fmd = null;

                if (isFieldNotInherited(fieldName, entity)) {
                    fmd = setFieldMetadata(cmd, classData, entity, entityType, field, definition);
                }
                // when field is in Lookup, we set field metadata indexed to retrieve instance faster
                if (!field.getLookups().isEmpty() && entityType.equals(EntityType.STANDARD)) {
                    if (fmd == null) {
                        String inheritedFieldName = ClassName.getSimpleName(entity.getSuperClass()) + "." + fieldName;
                        fmd = cmd.newFieldMetadata(inheritedFieldName);
                    }
                    fmd.setIndexed(true);
                }
                if (fmd != null) {
                    setColumnParameters(fmd, field, definition);
                    // Check whether the field is required and set appropriate metadata
                    fmd.setNullValue(isFieldRequired(field, entityType) ? NullValue.EXCEPTION : NullValue.NONE);
                }
            }
        }
    }

    private boolean isFieldRequired(Field field, EntityType entityType) {
        return field.isRequired() && !(entityType.equals(EntityType.TRASH) && field.getType().isRelationship());
    }

    private boolean isFieldNotInherited(String fieldName, Entity entity) {
        if (entity.isSubClassOfMdsEntity() && (ArrayUtils.contains(FIELD_VALUE_GENERATOR, fieldName))) {
            return false;
        } else {
            // return false if it is inherited field from superclass
            return entity.isBaseEntity() || !isFieldFromSuperClass(entity.getSuperClass(), fieldName);
        }
    }

    private boolean isFieldFromSuperClass(String className, String fieldName) {
        Entity entity = allEntities.retrieveByClassName(className);
        return entity.getField(fieldName) != null;
    }

    private FieldMetadata setFieldMetadata(ClassMetadata cmd, ClassData classData, Entity entity,
                                           EntityType entityType, Field field, Class<?> definition) {
        String name = getNameForMetadata(field);

        Type type = field.getType();
        Class<?> typeClass = type.getTypeClass();

        if (ArrayUtils.contains(FIELD_VALUE_GENERATOR, name)) {
            return setAutoGenerationMetadata(cmd, name);
        } else if (type.isCombobox()) {
            return setComboboxMetadata(cmd, entity, field, definition);
        } else if (type.isRelationship()) {
            return setRelationshipMetadata(cmd, classData, field, entityType, definition);
        } else if (Map.class.isAssignableFrom(typeClass)) {
            return setMapMetadata(cmd, field, definition);
        } else if (Time.class.isAssignableFrom(typeClass)) {
            return setTimeMetadata(cmd, name);
        }
        return cmd.newFieldMetadata(name);
    }

    private MemberMetadata getFieldMetadata(ClassMetadata cmd, String relatedField) {

        MemberMetadata fmd = null;

        for (MemberMetadata field : cmd.getMembers()) {
            if (field.getName().equals(relatedField)) {
                fmd = field;
                break;
            }
        }

        return fmd;
    }

    private void setColumnParameters(FieldMetadata fmd, Field field, Class<?> definition) {
        Value valueAnnotation = null;
        java.lang.reflect.Field fieldDefinition = FieldUtils.getDeclaredField(definition, field.getName(), true);
        //@Value in datanucleus is used with maps.
        if (fieldDefinition != null && java.util.Map.class.isAssignableFrom(field.getType().getTypeClass())) {
            valueAnnotation = ReflectionsUtil.getAnnotationSelfOrAccessor(fieldDefinition, Value.class);
        }

        if ((field.getMetadata(DATABASE_COLUMN_NAME) != null || field.getSettingByName(Constants.Settings.STRING_MAX_LENGTH) != null
                || field.getSettingByName(Constants.Settings.STRING_TEXT_AREA) != null) || (valueAnnotation != null)) {
            addColumnMetadata(fmd, field, valueAnnotation);
        }
    }

    private void addColumnMetadata(FieldMetadata fmd, Field field, Value valueAnnotation) {
        FieldSetting maxLengthSetting = field.getSettingByName(Constants.Settings.STRING_MAX_LENGTH);
        ColumnMetadata colMd = fmd.newColumnMetadata();
        // only set the metadata if the setting is different from default
        if (maxLengthSetting != null && !StringUtils.equals(maxLengthSetting.getValue(),
                maxLengthSetting.getDetails().getDefaultValue())) {
            colMd.setLength(Integer.parseInt(maxLengthSetting.getValue()));
        }

        // if TextArea then change length
        if (field.getSettingByName(Constants.Settings.STRING_TEXT_AREA) != null &&
                "true".equalsIgnoreCase(field.getSettingByName(Constants.Settings.STRING_TEXT_AREA).getValue())) {
            fmd.setIndexed(false);
            colMd.setSQLType("CLOB");
        }
        if (field.getMetadata(DATABASE_COLUMN_NAME) != null) {
            colMd.setName(field.getMetadata(DATABASE_COLUMN_NAME).getValue());
        }

        if (valueAnnotation != null) {
            copyParametersFromValueAnnotation(fmd, valueAnnotation);
        }
    }

    private void copyParametersFromValueAnnotation(FieldMetadata fmd, Value valueAnnotation) {
        ValueMetadata valueMetadata = fmd.newValueMetadata();
        for (Column column : valueAnnotation.columns()) {
            ColumnMetadata colMd = valueMetadata.newColumnMetadata();
            colMd.setName(column.name());
            colMd.setLength(column.length());
            colMd.setAllowsNull(Boolean.parseBoolean(column.allowsNull()));
            colMd.setDefaultValue(column.defaultValue());
            colMd.setInsertValue(column.insertValue());
            colMd.setJDBCType(column.jdbcType());
            colMd.setSQLType(column.sqlType());
        }
    }

    private FieldMetadata setTimeMetadata(ClassMetadata cmd, String name) {
        // for time we register our converter which persists as string
        FieldMetadata fmd = cmd.newFieldMetadata(name);

        fmd.setPersistenceModifier(PersistenceModifier.PERSISTENT);
        fmd.setDefaultFetchGroup(true);
        fmd.newExtensionMetadata(DATANUCLEUS, "type-converter-name", "dn.time-string");
        return fmd;
    }

    private FieldMetadata setMapMetadata(ClassMetadata cmd, Field field, Class<?> definition) {
        FieldMetadata fmd = cmd.newFieldMetadata(getNameForMetadata(field));

        org.motechproject.mds.domain.FieldMetadata keyMetadata = field.getMetadata(MAP_KEY_TYPE);
        org.motechproject.mds.domain.FieldMetadata valueMetadata = field.getMetadata(MAP_VALUE_TYPE);

        boolean serialized = shouldSerializeMap(keyMetadata, valueMetadata);

        // Depending on the types of key and value of the map we either serialize the map or create a separate table for it
        fmd.setSerialized(serialized);

        addDefaultFetchGroupMetadata(fmd, definition);

        MapMetadata mmd = fmd.newMapMetadata();

        if (serialized) {
            mmd.setSerializedKey(true);
            mmd.setSerializedValue(true);
        } else {
            mmd.setKeyType(keyMetadata.getValue());
            mmd.setValueType(valueMetadata.getValue());

            fmd.setTable(ClassTableName.getTableName(cmd.getTable(), getNameForMetadata(field)));
            JoinMetadata jmd = fmd.newJoinMetadata();
            ForeignKeyMetadata fkmd = jmd.newForeignKeyMetadata();
            fkmd.setDeleteAction(ForeignKeyAction.CASCADE);
        }
        return fmd;
    }

    private boolean shouldSerializeMap(org.motechproject.mds.domain.FieldMetadata keyMetadata,
                                       org.motechproject.mds.domain.FieldMetadata valueMetadata) {
        // If generics types of map are not supported in MDS, we serialized the field in DB.
        return keyMetadata == null || valueMetadata == null ||
                ! (TypeHelper.isTypeSupportedInMap(keyMetadata.getValue(), true) &&
                        TypeHelper.isTypeSupportedInMap(valueMetadata.getValue(), false));
    }

    private FieldMetadata setRelationshipMetadata(ClassMetadata cmd, ClassData classData, Field field,
                                         EntityType entityType, Class<?> definition) {

        RelationshipHolder holder = new RelationshipHolder(classData, field);
        FieldMetadata fmd = cmd.newFieldMetadata(getNameForMetadata(field));

        addDefaultFetchGroupMetadata(fmd, definition);

        if (entityType == EntityType.STANDARD) {
            processRelationship(fmd, holder, field, definition);
        } else {
            processHistoryTrashRelationship(cmd, fmd, holder);
        }

        return fmd;
    }

    private void processRelationship(FieldMetadata fmd, RelationshipHolder holder, Field field, Class<?> definition) {
        String relatedClass = holder.getRelatedClass();

        fmd.newExtensionMetadata(DATANUCLEUS, "cascade-persist", holder.isCascadePersist() ? TRUE : FALSE);
        fmd.newExtensionMetadata(DATANUCLEUS, "cascade-update", holder.isCascadeUpdate() ? TRUE : FALSE);

        if (holder.isOneToMany() || holder.isManyToMany()) {
            setUpCollectionMetadata(fmd, relatedClass, holder, EntityType.STANDARD);
        } else if (holder.isOneToOne()) {
            fmd.setPersistenceModifier(PersistenceModifier.PERSISTENT);

            //for bidirectional 1:1 we're setting foreign key with cascade deletion after metadata enhancement
            if (holder.getRelatedField() == null) {
                fmd.setDependent(holder.isCascadeDelete());
            }
        }

        if (holder.isManyToMany()) {
            addManyToManyMetadata(fmd, holder, field, definition);
        }
    }

    private void processHistoryTrashRelationship(ClassMetadata cmd, FieldMetadata fmd, RelationshipHolder holder) {
        if (holder.isOneToOne() || holder.isManyToOne()) {
            fmd.setColumn(holder.getFieldName() + ID_SUFFIX);
        } else {
            fmd.setTable(cmd.getTable() + '_' + holder.getFieldName());

            CollectionMetadata collMd = fmd.newCollectionMetadata();
            collMd.setElementType(Long.class.getName());

            JoinMetadata joinMd = fmd.newJoinMetadata();
            ColumnMetadata joinColumnMd = joinMd.newColumnMetadata();
            joinColumnMd.setName(cmd.getName() + ID_SUFFIX);

            ElementMetadata elementMd = fmd.newElementMetadata();
            elementMd.setColumn(holder.getFieldName() + ID_SUFFIX);
        }
    }

    private void addManyToManyMetadata(FieldMetadata fmd, RelationshipHolder holder, Field field, Class<?> definition) {
        java.lang.reflect.Field fieldDefinition = FieldUtils.getDeclaredField(definition, field.getName(), true);
        Join join = fieldDefinition.getAnnotation(Join.class);

        JoinMetadata jmd = null;
        // Join metadata must be present at both sides of the M:N relation in Datanucleus 3.2
        if (join == null) {
            jmd = fmd.newJoinMetadata();
            jmd.setOuter(false);
        }

        // If tables and column names have been specified in annotations, do not set their metadata
        if (!holder.isOwningSide()) {
            Persistent persistent = fieldDefinition.getAnnotation(Persistent.class);
            Element element = fieldDefinition.getAnnotation(Element.class);

            setTableNameMetadata(fmd, persistent, field, holder, EntityType.STANDARD);
            setElementMetadata(fmd, element, holder, EntityType.STANDARD);

            if (join == null || StringUtils.isEmpty(join.column())) {
                setJoinMetadata(jmd, fmd, ClassName.getSimpleName(field.getEntity().getClassName()).toUpperCase() + ID_SUFFIX);
           }
        }
    }

    private void setElementMetadata(FieldMetadata fmd, Element element, RelationshipHolder holder, EntityType entityType) {
        if (element != null && StringUtils.isNotEmpty(element.column()) && entityType != EntityType.STANDARD) {
            ElementMetadata emd = fmd.newElementMetadata();
            emd.setColumn(element.column());
        } else if (element == null || StringUtils.isEmpty(element.column())) {
            ElementMetadata emd = fmd.newElementMetadata();
            emd.setColumn((ClassName.getSimpleName(holder.getRelatedClass()) + ID_SUFFIX).toUpperCase());
        }
    }

    private void setJoinMetadata(JoinMetadata jmd, FieldMetadata fmd, String column) {
        JoinMetadata joinMetadata;
        if (jmd == null) {
            joinMetadata = fmd.newJoinMetadata();
            joinMetadata.setOuter(false);
        } else {
            joinMetadata = jmd;
        }

        joinMetadata.setColumn(column);
    }

    private void setTableNameMetadata(FieldMetadata fmd, Persistent persistent, Field field, RelationshipHolder holder, EntityType entityType) {
        if (persistent != null && StringUtils.isNotEmpty(persistent.table()) && entityType != EntityType.STANDARD) {
            fmd.setTable(entityType.getTableName(persistent.table()));
        } else if (persistent == null || StringUtils.isEmpty(persistent.table())) {
            fmd.setTable(getJoinTableName(field.getEntity().getModule(), field.getEntity().getNamespace(), field.getName(), holder.getRelatedField()));
        }
    }

    private void setUpCollectionMetadata(FieldMetadata fmd, String relatedClass, RelationshipHolder holder, EntityType entityType) {
        CollectionMetadata colMd = getOrCreateCollectionMetadata(fmd);
        colMd.setElementType(relatedClass);
        colMd.setEmbeddedElement(false);
        colMd.setSerializedElement(false);

        //for 1:N we're setting foreign key with cascade deletion after metadata enhancement
        if (holder.isManyToMany()) {
            colMd.setDependentElement(holder.isCascadeDelete() || entityType == EntityType.TRASH);
        }

        if (holder.isManyToMany() && !holder.isOwningSide() && entityType.equals(EntityType.STANDARD)) {
            fmd.setMappedBy(holder.getRelatedField());
        }
    }

    private FieldMetadata setComboboxMetadata(ClassMetadata cmd, Entity entity, Field field, Class<?> definition) {
        ComboboxHolder holder = new ComboboxHolder(entity, field);
        String fieldName = getNameForMetadata(field);
        FieldMetadata fmd = cmd.newFieldMetadata(fieldName);

        if (holder.isCollection()) {
            addDefaultFetchGroupMetadata(fmd, definition);

            fmd.setTable(ClassTableName.getTableName(cmd.getTable(), fieldName));

            JoinMetadata jm = fmd.newJoinMetadata();
            jm.newForeignKeyMetadata();
            jm.setDeleteAction(ForeignKeyAction.CASCADE);
            jm.setColumn(fieldName + "_OID");
        }
        return fmd;
    }

    private FieldMetadata setAutoGenerationMetadata(ClassMetadata cmd, String name) {
        FieldMetadata fmd = cmd.newFieldMetadata(name);
        fmd.setPersistenceModifier(PersistenceModifier.PERSISTENT);
        fmd.setDefaultFetchGroup(true);
        fmd.newExtensionMetadata(DATANUCLEUS, VALUE_GENERATOR, "ovg." + name);
        return fmd;
    }

    private static ClassMetadata getClassMetadata(PackageMetadata pmd, String className) {
        ClassMetadata[] classes = pmd.getClasses();
        if (ArrayUtils.isNotEmpty(classes)) {
            for (ClassMetadata cmd : classes) {
                if (StringUtils.equals(className, cmd.getName())) {
                    return cmd;
                }
            }
        }
        return pmd.newClassMetadata(className);
    }

    private static PackageMetadata getPackageMetadata(JDOMetadata jdoMetadata, String packageName) {
        // first look for existing metadata
        PackageMetadata[] packages = jdoMetadata.getPackages();
        if (ArrayUtils.isNotEmpty(packages)) {
            for (PackageMetadata pkgMetadata : packages) {
                if (StringUtils.equals(pkgMetadata.getName(), packageName)) {
                    return pkgMetadata;
                }
            }
        }
        // if not found, create new
        return jdoMetadata.newPackageMetadata(packageName);
    }

    private void addIdField(ClassMetadata cmd, Entity entity) {
        boolean containsID = null != entity.getField(ID_FIELD_NAME);
        boolean isBaseClass = entity.isBaseEntity();

        if (containsID && isBaseClass) {
            FieldMetadata metadata = cmd.newFieldMetadata(ID_FIELD_NAME);
            metadata.setValueStrategy(IdGeneratorStrategy.INCREMENT);
            metadata.setPrimaryKey(true);
            metadata.setIndexed(true);
        }
    }

    private void addIdField(ClassMetadata cmd, String className) {
        boolean containsID;
        boolean isBaseClass;

        try {
            CtClass ctClass = MotechClassPool.getDefault().getOrNull(className);
            containsID = null != ctClass && null != ctClass.getField(ID_FIELD_NAME);
            isBaseClass = null != ctClass && (null == ctClass.getSuperclass() || Object.class.getName().equalsIgnoreCase(ctClass.getSuperclass().getName()));
        } catch (NotFoundException e) {
            containsID = false;
            isBaseClass = false;
        }

        if (containsID && isBaseClass) {
            FieldMetadata metadata = cmd.newFieldMetadata(ID_FIELD_NAME);
            metadata.setValueStrategy(IdGeneratorStrategy.INCREMENT);
            metadata.setPrimaryKey(true);
            metadata.setIndexed(true);
        }
    }

    private CollectionMetadata getOrCreateCollectionMetadata(FieldMetadata fmd) {
        CollectionMetadata collMd = fmd.getCollectionMetadata();
        if (collMd == null) {
            collMd = fmd.newCollectionMetadata();
        }
        return collMd;
    }

    private String getJoinTableName(String module, String namespace, String owningSideName, String inversedSideNameWithSuffix) {
        String mod = defaultIfBlank(module, "MDS");

        StringBuilder builder = new StringBuilder();
        builder.append(mod).append("_");

        if (isNotBlank(namespace)) {
            builder.append(namespace).append("_");
        }

        builder.append("Join_").
                append(inversedSideNameWithSuffix).append("_").
                append(owningSideName).
                append(ClassName.getEntityTypeSuffix(inversedSideNameWithSuffix));

        return builder.toString().replace('-', '_').replace(' ', '_').toUpperCase();
    }

    private boolean shouldSetCascadeDelete(RelationshipHolder holder, EntityType entityType) {

        if (holder.isCascadeDelete() || entityType == EntityType.TRASH) {
            return (holder.isOneToOne() || holder.isOneToMany()) && ( holder.getRelatedField() != null);
        }

        return false;
    }

    private String getNameForMetadata(Field field) {
        return StringUtils.uncapitalize(field.getName());
    }

    @Autowired
    public void setAllEntities(AllEntities allEntities) {
        this.allEntities = allEntities;
    }
}
