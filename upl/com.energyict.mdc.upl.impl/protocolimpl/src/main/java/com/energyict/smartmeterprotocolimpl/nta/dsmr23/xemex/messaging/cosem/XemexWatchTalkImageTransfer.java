package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.attributeobjects.ImageTransferStatus;

import java.io.IOException;

/**
 * @author sva
 * @since 25/03/2014 - 10:53
 */
public class XemexWatchTalkImageTransfer extends ImageTransfer {

    private int initPollingDelay = 2000;
    private int initPollingRetries = 15;      //Poll status for 30 sec
    private boolean usePollingInit;

    public XemexWatchTalkImageTransfer(ProtocolLink protocolLink) {
        super(protocolLink);
    }

    public XemexWatchTalkImageTransfer(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public boolean isUsePollingInit() {
        return usePollingInit;
    }

    public void setUsePollingInit(boolean usePollingInit) {
        this.usePollingInit = usePollingInit;
    }

    public int getInitPollingDelay() {
        return initPollingDelay;
    }

    public void setInitPollingDelay(int initPollingDelay) {
        this.initPollingDelay = initPollingDelay;
    }

    public int getInitPollingRetries() {
        return initPollingRetries;
    }

    public void setInitPollingRetries(int initPollingRetries) {
        this.initPollingRetries = initPollingRetries;
    }

    @Override
    protected void initializationBeforeSendingOfBlocks() throws IOException {
        if (usePollingInit) {
            pollUntilImageTransferStatusInitiated();
        } else {
            super.initializationBeforeSendingOfBlocks();
        }
    }

    /**
     * Wait until the image transfer status becomes initiated (= 1)
     */
    private final void pollUntilImageTransferStatusInitiated() throws IOException {
        int tries = initPollingRetries;
        while (--tries > 0) {
            try {
                Thread.sleep(initPollingDelay);
                final ImageTransferStatus transferStatus = readImageTransferStatus();
                switch (transferStatus.getValue()) {
                    case 0:
                        getLogger().info("Image transfer state: [Image transfer status not initiated].");
                        break;
                    case 1:
                        getLogger().info("Image transfer state: [Image transfer initiated].");
                        return;
                    default:
                        throw new IOException("Invalid Image transfer state [" + transferStatus.getValue() + "] - expected 0 or 1.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while polling the image transfer status. [" + e.getMessage() + "]");
            }
        }
        throw new IOException("Image transfer initiation failed, even after a few polls!");
    }

    @Override
    public void verifyAndPollForSuccess() throws IOException {
        try {
            imageVerification();
        } catch (DataAccessResultException e) {
            if (isTemporaryFailure(e) || isHardwareFailure(e)) {
                getLogger().info("Received [" + e.getCode().getDescription() + "] while verifying image.");
            } else {
                throw e;
            }
        }
        getLogger().info("Polling Image transfer status until successful verification. Polling result ...");
        pollForImageVerificationStatus();
    }
}
