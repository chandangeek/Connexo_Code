package com.elster.jupiter.util.exception;

/**
 * Interface that marks the type of exception. Exception types are intended to be very finely grained.
 * Fine enough in fact to be able to pinpoint the problem in code. Conceptually an Exception type is equivalent with a unique exception number.
 * Given such a number, conceivably a list of possible causes and remedies can be compiled.
 * It is encouraged that every model implements this interface by defining an enum of all ExceptionTypes in the Module.
 */
public interface ExceptionType {

    /**
     * @return three letter code that identifies the module, which defines this ExceptionType.
     */
    String getModule();

    /**
     * @return unique number for this exception within the scope of a module.
     */
    int getNumber();
}
