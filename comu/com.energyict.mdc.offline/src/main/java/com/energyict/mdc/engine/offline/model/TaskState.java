package com.energyict.mdc.engine.offline.model;

public enum TaskState {

    READY_TO_READ {
        @Override
        public String getTranslationKey() {
            return "waitread";
        }
    },
    READ_SUCCESS {
        @Override
        public String getTranslationKey() {
            return "readSuccess";
        }
    },
    READ_FAILED {
        @Override
        public String getTranslationKey() {
            return "readFail";
        }
    },

    POST_DONE {
        @Override
        public String getTranslationKey() {
            return "postDone";
        }
    },

    POSTING {
        @Override
        public String getTranslationKey() {
            return "posting";
        }
    },

    EXECUTING {
        @Override
        public String getTranslationKey() {
            return "executing";
        }
    },

    ABORTING {
        @Override
        public String getTranslationKey() {
            return "aborting";
        }
    };

    public abstract String getTranslationKey();
}
