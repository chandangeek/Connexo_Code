package com.elster.jupiter.tasks.impl;

public class InstallerImpl {

    public void install() {
        Bus.getOrmClient().install();
    }

}
