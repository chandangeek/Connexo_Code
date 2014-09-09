Ext.define('Mdc.model.DeviceGroup', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'dynamic', type: 'boolean', useNull: true}
    ],

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devicegroups',
        reader: {
            type: 'json'
        }
    }

});