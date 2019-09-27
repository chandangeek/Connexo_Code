package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.engine.offline.gui.table.renderer.LocalizedListCellRenderer;

public class LogLevelAspectEditor extends EnumAspectEditor<ComServer.LogLevel> {

    public LogLevelAspectEditor(){
        super(ComServer.LogLevel.class, new LocalizedListCellRenderer());
    }

    public void removeLevel(ComServer.LogLevel level) {
        getValueComponent().removeItem(level);
    }

    public void addLevel(ComServer.LogLevel level, String name) {
        getValueComponent().addItem(level);
    }
}
