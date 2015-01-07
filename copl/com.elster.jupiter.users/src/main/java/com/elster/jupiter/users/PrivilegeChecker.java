package com.elster.jupiter.users;

/**
 * Copyrights EnergyICT
 * Date: 7/01/2015
 * Time: 9:28
 */
public interface PrivilegeChecker {
    boolean allowed(User user);
}
