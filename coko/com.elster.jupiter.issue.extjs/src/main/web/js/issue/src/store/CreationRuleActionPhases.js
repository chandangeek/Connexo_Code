/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.CreationRuleActionPhases', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRuleActionPhase',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/actions/phases',
        reader: {
            type: 'json',
            root: 'creationRuleActionPhases'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});