package org.motechproject.mds.it.reposistory;

import org.junit.After;
import org.junit.Test;
import org.motechproject.mds.domain.Entity;
import org.motechproject.mds.domain.EntityDraft;
import org.motechproject.mds.dto.EntityDto;
import org.motechproject.mds.it.BaseIT;
import org.motechproject.mds.repository.internal.AllEntities;
import org.motechproject.mds.repository.internal.AllEntityDrafts;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jdo.JDOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AllEntityDraftsContextIT extends BaseIT {

    private static final String USERNAME = "username";
    private static final String USERNAME_2 = USERNAME + "2";

    @Autowired
    private AllEntities allEntities;

    @Autowired
    private AllEntityDrafts allEntityDrafts;

    @After
    public void newTx() {
        // PostgreSQL won't allow any further queries in an existing transaction
        // when an exception is thrown
        getPersistenceManager().currentTransaction().rollback();
        getPersistenceManager().currentTransaction().begin();
    }

    @Test
    public void shouldCreateAndDeleteDrafts() {
        EntityDto dto = new EntityDto();
        dto.setClassName("DraftCls");

        Entity entity = allEntities.create(dto);

        allEntityDrafts.create(entity, USERNAME);
        allEntityDrafts.create(entity, USERNAME_2);

        EntityDraft draft = allEntityDrafts.retrieve(entity, USERNAME);

        assertNotNull(draft);
        assertEquals("DraftCls", draft.getClassName());
        assertEquals(USERNAME, draft.getDraftOwnerUsername());

        draft = allEntityDrafts.retrieve(entity, USERNAME_2);

        assertNotNull(draft);
        assertEquals("DraftCls", draft.getClassName());
        assertEquals(USERNAME_2, draft.getDraftOwnerUsername());

        assertNull(allEntityDrafts.retrieve(entity, "otherUser"));

        allEntityDrafts.deleteAll(entity);

        assertTrue(allEntityDrafts.retrieveAll(entity).isEmpty());
        assertNull(allEntityDrafts.retrieve(entity, USERNAME));
        assertNull(allEntityDrafts.retrieve(entity, USERNAME_2));
    }

    @Test(expected = JDOException.class)
    public void shouldNotAllowTwoDraftsOfTheSameEntityForOneUser() {
        EntityDto dto = new EntityDto();
        dto.setClassName("DraftCls2");

        Entity entity = allEntities.create(dto);

        allEntityDrafts.create(entity, USERNAME);
        allEntityDrafts.create(entity, USERNAME);
    }
}
