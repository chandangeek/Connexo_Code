package com.energyict.protocolimplv2.sdksample;

/**
 * Created by bvn on 12/3/14.
 */

import com.elster.jupiter.datavault.DataVault;
import java.io.OutputStream;

/**
 * An unsecure DataVault. The encrypt will just make a String from the given bytes and the decrypt will do the reverse!.
 * <b>NOT SECURE FOR IN PRODUCTION</b>
 *
 * @see <a href="http://jira.eict.vpdc/browse/JP-3879">JP-3879</a>
 */
public class StraightForwardUnSecureDataVault implements DataVault {

    @Override
    public String encrypt(byte[] decrypted) {
        return new String(decrypted);
    }

    @Override
    public byte[] decrypt(String encrypted) {
        if (encrypted != null) {
            return encrypted.getBytes();
        } else {
            return new byte[0];
        }
    }

    @Override
    public void createVault(OutputStream stream) {

    }

}