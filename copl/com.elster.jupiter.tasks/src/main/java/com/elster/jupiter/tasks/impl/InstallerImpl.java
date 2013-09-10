package com.elster.jupiter.tasks.impl;

class InstallerImpl {

    public void install() {
        Bus.getOrmClient().install();
    }

}
