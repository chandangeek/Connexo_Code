/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.EstimationRules', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.EstimationRule',
    proxy: {
        type: 'rest',
        urlTpl: '/api/est/estimation/{ruleSetId}/rules',
        reader: {
            type: 'json',
            root: 'rules',
            totalProperty: 'total'
        },
        setUrl: function (ruleSetId) {
            this.url = this.urlTpl.replace('{ruleSetId}', ruleSetId);
        }
    }
});