/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.model.FirmwareVersion', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'firmwareVersionId', type: 'number', useNull: true},
        {name: 'firmwareDeviceMessageId', type: 'number', useNull: true},
        {name: 'firmwareComTaskId', type: 'number', useNull: true},
        {name: 'firmwareComTaskSessionId', type: 'number', useNull: true},
        {name: 'firmwareVersion', type: 'string', useNull: true},
        {name: 'plannedDate', type: 'date', dateFormat: 'time', useNull: true},
        {name: 'plannedActivationDate', type: 'date', dateFormat: 'time', useNull: true, persist: false},
        {name: 'lastCheckedDate', type: 'date', dateFormat: 'time', useNull: true},
        {name: 'uploadStartDate', type: 'date', dateFormat: 'time', useNull: true},
        {
            name: 'status',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.firmwareVersionStatus ? data.firmwareVersionStatus.localizedValue : '';
            }
        },
        {
            name: 'managementOption',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.firmwareUpgradeOption ? data.firmwareManagementOption.localizedValue : '';
            }
        }
    ],

    retry: function (deviceId, callback) {
        var request = {
            method: 'PUT',
            url: '/api/fwc/devices/{deviceId}/comtasks/{id}/retry'
                .replace('{deviceId}', deviceId)
                .replace('{id}', this.get('firmwareComTaskId'))
        };

        if (Ext.isFunction(callback)) {
            request.callback = callback;
            Ext.Ajax.request(request);
        } else {
            Ext.Ajax.request(Ext.merge(request, callback));
        }
    },

    requires: [
        'Fwc.model.FirmwareStatus',
        'Fwc.devicefirmware.model.FirmwareUpgradeOption'
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Fwc.model.FirmwareStatus',
            name: 'firmwareVersionStatus',
            associationKey: 'firmwareVersionStatus',
            getterName: 'getFirmwareVersionStatus'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareUpgradeOption',
            name: 'firmwareManagementOption',
            associationKey: 'firmwareManagementOption'
        }
    ]
});