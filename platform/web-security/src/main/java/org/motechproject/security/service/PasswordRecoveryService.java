package org.motechproject.security.service;

import org.joda.time.DateTime;
import org.motechproject.security.exception.InvalidTokenException;
import org.motechproject.security.exception.NonAdminUserException;
import org.motechproject.security.exception.UserNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Service that defines APIs to manage password recovery
 */
public interface PasswordRecoveryService {

    /**
     * Creates password recovery for the user with the given email address and sends a recovery email
     *
     * @param email address of the user
     * @return the recovery token that can be used for resetting the password
     * @throws UserNotFoundException when no user for the given email exists
     */
    String passwordRecoveryRequest(String email) throws UserNotFoundException;

    /**
     * Creates password recovery for the user with the given email address, with an optional email notification.
     *
     * @param email address of the user
     * @param notify about the recovery
     * @return the recovery token that can be used for resetting the password
     * @throws UserNotFoundException when no user for the given email exists
     */
    String passwordRecoveryRequest(String email, boolean notify) throws UserNotFoundException;

    /**
     * Creates password recovery for the user with the given email address and sends a recovery email.
     * The recovery will expire on the given date.
     *
     * @param email address of the user
     * @param expiration date of recovery, it shouldn't be a past date
     * @return the recovery token that can be used for resetting the password
     * @throws UserNotFoundException when no user for the given email exists
     */
    String passwordRecoveryRequest(String email, DateTime expiration) throws UserNotFoundException;

    /**
     * Creates password recovery for the user with the given email address, with an optional email notification.
     * The recovery will expire on the given date.
     *
     * @param email address of the user
     * @param expiration date of recovery, it shouldn't be a past date
     * @param notify about the recovery
     * @return the recovery token that can be used for resetting the password
     * @throws UserNotFoundException when no user for the given email exists
     */
    String passwordRecoveryRequest(String email, DateTime expiration, boolean notify) throws UserNotFoundException;

    /**
     * Sets new password for user from token
     *
     * @param token for {@link org.motechproject.security.domain.PasswordRecovery}
     * @param password to be set for user
     * @param passwordConfirmation to check is password is correct
     * @throws InvalidTokenException when {@link org.motechproject.security.domain.PasswordRecovery}
     * as a null, recovery is already expired
     * or when user for name from token doesn't exists
     */
    void resetPassword(String token, String password, String passwordConfirmation) throws InvalidTokenException;

    /**
     * Removes all expired recoveries
     */
    void cleanUpExpiredRecoveries();

    /**
     * Checks if there's a not expired {@link org.motechproject.security.domain.PasswordRecovery}
     * for given token
     *
     * @param token to validate
     * @return true if recovery exists, otherwise false
     */
    boolean validateToken(String token);

    /**
     * Creates an one time token for OpenId for the user with the given email address and sends a recovery email
     *
     * @param email address of the user
     * @return the recovery token that can be used for resetting the password
     * @throws UserNotFoundException when no user for the given email exists
     * @throws NonAdminUserException when the user for the given email is not an
     * admin (don't have Admin role)
     */
    String oneTimeTokenOpenId(String email) throws UserNotFoundException, NonAdminUserException;

    /**
     * Creates an one time token for OpenId for the user with the given email address, with an optional email notification.
     *
     * @param email address of the user
     * @param notify about the recovery
     * @return the recovery token that can be used for resetting the password
     * @throws UserNotFoundException when no user with the given email exists
     * @throws NonAdminUserException when the user for the given email is not an
     * admin (don't have Admin role)
     */
    String oneTimeTokenOpenId(String email, boolean notify) throws UserNotFoundException, NonAdminUserException;

    /**
     * Creates an one time token for OpenId for the user with the given email address, with an optional email notification.
     * The recovery will expire on the given date.
     *
     * @param email address of the user
     * @param expiration date of recovery, it shouldn't be a past date
     * @param notify about the recovery
     * @return the recovery token that can be used for resetting the password
     * @throws UserNotFoundException when no user with the given email exists
     * @throws NonAdminUserException when the user for the given email is not an
     * admin (don't have Admin role)
     */
    String oneTimeTokenOpenId(String email, DateTime expiration, boolean notify) throws UserNotFoundException, NonAdminUserException;

    /**
     * Creates new openId Token for user from token as long as there's a
     * {@link org.motechproject.security.domain.PasswordRecovery} for that token
     * and redirect to home page. If there's no such recovery then redirect to login page
     *
     * @param token for password recovery
     * @param request for session
     * @param response for session
     * @throws IOException when response cannot redirect to given URL (home or login page)
     */
    void validateTokenAndLoginUser(String token, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
