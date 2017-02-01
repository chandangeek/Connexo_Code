/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.DataValidationGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.DataValidationGroup'
    ],
    model: 'Cfg.model.DataValidationGroup',
    storeId: 'DataValidationGroup',
    proxy: {
        type: 'rest',
        url: '/api/val/kpis/groups',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'deviceGroups'
        }
    }
});
