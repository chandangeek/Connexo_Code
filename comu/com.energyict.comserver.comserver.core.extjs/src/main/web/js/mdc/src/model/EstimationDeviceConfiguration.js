/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.EstimationDeviceConfiguration', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'active',
        'deviceTypeName',
        'loadProfileCount',
        'registerCount',
        'version',
        'deviceTypeId',
        {
            name: 'config_active',
            persist: false,
            mapping: function (data) {
                return data.active ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/estimationrulesets/{id}/deviceconfigurations',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.ruleSetId);
        }
    }
});
