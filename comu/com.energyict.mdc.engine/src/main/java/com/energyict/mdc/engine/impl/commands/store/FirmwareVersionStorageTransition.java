package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.firmware.FirmwareStatus;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Copyrights EnergyICT
 * Date: 20.04.15
 * Time: 15:41
 */
public enum FirmwareVersionStorageTransition {

    UNKNOWN {
        @Override
        String getFromStatus() {
            return Constants.UNKNOWN;
        }

        @Override
        String getToStatus() {
            return Constants.UNKNOWN;
        }
    },

    EMPTY_EMPTY {
        @Override
        String getFromStatus() {
            return Constants.EMPTY;
        }

        @Override
        String getToStatus() {
            return Constants.EMPTY;
        }
    },

    EMPTY_NEW_GHOST {
        @Override
        String getFromStatus() {
            return Constants.EMPTY;
        }

        @Override
        String getToStatus() {
            return Constants.NEWGHOST;
        }
    },

    EMPTY_EXISTINGGHOST {
        @Override
        String getFromStatus() {
            return Constants.EMPTY;
        }

        @Override
        String getToStatus() {
            return Constants.EXISTINGGHOST;
        }
    },

    EMPTY_TEST {
        @Override
        String getFromStatus() {
            return Constants.EMPTY;
        }

        @Override
        String getToStatus() {
            return Constants.TEST;
        }
    },

    EMPTY_FINAL {
        @Override
        String getFromStatus() {
            return Constants.EMPTY;
        }

        @Override
        String getToStatus() {
            return Constants.FINAL;
        }
    },

    EMPTY_DEPRECATE {
        @Override
        String getFromStatus() {
            return Constants.EMPTY;
        }

        @Override
        String getToStatus() {
            return Constants.DEPRECATED;
        }
    },

