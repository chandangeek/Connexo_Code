/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.CreationRuleReasons', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueReason',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/reasons',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});