package com.elster.jupiter.users;

import java.security.Principal;
import java.time.Instant;
import java.util.*;

import com.elster.jupiter.util.HasName;

public interface User extends Principal, HasName {

    long getId();

    boolean hasPrivilege(String applicationName, String privilege);

    boolean hasPrivilege(String applicationName, Privilege privilege);

    boolean isMemberOf(String groupName);

    String getDescription();

    long getVersion();

    long getUserDirectoryId();

    void setDescription(String description);

    void update();

    void delete();

    /**
     * @param group
     * @return true, if a new membership was created, false if it already existed.
     */
    boolean join(Group group);

    /**
     * @param group
     * @return true if the membership existed, false otherwise.
     */
    boolean leave(Group group);

    boolean isMemberOf(Group group);

    List<Group> getGroups();

    /*
     * Returns the Ha1 used in Digest Authentication for the given user,
     * can be null if the user does not have interactive access
     * 
     */
    String getDigestHa1();

    void setPassword(String password);

    boolean check(String password);

    Optional<Locale> getLocale();

    void setLocale(Locale locale);

    Set<Privilege> getPrivileges();

    Map<String, List<Privilege>> getApplicationPrivileges();

    Set<Privilege> getPrivileges(String applicationName);

    String getDomain();

    boolean getStatus();

    void setStatus(boolean status);

    String getLanguage();

    Instant getCreationDate();

    Instant getModifiedDate();
}
