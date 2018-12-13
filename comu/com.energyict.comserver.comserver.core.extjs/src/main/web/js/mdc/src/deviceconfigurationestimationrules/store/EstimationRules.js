/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.deviceconfigurationestimationrules.store.EstimationRules', {
    extend: 'Ext.data.Store',
    model: 'Mdc.deviceconfigurationestimationrules.model.EstimationRule',
    requires: [
    'Mdc.deviceconfigurationestimationrules.model.EstimationRule'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/est/estimation/{id}/rules',
        reader: {
            type: 'json',
            root: 'rules'
        },
        setUrl: function (ruleSetId) {
            this.url = this.urlTpl.replace('{id}', ruleSetId);
        }
    }
});