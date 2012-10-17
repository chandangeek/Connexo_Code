package test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 16:50:52
 */
public class CtrCipheringException extends CTRException {

    public CtrCipheringException(String s, Exception e) {
        super(s, e);
    }

    public CtrCipheringException(String s) {
        super(s);
    }

    public CtrCipheringException(Exception e) {
        super(e);
    }

    public CtrCipheringException() {
    }
}
