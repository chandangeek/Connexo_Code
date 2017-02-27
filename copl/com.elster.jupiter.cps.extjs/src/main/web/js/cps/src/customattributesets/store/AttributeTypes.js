/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.store.AttributeTypes', {
    extend: 'Ext.data.Store',
    model: 'Cps.customattributesets.model.AttributeType',
    requires: [
        'Cps.customattributesets.model.AttributeType'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/cps/custompropertysets/domains',
        reader: {
            type: 'json',
            root: 'domainExtensions'
        }
    },

    sorters: [
        {
            property: 'displayValue',
            direction: 'ASC'
        }
    ]
});