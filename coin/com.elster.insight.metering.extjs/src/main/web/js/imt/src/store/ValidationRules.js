/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.store.ValidationRules', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.ValidationRule'
    ],
    model: 'Cfg.model.ValidationRule',
    proxy: {
        type: 'rest',
        url: '/api/val/validation/{ruleSetId}/versions/{versionId}/rules',
        reader: {
            type: 'json',
            root: 'rules',
            totalProperty: 'total'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});

