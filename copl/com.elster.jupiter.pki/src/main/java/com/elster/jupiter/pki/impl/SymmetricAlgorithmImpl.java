package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.SymmetricAlgorithm;

public final class SymmetricAlgorithmImpl implements SymmetricAlgorithm {
        private final String cipherName;
        private final String identifier;
        private final int keyLength;
        
        public SymmetricAlgorithmImpl(SymmetricAlgorithm symmetricAlgorithm) {
            this.cipherName = symmetricAlgorithm.getCipherName();
            this.identifier = symmetricAlgorithm.getIdentifier();
            this.keyLength = symmetricAlgorithm.getKeyLength();
        }

        @Override
        public String getCipherName() {
            return cipherName;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public int getKeyLength() {
            return keyLength;
        }
    }

