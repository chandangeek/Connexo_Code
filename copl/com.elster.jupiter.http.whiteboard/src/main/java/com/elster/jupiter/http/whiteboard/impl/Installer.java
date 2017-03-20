/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import static com.elster.jupiter.util.Checks.is;

class Installer implements FullInstaller {
    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());
    private static final String PUBLIC_KEY_FILE_NAME = "publicKey.txt";

    private final DataModel dataModel;
    private final DataVaultService dataVaultService;
    private final BasicAuthentication basicAuthentication;
    private volatile UserService userService;

    @Inject
    public Installer(DataModel dataModel, DataVaultService dataVaultService, BasicAuthentication basicAuthentication, UserService userService) {
        this.dataModel = dataModel;
        this.dataVaultService = dataVaultService;
        this.basicAuthentication = basicAuthentication;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

        doTry(
                "Create Key Pairs",
                this::createKeys,
                logger
        );
        doTry(
                "Write public key to file " + PUBLIC_KEY_FILE_NAME,
                this::dumpPublicKey,
                logger
        );
    }

    private void createKeys() {
        if (!basicAuthentication.getKeyPair().isPresent()) {
            try {
                dataModel.getInstance(KeyStoreImpl.class).init(dataVaultService);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void dumpPublicKey() {
        if (!is(basicAuthentication.getInstallDir()).emptyOrOnlyWhiteSpace()) {
            Path conf = FileSystems.getDefault()
                    .getPath(basicAuthentication.getInstallDir())
                    .resolve(PUBLIC_KEY_FILE_NAME);
            dumpToFile(conf);
        }
    }

    private void dumpToFile(Path conf) {
        basicAuthentication.saveKeyToFile(conf);
    }

}
