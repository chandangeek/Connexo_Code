Ext.define('Fwc.devicefirmware.model.FirmwareVersion', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
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
            name: 'upgradeOption',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.firmwareUpgradeOption ? data.firmwareUpgradeOption.localizedValue : '';
            }
        }
    ],

    retry: function (mrid, callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: '/api/ddr/devices/{mrid}/comtasks/{id}/runnow'
                .replace('{mrid}', mrid)
                .replace('{id}', this.get('firmwareComTaskId')),
            callback: callback
        });
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
            associationKey: 'firmwareVersionStatus'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareUpgradeOption',
            name: 'firmwareUpgradeOption',
            associationKey: 'firmwareUpgradeOption'
        }
    ]
});