package org.motechproject.security.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Access;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.util.SecurityMode;
import org.motechproject.security.constants.PermissionNames;

import javax.jdo.annotations.Unique;
import java.util.Locale;

/**
 * Entity that holds data used for password recovery
 */
@Entity
@Access(value = SecurityMode.PERMISSIONS, members = {PermissionNames.MANAGE_USER_PERMISSION})
public class PasswordRecovery {

    @Field(required = true)
    @Unique
    private String token;

    @Field(required = true)
    @Unique
    private String username;

    @Field
    private String email;

    @Field
    private DateTime expirationDate;

    @Field
    private Locale locale;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public DateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(DateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
