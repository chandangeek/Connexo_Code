package com.energyict.mdc.protocol.api;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the behavior of a component
 * that is capable of finding {@link UserFile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-09 (08:55)
 */
public interface UserFileFactory {

    UserFile findUserFile(int userFileId);

    List<UserFile> findAllUserFiles();

    UserFile createUserFile(UserFileShadow shadow) throws SQLException;

}