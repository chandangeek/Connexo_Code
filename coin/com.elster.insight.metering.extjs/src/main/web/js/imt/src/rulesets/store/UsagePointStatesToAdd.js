/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.store.UsagePointStatesToAdd', {
    extend: 'Ext.data.Store',
    model: 'Imt.rulesets.model.UsagePointState',
    proxy: {
        type: 'rest',
        url: '/api/upl/states',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'states'
        }
    }
});