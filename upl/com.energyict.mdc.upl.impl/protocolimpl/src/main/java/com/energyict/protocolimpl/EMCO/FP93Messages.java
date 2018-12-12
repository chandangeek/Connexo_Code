package com.energyict.protocolimpl.EMCO;

import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 24/02/12
 * Time: 16:32
 */
public class FP93Messages extends ProtocolMessages {

    static final String FAULTS_FLAGS = "Clear faults and changed flags";
    static final String STATISTICAL_VALUES = "Clear statistical values";
    static final String TOTALIZERS = "Clear resettable totalizers";

    static final String TAG_CLEAR_FAULTS_FLAGS = "Clear_Faults_Flags";
    static final String TAG_CLEAR_STATISTICAL_VALUES = "Clear_Statistical_Values";
    static final String TAG_CLEAR_TOTALIZERS = "Clear_Totalizers";

    private FP93 meterProtocol;

    public FP93Messages(FP93 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public void applyMessages(List messageEntries) throws IOException {
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();

        MessageCategorySpec resetCat = new MessageCategorySpec(RtuMessageCategoryConstants.RESET_PARAMETERS);
        resetCat.addMessageSpec(addNoValueMsg(FAULTS_FLAGS, TAG_CLEAR_FAULTS_FLAGS, false));
        resetCat.addMessageSpec(addNoValueMsg(STATISTICAL_VALUES, TAG_CLEAR_STATISTICAL_VALUES, false));
        resetCat.addMessageSpec(addNoValueMsg(TOTALIZERS, TAG_CLEAR_TOTALIZERS, false));
        categories.add(resetCat);
        return categories;
    }

    /**
	 * Creates a MessageSpec with no fields to be filled in.
	 * @param keyId - id for the MessageSpec
	 * @param tagName - name for the MessageSpec
	 * @param advanced - indicates whether it's an advanced message or not
	 * @return the newly created MessageSpec
	 */
	protected MessageSpec addNoValueMsg(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (isItThisMessage(messageEntry, TAG_CLEAR_FAULTS_FLAGS)) {
                infoLog("Sending message " + FAULTS_FLAGS + ".");
                clearFaultFlags();
                infoLog(FAULTS_FLAGS + " message successful.");
                return MessageResult.createSuccess(messageEntry);
            } else if (isItThisMessage(messageEntry, TAG_CLEAR_STATISTICAL_VALUES)) {
                infoLog("Sending message " + STATISTICAL_VALUES + ".");
                clearStatisticalValues();
                infoLog(STATISTICAL_VALUES + " message successful.");
                return MessageResult.createSuccess(messageEntry);
            } else if ((isItThisMessage(messageEntry, TAG_CLEAR_TOTALIZERS))) {
                infoLog("Sending message " + TOTALIZERS + ".");
                clearTotalizers();
                infoLog(TOTALIZERS + " message successful.");
                return MessageResult.createSuccess(messageEntry);
            } else {
                infoLog("Unknown message received.");
                return MessageResult.createUnknown(messageEntry);
            }
        } catch (IOException e) {
            infoLog("Message failed : " + e.getMessage());
        }
        return MessageResult.createFailed(messageEntry);
    }

    private void clearFaultFlags() throws IOException {
        ObisCode faultFlagObisCode = this.meterProtocol.getObisCodeMapper().searchRegisterMapping(91).getObisCode();
        RegisterValue registerValue = this.meterProtocol.getObisCodeMapper().readRegister(faultFlagObisCode);
        infoLog("State of fault flags before the reset: " + registerValue.getQuantity().intValue() + " - " + registerValue.getText());
    }

    private void clearStatisticalValues() throws IOException {
        ObisCode clearStatsObisCode = this.meterProtocol.getObisCodeMapper().searchRegisterMapping(92).getObisCode();
        this.meterProtocol.getObisCodeMapper().readRegister(clearStatsObisCode);
    }

    private void clearTotalizers() throws IOException {
        ObisCode clearTotalizersObisCode = this.meterProtocol.getObisCodeMapper().searchRegisterMapping(93).getObisCode();
        this.meterProtocol.getObisCodeMapper().readRegister(clearTotalizersObisCode);
    }

    /**
     * Log the given message to the logger with the INFO level
     *
     * @param messageToLog
     */
    private void infoLog(String messageToLog) {
        this.meterProtocol.getLogger().info(messageToLog);
    }
}