/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.store.Rules', {
    extend: 'Ext.data.Store',
    model: 'Est.estimationrules.model.Rule',
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