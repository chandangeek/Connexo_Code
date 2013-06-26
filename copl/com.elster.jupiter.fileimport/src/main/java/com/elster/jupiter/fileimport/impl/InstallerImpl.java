package com.elster.jupiter.fileimport.impl;

public class InstallerImpl {

    public void install() {
        Bus.getOrmClient().install();
    }

}
