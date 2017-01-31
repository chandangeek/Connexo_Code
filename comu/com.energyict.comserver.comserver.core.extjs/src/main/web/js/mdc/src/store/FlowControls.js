/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.FlowControls',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.FlowControl'
    ],
    model: 'Mdc.model.field.FlowControl',
    autoLoad: false,
    storeId: 'FlowControls',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/flowControl',
        reader: {
            type: 'json',
            root: 'flowControls'
        }
    }
});

