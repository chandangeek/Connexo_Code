package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.common.BusinessException;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link UserFile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-09 (08:55)
 */
public interface UserFileFactory {

    public UserFile findUserFile (int userFileId);

    public List<UserFile> findAllUserFiles ();

    public UserFile createUserFile (UserFileShadow shadow) throws BusinessException, SQLException;

}