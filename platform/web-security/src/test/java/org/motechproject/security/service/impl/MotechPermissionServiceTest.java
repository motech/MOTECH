package org.motechproject.security.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.motechproject.security.domain.MotechPermission;
import org.motechproject.security.model.PermissionDto;
import org.motechproject.security.mds.MotechPermissionsDataService;
import org.motechproject.security.service.MotechPermissionService;
import org.motechproject.security.service.MotechRoleService;
import org.motechproject.security.service.UserContextService;

import java.util.List;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MotechPermissionServiceTest {

    @InjectMocks
    private MotechPermissionService permissionService = new MotechPermissionServiceImpl();

    @Mock
    private MotechPermissionsDataService motechPermissionsDataService;

    @Mock
    private MotechRoleService motechRoleService;

    @Mock
    private UserContextService userContextsService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAddPermissions() {
        PermissionDto permissionDto = new PermissionDto("permName", "bundleName");

        permissionService.addPermission(permissionDto);

        ArgumentCaptor<MotechPermission> captor = ArgumentCaptor.forClass(MotechPermission.class);
        verify(motechPermissionsDataService).create(captor.capture());

        assertEquals("permName", captor.getValue().getPermissionName());
        assertEquals("bundleName", captor.getValue().getBundleName());
    }

    @Test
    public void shouldDeletePermissions() {
        MotechPermission permission = mock(MotechPermission.class);
        when(motechPermissionsDataService.findByPermissionName("permName")).thenReturn(permission);

        permissionService.deletePermission("permName");

        verify(motechPermissionsDataService).delete(permission);
    }

    @Test
    public void shouldReturnPermissions() {
        MotechPermission perm1 = new MotechPermission("perm1", "bundle1");
        MotechPermission perm2 = new MotechPermission("perm2", "bundle2");
        when(motechPermissionsDataService.retrieveAll()).thenReturn(asList(perm1, perm2));

        List<PermissionDto> result = permissionService.getPermissions();

        assertNotNull(result);
        assertEquals(asList("perm1", "perm2"), extract(result, on(PermissionDto.class).getPermissionName()));
        assertEquals(asList("bundle1", "bundle2"), extract(result, on(PermissionDto.class).getBundleName()));
    }

    @Test
    public void shouldRefreshUserContextWhenPermissionIsDeleted() {
        MotechPermission permission = mock(MotechPermission.class);
        when(motechPermissionsDataService.findByPermissionName("permName")).thenReturn(permission);

        permissionService.deletePermission("permName");

        verify(userContextsService).refreshAllUsersContextIfActive();
    }
}
