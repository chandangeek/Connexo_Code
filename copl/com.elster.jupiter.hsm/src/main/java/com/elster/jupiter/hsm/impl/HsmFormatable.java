package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.EncryptBaseException;

public interface HsmFormatable<T> {

    T toHsmFormat() throws EncryptBaseException;

}
