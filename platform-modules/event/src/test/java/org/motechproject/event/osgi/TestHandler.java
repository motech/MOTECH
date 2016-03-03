package org.motechproject.event.osgi;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.event.listener.annotations.MotechListenerType;
import org.motechproject.event.listener.annotations.MotechParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Component
public class TestHandler {


    public static final List<String> EVENTS_HANDLED = Collections.synchronizedList(new ArrayList<String>());
    public static final String TEST_SUBJECT = "test-subject";
    public static final String SUBJECT_READ = "read";
    public static final Properties PROPERTIES = new Properties();

    @MotechListener(subjects = {TEST_SUBJECT})
    public void handle(MotechEvent event) {
        add(event);
    }


    @MotechListener(subjects = {"sub_a", "sub_b"})
    public void handleX(MotechEvent event) {
        add(event);
    }

    @MotechListener(subjects = {"sub_a", "sub_c"})
    public void handleY(MotechEvent event) {
    }

    @MotechListener(subjects = {"named"}, type = MotechListenerType.NAMED_PARAMETERS)
    public void namedParams(@MotechParam("id") String id, @MotechParam("key") String key) {
    }

    @MotechListener(subjects = {SUBJECT_READ})
    public void read(MotechEvent event) throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("test.properties")) {
            PROPERTIES.load(inputStream);
        }
    }

    private void add(MotechEvent event) {
        EVENTS_HANDLED.add(event.getSubject());
    }

}
