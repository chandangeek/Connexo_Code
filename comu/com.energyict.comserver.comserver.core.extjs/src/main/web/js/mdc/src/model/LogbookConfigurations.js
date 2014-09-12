Ext.define('Mdc.model.LogbookConfigurations', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'obisCode',
            type: 'string'
        },
        {
            name: 'overruledObisCode',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfiguration}/logbookconfigurations',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