    EXISTINGGHOST_EMPTY {
        @Override
        String getFromStatus() {
            return Constants.EXISTINGGHOST;
        }

        @Override
        String getToStatus() {
            return Constants.EMPTY;
        }
    },
    EXISTINGGHOST_NEW_GHOST {
        @Override
        String getFromStatus() {
            return Constants.EXISTINGGHOST;
        }

        @Override
        String getToStatus() {
            return Constants.NEWGHOST;
        }
    },
    EXISTINGGHOST_EXISTINGGHOST {
        @Override
        String getFromStatus() {
            return Constants.EXISTINGGHOST;
        }

        @Override
        String getToStatus() {
            return Constants.EXISTINGGHOST;
        }
    },
    EXISTINGGHOST_TEST {
        @Override
        String getFromStatus() {
            return Constants.EXISTINGGHOST;
        }

        @Override
        String getToStatus() {
            return Constants.TEST;
        }
    },
    EXISTINGGHOST_FINAL {
        @Override
        String getFromStatus() {
            return Constants.EXISTINGGHOST;
        }

        @Override
        String getToStatus() {
            return Constants.FINAL;
        }
    },
    EXISTINGGHOST_DEPRECATE {
        @Override
        String getFromStatus() {
            return Constants.EXISTINGGHOST;
        }

        @Override
        String getToStatus() {
            return Constants.DEPRECATED;
        }
    },
    TEST_EMPTY {
        @Override
        String getFromStatus() {
            return Constants.TEST;
        }

        @Override
        String getToStatus() {
            return Constants.EMPTY;
        }
    },
    TEST_NEW_GHOST {
        @Override
        String getFromStatus() {
            return Constants.TEST;
        }

        @Override
        String getToStatus() {
            return Constants.NEWGHOST;
        }
    },
    TEST_EXISTINGGHOST {
        @Override
        String getFromStatus() {
            return Constants.TEST;
        }

        @Override
        String getToStatus() {
            return Constants.EXISTINGGHOST;
        }
    },
    TEST_TEST {
        @Override
        String getFromStatus() {
            return Constants.TEST;
        }

        @Override
        String getToStatus() {
            return Constants.TEST;
        }
    },
    TEST_FINAL {
        @Override
        String getFromStatus() {
            return Constants.TEST;
        }

        @Override
        String getToStatus() {
            return Constants.FINAL;
        }
    },
    TEST_DEPRECATE {
        @Override
        String getFromStatus() {
            return Constants.TEST;
        }

        @Override
        String getToStatus() {
            return Constants.DEPRECATED;
        }
    },
    FINAL_EMPTY {
        @Override
        String getFromStatus() {
            return Constants.FINAL;
        }

        @Override
        String getToStatus() {
            return Constants.EMPTY;
        }
    },
    FINAL_NEW_GHOST {
        @Override
        String getFromStatus() {
            return Constants.FINAL;
        }

        @Override
        String getToStatus() {
            return Constants.NEWGHOST;
        }
    },
    FINAL_EXISTINGGHOST {
        @Override
        String getFromStatus() {
            return Constants.FINAL;
        }

        @Override
        String getToStatus() {
            return Constants.EXISTINGGHOST;
        }
    },
    FINAL_TEST {
        @Override
        String getFromStatus() {
            return Constants.FINAL;
        }

        @Override
        String getToStatus() {
            return Constants.TEST;
        }
    },
    FINAL_FINAL {
        @Override
        String getFromStatus() {
            return Constants.FINAL;
        }

        @Override
        String getToStatus() {
            return Constants.FINAL;
        }
    },
    FINAL_DEPRECATE {
        @Override
        String getFromStatus() {
            return Constants.FINAL;
        }

        @Override
        String getToStatus() {
            return Constants.DEPRECATED;
        }
    },
    DEPRECATE_EMPTY {
        @Override
        String getFromStatus() {
            return Constants.DEPRECATED;
        }

        @Override
        String getToStatus() {
            return Constants.EMPTY;
        }
    },
    DEPRECATE_NEW_GHOST {
        @Override
        String getFromStatus() {
            return Constants.DEPRECATED;
        }

        @Override
        String getToStatus() {
            return Constants.NEWGHOST;
        }
    },
    DEPRECATE_EXISTINGGHOST {
        @Override
        String getFromStatus() {
            return Constants.DEPRECATED;
        }

        @Override
        String getToStatus() {
            return Constants.EXISTINGGHOST;
        }
    },
    DEPRECATE_TEST {
        @Override
        String getFromStatus() {
            return Constants.DEPRECATED;
        }

        @Override
        String getToStatus() {
            return Constants.TEST;
        }
    },
    DEPRECATE_FINAL {
        @Override
        String getFromStatus() {
            return Constants.DEPRECATED;
        }

        @Override
        String getToStatus() {
            return Constants.FINAL;
        }
    },
    DEPRECATE_DEPRECATE {
        @Override
        String getFromStatus() {
            return Constants.DEPRECATED;
        }

        @Override
        String getToStatus() {
            return Constants.DEPRECATED;
        }
    };

    public static FirmwareVersionStorageTransition from(String fromStatus, String toStatus) {
        Optional<FirmwareVersionStorageTransition> match = Stream.of(values()).filter(transition -> transition.getFromStatus().equals(fromStatus) && transition.getToStatus().equals(toStatus)).findFirst();
        return match.map(firmwareVersionStorageTransitions -> firmwareVersionStorageTransitions).orElseGet(() -> UNKNOWN);
    }

    abstract String getFromStatus();

    abstract String getToStatus();

    public static class Constants {
        private static final String UNKNOWN = "UNKNOWN";

        private static final String NEWGHOST = "NEWGHOST";
        public static final  String EMPTY = "";
        private static final String EXISTINGGHOST = FirmwareStatus.GHOST.getStatus();
        private static final String TEST = FirmwareStatus.TEST.getStatus();
        private static final String FINAL = FirmwareStatus.FINAL.getStatus();
        private static final String DEPRECATED = FirmwareStatus.DEPRECATED.getStatus();
    }
}
