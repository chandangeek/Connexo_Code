package com.energyict.mdc.protocol.api.impl;

import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.UserFileShadow;

import org.osgi.service.component.annotations.Component;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Provides a dummy implementation for the {@link UserFileFactory}
 * until the jupiter platform adds support for the legacy {@link UserFile} entity.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-29 (09:21)
 */
@Component(name="com.energyict.mdc.legacy.factory.userfile", service = UserFileFactory.class)
@SuppressWarnings("unused")
public class DummyUserFileFactoryImpl implements UserFileFactory {

    @Override
    public UserFile findUserFile(int userFileId) {
        return null;
    }

    @Override
    public List<UserFile> findAllUserFiles() {
        return Collections.emptyList();
    }

    @Override
    public UserFile createUserFile(UserFileShadow shadow) throws SQLException {
        throw new UnsupportedOperationException("UserFile is not supported yet by the jupiter platform");
    }

}