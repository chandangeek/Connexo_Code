package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 8/12/14.
 */
public class SuccessIndicatorInfo {
    private final static ComSessionSuccessIndicatorAdapter SUCCESS_INDICATOR_ADAPTER = new ComSessionSuccessIndicatorAdapter();
    @XmlJavaTypeAdapter(ComSessionSuccessIndicatorAdapter.class)
    public ComSession.SuccessIndicator id;
    public String displayValue;
    public Integer retries;

    public SuccessIndicatorInfo(ComSession.SuccessIndicator successIndicator, Thesaurus thesaurus) throws RuntimeException {
        this.id=successIndicator;
        this.displayValue=thesaurus.getString(SUCCESS_INDICATOR_ADAPTER.marshal(successIndicator), SUCCESS_INDICATOR_ADAPTER.marshal(successIndicator));
    }

    public SuccessIndicatorInfo(ComSession.SuccessIndicator successIndicator, ConnectionTask<?, ?> connectionTask, Thesaurus thesaurus) {
        this(successIndicator,thesaurus);

        if (!successIndicator.equals(ComSession.SuccessIndicator.Success) && connectionTask instanceof OutboundConnectionTask) {
            this.retries=((OutboundConnectionTask)connectionTask).getCurrentTryCount();
        }

    }
}
