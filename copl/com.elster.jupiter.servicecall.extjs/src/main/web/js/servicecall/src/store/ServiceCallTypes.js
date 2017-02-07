/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.store.ServiceCallTypes', {
    extend: 'Ext.data.Store',
    model: 'Scs.model.ServiceCallType',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/scs/servicecalltypes',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'serviceCallTypes'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});
