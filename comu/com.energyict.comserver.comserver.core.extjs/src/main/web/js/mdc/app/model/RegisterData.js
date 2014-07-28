Ext.define('Mdc.model.RegisterData', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timeStamp', type:'date', dateFormat: 'time'},
        {name: 'validationStatus', type:'auto', useNull: true},
        {name: 'type', type:'string', useNull: true},
        {name: 'value', type:'auto', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers/{registerId}/data'
    }
});
