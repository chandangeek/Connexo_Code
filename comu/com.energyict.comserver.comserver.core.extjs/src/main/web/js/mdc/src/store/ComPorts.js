/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ComPorts',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComPort'
    ],
    model: 'Mdc.model.ComPort',
    storeId: 'ComPorts',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/comports',
        reader: {
            type: 'json',
            root: 'ComPorts'
        }
    }
});
