Ext.define('Fwc.devicefirmware.model.FirmwareVersion', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'firmwareDeviceMessageId', type: 'number', useNull: true},
        {name: 'firmwareComTaskId', type: 'number', useNull: true},
        {name: 'firmwareVersion', type: 'string', useNull: true},
        {name: 'plannedDate', type: 'date', dateFormat: 'time', useNull: true},
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