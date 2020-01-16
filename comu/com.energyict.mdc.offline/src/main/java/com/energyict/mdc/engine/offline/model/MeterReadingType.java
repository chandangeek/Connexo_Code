package com.energyict.mdc.engine.offline.model;

import com.energyict.mdc.engine.offline.MdwIcons;

import javax.swing.*;

public enum MeterReadingType {
    Remote {
        public Icon getIcon() {
            return MdwIcons.REMOTE_METER_READ_ICON_24;
        }
    },
    Manual {
        public Icon getIcon() {
            return MdwIcons.MANUAL_METER_READ_ICON_24;
        }
    },
    None {
        public Icon getIcon() {
            return null;
        }
    };

    public abstract Icon getIcon();
}
