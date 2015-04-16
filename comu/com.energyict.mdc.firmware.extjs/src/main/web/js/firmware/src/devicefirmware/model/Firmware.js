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
                return data.activeVersion ? data.activeVersion.firmwareVersion : Uni.I18n.translate('device.firmware.version.unknown', 'FWC', 'Unknown');
            }
        }
    ],

    requires: [
        'Fwc.model.FirmwareType',
        'Fwc.devicefirmware.model.FirmwareVersion'
    ],

    retry: function (comTaskId, callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: 'api/ddr/devices/{mrid}/comtasks/{id}/runnow'.replace('{id}', comTaskId),
            callback: callback
        });
    },

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
            associatedName: 'activeVersion',
            associationKey: 'activeVersion',
            getterName: 'getActiveVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'pendingVersion',
            associatedName: 'pendingVersion',
            associationKey: 'pendingVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'failedVersion',
            associatedName: 'failedVersion',
            associationKey: 'failedVersion'
        },
        {
            type: 'hasOne',
            model: 'Fwc.devicefirmware.model.FirmwareVersion',
            name: 'ongoingVersion',
            associatedName: 'ongoingVersion',
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