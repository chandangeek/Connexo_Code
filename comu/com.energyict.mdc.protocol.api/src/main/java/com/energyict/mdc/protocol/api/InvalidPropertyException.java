/** Java class "UnsupportedException.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.energyict.mdc.protocol.api;

/**
 * This exception is thrown when a meter protocol specific parameter has an invalid value.
 *
 * @author Karel
 */
public class InvalidPropertyException extends ProtocolException {

    public InvalidPropertyException() {
    }

    public InvalidPropertyException(String msg) {
        super(msg);
    }

}