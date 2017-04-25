/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import java.util.Optional;

public interface PlaintextPassphrase extends PassphraseWrapper {
    /**
     * Plaintext passphrase exposes the actual passphrase. The passphrase is stored encrypted, but can be shown in decrypted from to the user.
     * @return If the wrapper contains a passphrase, the plaintext (decrypted) passphrase is returned, if not, Optional.empty()
     */
    Optional<String> getPassphrase();

    /**
     * Set the plaintxt value of the passphrase. The passphrase will be encrypted prior to storage in the db.
     * @param plainTextPassphrase The plaintext Passphrase to store.
     */
    void setPassphrase(String plainTextPassphrase);
}
