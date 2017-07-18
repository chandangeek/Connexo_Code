/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.EstimationDeviceConfigurationsBuffered', {
    extend: 'Ext.data.Store',
    buffered: true,
    pageSize: 200,
    model: 'Mdc.model.EstimationDeviceConfiguration',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/estimationrulesets/{id}/linkabledeviceconfigurations',
        reader: {
            type: 'json',
            root: 'deviceConfigurations'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.ruleSetId);
        }
    }
});
