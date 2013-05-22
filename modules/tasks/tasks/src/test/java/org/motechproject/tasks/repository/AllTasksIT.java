package org.motechproject.tasks.repository;

import org.ektorp.CouchDbConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.tasks.domain.Task;
import org.motechproject.tasks.domain.TaskActionInformation;
import org.motechproject.tasks.domain.TaskEventInformation;
import org.motechproject.testing.utils.SpringIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/motech/*.xml"})
public class AllTasksIT extends SpringIntegrationTest {

    @Autowired
    private AllTasks allTasks;

    @Autowired
    @Qualifier("taskDbConnector")
    private CouchDbConnector couchDbConnector;

    @Test
    public void shouldAddAndUpdateTask() {
        TaskActionInformation action = new TaskActionInformation("send", "test", "test", "0.15", "SEND", new HashMap<String, String>());
        TaskEventInformation trigger = new TaskEventInformation("receive", "test", "test", "0.14", "RECEIVE");
        Task expected = new Task("name", trigger, asList(action));

        allTasks.addOrUpdate(expected);

        List<Task> tasks = allTasks.getAll();

        assertEquals(asList(expected), tasks);

        Task actual = tasks.get(0);

        actual.setName("newName");

        allTasks.addOrUpdate(actual);

        tasks = allTasks.getAll();

        assertEquals(asList(actual), tasks);

        markForDeletion(allTasks.getAll());
    }

    @Override
    public CouchDbConnector getDBConnector() {
        return couchDbConnector;
    }

}
