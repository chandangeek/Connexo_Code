/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.store.CustomAttributeSets', {
    extend: 'Ext.data.Store',
    model: 'Imt.customattributesets.model.CustomAttributeSet',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations/{id}/custompropertysets',
        reader: {
            type: 'json',
            root: 'customPropertySets'
        }
    }
});