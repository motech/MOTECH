package org.motechproject.server.web.validator;

import org.junit.Test;
import org.motechproject.config.core.domain.ConfigSource;
import org.motechproject.server.web.form.StartupForm;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class UserRegistrationValidatorTest {

    @Test
    public void shouldValidateUserDetailsWithOpenIdValidatorWhenLoginModeIsOpenId() {
        PersistedUserValidator persistedUserValidator = mock(PersistedUserValidator.class);
        OpenIdUserValidator openIdUserValidator = mock(OpenIdUserValidator.class);

        StartupForm target = new StartupForm();
        target.setLoginMode("openId");

        UserRegistrationValidator userRegistrationValidator = new UserRegistrationValidator(persistedUserValidator, openIdUserValidator);
        userRegistrationValidator.validate(target, new ArrayList<String>(), ConfigSource.FILE);

        verify(openIdUserValidator).validate(target, new ArrayList<String>(), ConfigSource.FILE);
        verify(persistedUserValidator, never()).validate(target, new ArrayList<String>(), ConfigSource.FILE);
    }

    @Test
    public void shouldValidateUserDetailsWithPersistentUserWhenLoginModeIsRepository() {
        PersistedUserValidator persistedUserValidator = mock(PersistedUserValidator.class);
        OpenIdUserValidator openIdUserValidator = mock(OpenIdUserValidator.class);

        StartupForm target = new StartupForm();
        target.setLoginMode("repository");

        UserRegistrationValidator userRegistrationValidator = new UserRegistrationValidator(persistedUserValidator, openIdUserValidator);
        userRegistrationValidator.validate(target, new ArrayList<String>(), ConfigSource.FILE);

        verify(persistedUserValidator).validate(target, new ArrayList<String>(), ConfigSource.FILE);
        verify(openIdUserValidator, never()).validate(target, new ArrayList<String>(), ConfigSource.FILE);
    }

    @Test
    public void shouldNotValidateUserDetailsWhenLoginModeIsNull() {
        PersistedUserValidator persistedUserValidator = mock(PersistedUserValidator.class);
        OpenIdUserValidator openIdUserValidator = mock(OpenIdUserValidator.class);

        StartupForm target = new StartupForm();
        target.setLoginMode(null);

        UserRegistrationValidator userRegistrationValidator = new UserRegistrationValidator(persistedUserValidator, openIdUserValidator);
        userRegistrationValidator.validate(target, new ArrayList<String>(), ConfigSource.FILE);

        verify(persistedUserValidator, never()).validate(target, new ArrayList<String>(), ConfigSource.FILE);
        verify(openIdUserValidator, never()).validate(target, new ArrayList<String>(), ConfigSource.FILE);
    }
}
