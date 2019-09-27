package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.common.comserver.ComServer;

import java.util.Arrays;

public class LogLevelListAspectEditor extends ListAspectEditor {

    public LogLevelListAspectEditor(){
        super(Arrays.asList(ComServer.LogLevel.ERROR, ComServer.LogLevel.WARN, ComServer.LogLevel.INFO, ComServer.LogLevel.DEBUG, ComServer.LogLevel.TRACE));
    }

    public void removeLevel(ComServer.LogLevel level) {
        getValueComponent().removeItem(level);
    }

    public void addLevel(ComServer.LogLevel level, String name) {
        getValueComponent().addItem(level);
    }
}
