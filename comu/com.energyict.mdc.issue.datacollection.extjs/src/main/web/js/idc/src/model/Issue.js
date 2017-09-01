/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.model.Issue', {
    extend: 'Isu.model.Issue',
    requires: [
        'Idc.model.Gateway'
    ],
    fields: [
        'deviceConfiguration', 'deviceType', 'deviceState', 'deviceName', 'slaveDeviceIdentification', 'connectionAttemptsNumber', 'connectionTask',
        'communicationTask', 'master', 'masterDeviceConfig', 'masterDeviceType', 'masterState', 'masterUsagePoint', 'gateways', 'masterFrom', 'masterTo',
        {name: 'firstConnectionAttempt', type: 'date', dateFormat: 'time'},
        {name: 'lastConnectionAttempt', type: 'date', dateFormat: 'time'},
        {name: 'deviceState_name', persist: false, mapping: 'deviceState.name'},
        {name: 'masterState_name', persist: false, mapping: 'masterState.name'},
        {name: 'connectionMethod_name', persist: false, mapping: 'connectionTask.connectionMethod.name'},
        {name: 'connectionTask_latestAttempt', type: 'date', dateFormat: 'time', persist: false, mapping: 'connectionTask.latestAttempt'},
        {name: 'connectionTask_latestStatus_name', persist: false, mapping: 'connectionTask.latestStatus.name'},
        {name: 'connectionTask_latestResult_name', persist: false, mapping: 'connectionTask.latestResult.name'},
        {name: 'connectionTask_lastSuccessfulAttempt', type: 'date', dateFormat: 'time', persist: false, mapping: 'connectionTask.lastSuccessfulAttempt'},
        {name: 'communicationTask_name', persist: false, mapping: 'communicationTask.name'},
        {name: 'latestConnectionUsed_name', persist: false, mapping: 'communicationTask.latestConnectionUsed.name'},
        {name: 'communicationTask_latestAttempt', type: 'date', dateFormat: 'time', persist: false, mapping: 'communicationTask.latestAttempt'},
        {name: 'communicationTask_latestStatus_name', persist: false, mapping: 'communicationTask.latestStatus.name'},
        {name: 'communicationTask_latestResult_name', persist: false, mapping: 'communicationTask.latestResult.name'},
        {name: 'communicationTask_lastSuccessfulAttempt', type: 'date', dateFormat: 'time', persist: false, mapping: 'communicationTask.lastSuccessfulAttempt'},
        {name: 'connectionTask_journals', persist: false, mapping: 'connectionTask.journals'},
        {name: 'communicationTask_journals', persist: false, mapping: 'communicationTask.journals'},
        {
            name: 'period',
            persist: false,
            mapping: function (data) {
                var result, startDate, endDate;

                startDate = data.masterFrom;
                endDate = data.masterTo;
                if (!Ext.isEmpty(startDate) && !Ext.isEmpty(endDate)) {
                    result = Uni.I18n.translate('period.fromXToY', 'IDC', "From {0} to {1}",
                        [Uni.DateTime.formatDateTimeShort(new Date(startDate)),
                            Uni.DateTime.formatDateTimeShort(new Date(endDate))],
                        false
                    );
                } else if (!Ext.isEmpty(startDate)) {
                    result = Uni.I18n.translate('period.fromX', 'IDC', "From {0}",
                        [Uni.DateTime.formatDateTimeShort(new Date(startDate))], false);
                } else {
                    result = '-'
                }
                return result;
            }
        }
    ],

    associations: [
        {
            name: 'gateways',
            type: 'hasMany',
            model: 'Idc.model.Gateway',
            associationKey: 'gateways'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/idc/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});