package org.motechproject.server.web.validator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;
import org.motechproject.config.core.domain.ConfigSource;
import org.motechproject.security.service.MotechUserService;
import org.motechproject.server.web.form.StartupForm;

import java.util.List;

import static org.motechproject.server.web.form.StartupForm.ADMIN_CONFIRM_PASSWORD;
import static org.motechproject.server.web.form.StartupForm.ADMIN_LOGIN;
import static org.motechproject.server.web.form.StartupForm.ADMIN_PASSWORD;

/**
 * Validates presence of admin registration fields.
 * Checks existence of user with identical name
 * Checks existence of user with identical email
 * Checks that password and confirmed password field are same.
 */
public class PersistedUserValidator implements AbstractValidator {

    private static final String ERROR_REQUIRED = "server.error.required.%s";
    private MotechUserService userService;

    public PersistedUserValidator(MotechUserService userService) {
        this.userService = userService;
    }

    @Override
    public void validate(StartupForm target, List<String> errors, ConfigSource configSource) {
        // only validate without active admin
        if (userService.hasActiveMotechAdmin()) {
            return;
        }

        if (StringUtils.isBlank(target.getAdminLogin())) {
            errors.add(String.format(ERROR_REQUIRED, ADMIN_LOGIN));
        } else if (userService.hasUser(target.getAdminLogin())) {
            errors.add("server.error.user.exist");
        } else if (userService.hasEmail(target.getAdminEmail())) {
            errors.add("server.error.email.exist");
        }

        if (StringUtils.isBlank(target.getAdminPassword())) {
            errors.add(String.format(ERROR_REQUIRED, ADMIN_PASSWORD));
        } else if (StringUtils.isBlank(target.getAdminConfirmPassword())) {
            errors.add(String.format(ERROR_REQUIRED, ADMIN_CONFIRM_PASSWORD));
        } else if (!target.getAdminPassword().equals(target.getAdminConfirmPassword())) {
            errors.add("server.error.invalid.password");
        }

        if (!EmailValidator.getInstance().isValid(target.getAdminEmail())) {
            errors.add("server.error.invalid.email");
        }
    }
}
