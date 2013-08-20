package com.elster.jupiter.orm;

public class UnderlyingSQLFailedException extends PersistenceException {

    public UnderlyingSQLFailedException(Throwable cause) {
        super(ExceptionTypes.SQL, cause);
    }
}
