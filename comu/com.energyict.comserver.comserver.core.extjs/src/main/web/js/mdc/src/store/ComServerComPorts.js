/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ComServerComPorts',{
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ComServerComPort',
   /* sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    sortOnLoad: true,*/
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comservers/{comServerId}/comports',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
