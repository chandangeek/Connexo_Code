/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.deviceconfigurationestimationrules.store.EstimationRuleSets', {
    extend: 'Ext.data.Store',
    model: 'Mdc.deviceconfigurationestimationrules.model.EstimationRuleSet',
    requires: [
        'Mdc.deviceconfigurationestimationrules.model.EstimationRuleSet'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/estimationrulesets',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        extraParams: {
          linkable: false
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