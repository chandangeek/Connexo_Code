package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.DataModel;

public enum TableSpecs {
    PKI_CERTIFICATES {
        @Override
        void addTo(DataModel component) {

        }
    },
    PKI_KEYS {
        @Override
        void addTo(DataModel component) {

        }
    };

    abstract void addTo(DataModel component);

}
