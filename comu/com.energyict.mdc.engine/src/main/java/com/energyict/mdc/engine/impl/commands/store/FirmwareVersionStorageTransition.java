package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.exceptions.MessageSeeds;
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_NEW_GHOST);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_EXISTING_GHOST);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_DEPRECATE);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_EMPTY_WAS_GHOST);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_NEW_GHOST);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_DEPRECATE);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_EMPTY_WAS_TEST);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_NEW_GHOST);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_EXISTING_GHOST);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_DEPRECATE);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_EMPTY_WAS_FINAL);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_NEW_GHOST_WAS_FINAL);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_EXISTING_GHOST_WAS_FINAL);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_DEPRECATE_WAS_FINAL);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_EMPTY_WAS_DEPRECATE);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_NEW_GHOST);
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

        @Override
        Optional<MessageSeeds> getMessageSeed() {
            return Optional.of(MessageSeeds.FW_DISCOVERED_EXISTING_GHOST);
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

    Optional<MessageSeeds> getMessageSeed(){
        return Optional.empty();
    }

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
