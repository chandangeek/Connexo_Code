Ext.define('Fwc.devicefirmware.model.Firmware', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {
            name: 'type',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.firmwareType ? data.firmwareType.displayValue : '';
            }
        },
        {
            name: 'version',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.activeVersion ? data.activeVersion.firmwareVersion : '';
            }
        }
    ],

    requires: [
        'Fwc.model.FirmwareType',
        'Fwc.devicefirmware.model.FirmwareVersion'
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Fwc.model.FirmwareType',
            name: 'firmwareType',
            associationKey: 'firmwareType'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'activeVersion',
            associationKey: 'activeVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'pendingVersion',
            associationKey: 'pendingVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'failedVersion',
            associationKey: 'failedVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'ongoingVersion',
            associationKey: 'ongoingVersion'
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/device/{deviceId}/firmwares',
        reader: 'json',
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }
});