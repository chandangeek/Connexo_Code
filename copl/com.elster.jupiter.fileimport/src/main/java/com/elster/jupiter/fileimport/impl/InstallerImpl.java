package com.elster.jupiter.fileimport.impl;

class InstallerImpl {

    public void install() {
        Bus.getOrmClient().install();
    }

}
