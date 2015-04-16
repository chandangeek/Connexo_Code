Ext.define('Dlc.devicelifecycles.model.DeviceLifeCycle', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        {
            name: 'sorted_name',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        },
        {
            name: 'statesCount',
            persist: false
        },
        {
            name: 'actionsCount',
            persist: false
        },
        {
            name: 'deviceTypes',
            persist: false
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles',
        reader: {
            type: 'json'            
        }
    }
});
