/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'
    ],
    model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint',

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/customproperties/metrologyconfiguration',
        reader: {
            type: 'json',
            root: 'customPropertySets'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});