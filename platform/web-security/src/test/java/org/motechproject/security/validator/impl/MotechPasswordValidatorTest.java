package org.motechproject.security.validator.impl;

import org.junit.Test;
import org.motechproject.security.exception.PasswordValidatorException;

public class MotechPasswordValidatorTest {

    private MotechPasswordValidator validator = new MotechPasswordValidator(3, 1, 1 , 2, "test", null);

    @Test
    public void shouldValidatePasswords() {
        validator.validate("Aaft1$@");
        validator.validate("T44sss_$");
        validator.validate("Something 123&");
    }

    @Test(expected = PasswordValidatorException.class)
    public void shouldFailIfNotEnoughLowerCase() {
        validator.validate("A2tw*@");
    }

    @Test(expected = PasswordValidatorException.class)
    public void shouldFailIfNotEnoughUpperCase() {
        validator.validate("abw2*@");
    }

    @Test(expected = PasswordValidatorException.class)
    public void shouldFailIfNotEnoughDigits() {
        validator.validate("zzzaV*@");
    }

    @Test(expected = PasswordValidatorException.class)
    public void shouldFailIfNotEnoughSpecialChars() {
        validator.validate("sdfaV234%");
    }
}
