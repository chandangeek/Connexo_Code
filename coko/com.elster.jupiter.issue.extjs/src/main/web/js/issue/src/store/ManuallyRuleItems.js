/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.ManuallyRuleItems', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.ManuallyRuleItem',
    autoLoad: false,

    proxy: {
        type: 'rest',
        api: {
            create: '/api/isu/issues/add'
        },
        reader: {
            type: 'json',
        }
    }

});