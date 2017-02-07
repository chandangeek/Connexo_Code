/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.CreationRules', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRule',
    pageSize: 10,
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/creationrules',
        reader: {
            type: 'json',
            root: 'creationRules'
        }
    }
});
