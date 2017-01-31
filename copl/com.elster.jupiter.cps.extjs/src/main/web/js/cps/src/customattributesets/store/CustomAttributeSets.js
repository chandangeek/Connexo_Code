/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.store.CustomAttributeSets', {
    extend: 'Ext.data.Store',
    model: 'Cps.customattributesets.model.CustomAttributeSet',
    requires: [
        'Cps.customattributesets.model.CustomAttributeSet'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/cps/custompropertysets',
        reader: {
            type: 'json',
            root: 'customAttributeSets'
        }
    }
});