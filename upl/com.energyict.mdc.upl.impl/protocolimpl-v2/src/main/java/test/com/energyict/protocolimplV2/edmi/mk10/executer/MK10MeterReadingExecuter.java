package test.com.energyict.protocolimplV2.edmi.mk10.executer;

import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;

import java.io.IOException;
import java.util.Iterator;

// ToDo: should be integrated in new DeviceProtocol
public class MK10MeterReadingExecuter {

	private static final int DEBUG = 0;
	
	private MK10ProtocolExecuter executer 	= null;

	/*
	 * Constructors
	 */

    public MK10MeterReadingExecuter(MK10ProtocolExecuter mk10ProtocolExecuter) {
		this.executer = mk10ProtocolExecuter;
	}
	
	/*
	 * Private getters, setters and methods
	 */

	public MK10ProtocolExecuter getExecuter() {
		return executer;
	}
	
	/*
	 * Public methods
	 */

	public MeterReadingData getMeterReadings() throws UnsupportedException, IOException {
        MeterReadingData mrd = null;
        Iterator it = getExecuter().getMeter().getRegisters().iterator();
        StringBuffer strBuff = null;
        while (it.hasNext()) {
        	RtuRegister rtuRegister = (RtuRegister) it.next();
            RegisterValue registerValue = null;
            ObisCode obisCode = rtuRegister.getRtuRegisterSpec().getObisCode();
            try {
                registerValue = getExecuter().getMk10Protocol().readRegister(obisCode);
            }
            catch (NoSuchRegisterException e) {
                if (strBuff == null)
                    strBuff = new StringBuffer();
                else
                    strBuff.append(" , ");
                registerValue = new RegisterValue(obisCode);
                strBuff.append(obisCode.toString());
            }
            registerValue.setRtuRegisterId(rtuRegister.getId()); // FIXME: getId == RtuRegisterId ?????
            if (mrd == null) mrd = new MeterReadingData();
            mrd.add(registerValue);

        } // while(it.hasNext())

        // Add all unsupported obiscodes to a stringbuffer and post once to the amr journal, otherwise we get a primarykey constrain error!
        if (strBuff != null) {
        	getExecuter().adjustCompletionCode(AmrJournalEntry.CC_CONFIGURATION_WARNING);
            getExecuter().journal(new AmrJournalEntry(AmrJournalEntry.NO_SUCH_REGISTER, strBuff.toString()));
        }

        return mrd;
    }

}
