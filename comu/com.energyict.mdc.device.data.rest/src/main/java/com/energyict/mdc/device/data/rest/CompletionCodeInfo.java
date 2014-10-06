package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 8/12/14.
 */
public class CompletionCodeInfo {
    private static final CompletionCodeAdapter COMPLETION_CODE_ADAPTER = new CompletionCodeAdapter();
    @XmlJavaTypeAdapter(CompletionCodeAdapter.class)
    public CompletionCode id;
    public String displayValue;

    public static CompletionCodeInfo from(CompletionCode completionCode, Thesaurus thesaurus) {
        CompletionCodeInfo info = new CompletionCodeInfo();
        info.id=completionCode;
        info.displayValue=thesaurus.getString(COMPLETION_CODE_ADAPTER.marshal(completionCode),COMPLETION_CODE_ADAPTER.marshal(completionCode));
        return info;
    }
}
