Ext.define('Mdc.model.DeviceGroup', {
    extend: 'Uni.model.Version',
    requires: [
        'Mdc.model.SearchCriteria'
    ],
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'mRID', type: 'string', useNull: true},
        {name: 'dynamic', type: 'boolean', defaultValue: true},
        {name: 'filter', type: 'auto', useNull: true, defaultValue: null},
        {name: 'devices', type: 'auto', useNull: true, defaultValue: null},
        {name: 'deviceTypeIds', persist: false},
        {name: 'deviceConfigurationIds', persist: false},
        {name: 'selectedDevices', persist: false}
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devicegroups',
        reader: {
            type: 'json'
        }
    },

    getNumberOfSearchResults: function (callback) {
        var me = this;

        Ext.Ajax.request({
            method: 'GET',
            url: '/api/jsr/search/com.energyict.mdc.device.data.Device/count',
            params: {
                filter: me.get('filter')
            },
            callback: callback
        });
    }
});