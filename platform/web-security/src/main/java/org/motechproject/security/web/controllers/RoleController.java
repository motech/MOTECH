package org.motechproject.security.web.controllers;

import org.motechproject.security.constants.WebSecurityRoles;
import org.motechproject.security.exception.RoleHasUserException;
import org.motechproject.security.model.RoleDto;
import org.motechproject.security.service.MotechRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Handle HTTP requests from web clients for Manage Roles user interface.
 */
@Controller
@RequestMapping("/web-api")
public class RoleController {
    private MotechRoleService motechRoleService;

    /**
     * Gets list of roles as long as user has role that allows it
     * @return list of roles
     */
    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    @ResponseBody
    public List<RoleDto> getRoles() {
        return motechRoleService.getRoles();
    }

    /**
     * Gets role with given name
     *
     * @param roleName name of role
     * @return role with given name
     */
    @PreAuthorize(WebSecurityRoles.HAS_MANAGE_ROLE_AND_PERMISSION)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/roles/role/{roleName}")
    @ResponseBody
    public RoleDto role(@PathVariable String roleName) {
        return motechRoleService.getRole(roleName);
    }

    /**
     * Updates given role
     *
     * @param role role to be updated
     */
    @PreAuthorize(WebSecurityRoles.HAS_MANAGE_ROLE_AND_PERMISSION)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/roles/update", method = RequestMethod.POST)
    public void updateRole(@RequestBody RoleDto role) {
        motechRoleService.updateRole(role);
    }

    /**
     * Deletes given role
     *
     * @param role role to be removed
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/roles/delete", method = RequestMethod.POST)
    public void deleteRole(@RequestBody RoleDto role) {
        motechRoleService.deleteRole(role);
    }

    /**
     * Creates given role
     *
     * @param role role to be created
     */
    @PreAuthorize(WebSecurityRoles.HAS_MANAGE_ROLE_AND_PERMISSION)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/roles/create", method = RequestMethod.POST)
    public void saveRole(@RequestBody RoleDto role) {
        motechRoleService.createRole(role);
    }

    @ExceptionHandler(RoleHasUserException.class)
    public void handleRoleHasUserException(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_CONFLICT);

        try (Writer writer = response.getWriter()) {
            writer.write("key:security.roleHasUserException");
        }
    }

    @Autowired
    public void setMotechRoleService(MotechRoleService motechRoleService) {
        this.motechRoleService = motechRoleService;
    }
}
