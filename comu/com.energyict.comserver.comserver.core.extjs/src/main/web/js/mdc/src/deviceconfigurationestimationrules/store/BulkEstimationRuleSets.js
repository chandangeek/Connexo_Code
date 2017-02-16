/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.deviceconfigurationestimationrules.store.BulkEstimationRuleSets', {
    extend: 'Ext.data.Store',
    model: 'Mdc.deviceconfigurationestimationrules.model.EstimationRuleSet',
    requires: [
        'Mdc.deviceconfigurationestimationrules.model.EstimationRuleSet'
    ],
    autoLoad: false,

    buffered: true,
    pageSize: 10,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/estimationrulesets',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        extraParams: {
            linkable: true
        },
        reader: {
            type: 'json',
            root: 'estimationRuleSets'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{deviceTypeId}', params.deviceTypeId).replace('{deviceConfigurationId}', params.deviceConfigurationId);
        }
    }
});