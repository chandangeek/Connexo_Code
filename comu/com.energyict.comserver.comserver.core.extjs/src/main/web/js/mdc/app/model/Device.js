Ext.define('Mdc.model.Device', {
    extend: 'Ext.data.Model',
    requires: [

    ],
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'mRId', type: 'string', useNull: true},
        {name: 'serialNumber', type: 'string', useNull: true},
        {name: 'deviceTypeId', type: 'number', useNull: true},
        {name: 'deviceTypeName', type: 'string', useNull: true},
        {name: 'deviceConfigurationId', type: 'number', userNull: true},
        {name: 'deviceConfigurationName', type: 'string', userNull: true},
        {name: 'yearOfCertification', type: 'number', userNull: true}
    ],

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices',
        reader: {
            type: 'json'
        }
    }

});

