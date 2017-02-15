/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint', {
    extend: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',
        'Uni.util.Common'
    ],

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/customproperties/'
    }
});
