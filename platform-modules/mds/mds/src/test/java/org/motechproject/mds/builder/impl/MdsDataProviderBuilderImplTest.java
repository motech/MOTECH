package org.motechproject.mds.builder.impl;

import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.motechproject.mds.dto.AdvancedSettingsDto;
import org.motechproject.mds.dto.EntityDto;
import org.motechproject.mds.dto.FieldBasicDto;
import org.motechproject.mds.dto.FieldDto;
import org.motechproject.mds.dto.LookupDto;
import org.motechproject.mds.dto.LookupFieldDto;
import org.motechproject.mds.dto.SchemaHolder;
import org.motechproject.mds.testutil.FieldTestHelper;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MdsDataProviderBuilderImplTest {

    private MDSDataProviderBuilderImpl mdsDataProviderBuilder = new MDSDataProviderBuilderImpl();

    private List<LookupDto> lookupList = new LinkedList<>();
    private List<FieldDto> fieldList = new LinkedList<>();

    private VelocityEngine velocityEngine = new VelocityEngine();

    @Before
    public void setUp() {
        velocityEngine.addProperty("resource.loader", "classpath");
        velocityEngine.addProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        mdsDataProviderBuilder.setVelocityEngine(velocityEngine);
    }

    @Test
    public void shouldGenerateEmptyJson() {
        String generatedJson = mdsDataProviderBuilder.generateDataProvider(new SchemaHolder());
        assertEquals("", generatedJson);
    }

    @Test
    public void shouldGenerateJson() {
        String json = "{\n" +
                "    \"name\": \"data-services\",\n" +
                "    \"objects\": [         {\n" +
                "            \"displayName\": \"TestEntity\",\n" +
                "            \"type\": \"org.motechproject.TestEntity\",\n" +
                "            \"lookupFields\": [                 {\n" +
                "                    \"displayName\": \"TestLookupName\",\n" +
                "                    \"fields\": [\n" +
                "                         \"TestFieldName\"                      ]\n" +
                "                },                {\n" +
                "                    \"displayName\": \"mds.dataprovider.byinstanceid\",\n" +
                "                    \"fields\": [\n" +
                "                        \"mds.dataprovider.instanceid\"\n" +
                "                    ]\n" +
                "                }\n" +
                "            ],\n" +
                "            \"fields\": [\n" +
                "                                {\n" +
                "                    \"displayName\": \"TestFieldDisplayName\",\n" +
                "                    \"fieldKey\": \"TestFieldName\"\n" +
                "                }              ]\n" +
                "        }      ]\n" +
                "}\n";

        EntityDto entity = new EntityDto();
        entity.setId(Long.valueOf("1"));
        entity.setName("TestEntity");
        entity.setClassName("org.motechproject.TestEntity");

        FieldDto field = new FieldDto();
        FieldBasicDto fieldBasicDto = new FieldBasicDto();
        fieldBasicDto.setName("TestFieldName");
        fieldBasicDto.setDisplayName("TestFieldDisplayName");
        field.setBasic(fieldBasicDto);
        fieldList.add(field);

        LookupDto lookup = new LookupDto();
        lookup.setLookupName("TestLookupName");
        List<LookupFieldDto> lookupFields = new LinkedList<>();
        lookupFields.add(FieldTestHelper.lookupFieldDto("TestFieldName"));
        lookup.setLookupFields(lookupFields);
        lookupList.add(lookup);

        AdvancedSettingsDto advancedSettings = new AdvancedSettingsDto();
        advancedSettings.setIndexes(lookupList);

        SchemaHolder schema = new SchemaHolder();
        schema.addEntity(entity, advancedSettings, fieldList);

        String generatedJson = mdsDataProviderBuilder.generateDataProvider(schema);

        assertEquals(json, generatedJson.replace("\r\n", "\n"));
    }
}
