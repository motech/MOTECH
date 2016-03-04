package org.motechproject.mds.annotations.internal;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.motechproject.mds.annotations.internal.samples.AnotherSample;
import org.motechproject.mds.annotations.internal.samples.RelatedSample;
import org.motechproject.mds.annotations.internal.samples.Sample;
import org.motechproject.mds.dto.SchemaHolder;
import org.motechproject.mds.dto.TrackingDto;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrudEventsProcessorTest {

    @Spy
    private MockBundle bundle = new MockBundle();

    @Mock
    private SchemaHolder schemaHolder;

    @Mock
    private TrackingDto initialTrackingDto;

    private CrudEventsProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new CrudEventsProcessor();
    }

    @Test
    public void shouldSetEntityCrudEvents() {
        when(initialTrackingDto.isModifiedByUser()).thenReturn(false);

        processor.setClazz(Sample.class);
        processor.setTrackingDto(initialTrackingDto);
        processor.execute(bundle, schemaHolder);

        verify(initialTrackingDto, atLeastOnce()).setAllEvents(false);
        verify(initialTrackingDto).setAllowCreateEvent(true);
    }

    @Test
    public void shouldSetAllEntityCrudEventsByDefault() {
        when(initialTrackingDto.isModifiedByUser()).thenReturn(false);

        processor.setClazz(RelatedSample.class);
        processor.setTrackingDto(initialTrackingDto);
        processor.execute(bundle, schemaHolder);

        verify(initialTrackingDto).setAllEvents(true);
    }

    @Test
    public void shouldNotSetCrudEventsForNoneValue() {
        when(initialTrackingDto.isModifiedByUser()).thenReturn(false);

        processor.setClazz(AnotherSample.class);
        processor.setTrackingDto(initialTrackingDto);
        processor.execute(bundle, schemaHolder);

        verify(initialTrackingDto, times(2)).setAllEvents(false);
    }
}
