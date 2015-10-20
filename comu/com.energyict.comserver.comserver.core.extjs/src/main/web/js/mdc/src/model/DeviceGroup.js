Ext.define('Mdc.model.DeviceGroup', {
    extend: 'Uni.model.Version',
    requires: [
        'Mdc.model.SearchCriteria'
    ],
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'mRID', type: 'string', useNull: true},
        {name: 'dynamic', type: 'boolean', useNull: true},
        {name: 'filter', type: 'auto', useNull: true, defaultValue: null},
        {name: 'devices', type: 'auto', useNull: true, defaultValue: null},
        {name: 'deviceTypeIds', persist: false},
        {name: 'deviceConfigurationIds', persist: false},
        {name: 'selectedDevices', persist: false}
    ],

    associations: [
        {
            type: 'hasMany',
            model: 'Mdc.model.SearchCriteria',
            associationKey: 'criteria',
            name: 'criteria'
        }
    ],

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devicegroups',
        reader: {
            type: 'json'
        }
    }

});