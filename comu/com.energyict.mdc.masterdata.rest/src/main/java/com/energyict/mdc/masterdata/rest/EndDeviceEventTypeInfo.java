package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.rest.impl.MasterDataApplication;

public class EndDeviceEventTypeInfo {
    
    public String code;
    public EndDeviceTypeInfo deviceType;
    public EndDeviceDomainInfo domain;
    public EndDeviceSubDomainInfo subDomain;
    public EndDeviceEventOrActionInfo eventOrAction;

    public static EndDeviceEventTypeInfo from(EndDeviceEventType eventType, NlsService nlsService) {
        EndDeviceEventTypeInfo info = new EndDeviceEventTypeInfo();

        Thesaurus thesaurus = nlsService.getThesaurus(MasterDataApplication.COMPONENT_NAME, Layer.REST);
        
        info.code = eventType.getMRID();
        info.deviceType = EndDeviceTypeInfo.from(eventType.getType(), thesaurus);
        info.domain = EndDeviceDomainInfo.from(eventType.getDomain(), thesaurus);
        info.subDomain = EndDeviceSubDomainInfo.from(eventType.getSubDomain(), thesaurus);
        info.eventOrAction = EndDeviceEventOrActionInfo.from(eventType.getEventOrAction(), thesaurus);

        return info;
    }
    
    public static class EndDeviceTypeInfo {
        public int id;
        public String name;
        
        public static EndDeviceTypeInfo from(EndDeviceType deviceType, Thesaurus thesaurus) {
            EndDeviceTypeInfo info = new EndDeviceTypeInfo();
            info.id = deviceType.getValue();
            info.name = thesaurus.getString(EndDeviceType.class.getSimpleName() + deviceType.getMnemonic(), deviceType.getMnemonic());
            return info;
        }
    }
    
    public static class EndDeviceDomainInfo {
        public int id;
        public String name;
        
        public static EndDeviceDomainInfo from(EndDeviceDomain domain, Thesaurus thesaurus) {
            EndDeviceDomainInfo info = new EndDeviceDomainInfo();
            info.id = domain.getValue();
            info.name = thesaurus.getString(EndDeviceDomain.class.getSimpleName() + domain.getMnemonic(), domain.getMnemonic());
            return info;
        }
    }
    
    public static class EndDeviceSubDomainInfo {
        public int id;
        public String name;
        
        public static EndDeviceSubDomainInfo from(EndDeviceSubDomain subDomain, Thesaurus thesaurus) {
            EndDeviceSubDomainInfo info = new EndDeviceSubDomainInfo();
            info.id = subDomain.getValue();
            info.name = thesaurus.getString(EndDeviceSubDomain.class.getSimpleName() + subDomain.getMnemonic(), subDomain.getMnemonic());
            return info;
        }
    }
    
    public static class EndDeviceEventOrActionInfo {
        public int id;
        public String name;
        
        public static EndDeviceEventOrActionInfo from(EndDeviceEventorAction eventOrAction, Thesaurus thesaurus) {
            EndDeviceEventOrActionInfo info = new EndDeviceEventOrActionInfo();
            info.id = eventOrAction.getValue();
            info.name = thesaurus.getString(EndDeviceEventorAction.class.getSimpleName() + eventOrAction.getMnemonic(), eventOrAction.getMnemonic());
            return info;
        }
    }
}
