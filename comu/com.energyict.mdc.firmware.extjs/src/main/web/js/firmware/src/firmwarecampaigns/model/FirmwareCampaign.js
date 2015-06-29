Ext.define('Fwc.firmwarecampaigns.model.FirmwareCampaign', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property',
        'Fwc.model.DeviceType',
        'Fwc.model.FirmwareType',
        'Fwc.model.DeviceGroup'
    ],
    fields: [
        'id',
        'name',
        {name: 'status', defaultValue: null},
        {name: 'firmwareVersion', defaultValue: null},
        {name: 'devicesStatus', defaultValue: null},
        {name: 'managementOption', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'deviceType', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'firmwareType', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'deviceGroup', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'startedOn', type: 'date', dateFormat: 'time', persist: false},
        {name: 'finishedOn', type: 'date', dateFormat: 'time', persist: false}
    ],
    associations: [
        {
            type: 'hasMany',
            name: 'properties',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/fwc/campaigns',
        reader: {
            type: 'json'
        }
    },
    convertObjectField: function (value) {
        if (Ext.isObject(value) || value === null) {
            return value
        } else {
            return {
                id: value
            }
        }
    }
});