Ext.define('Mdc.model.Register', {
    extend: 'Mdc.model.RegisterConfiguration',
    fields: [
        {name: 'lastReading', type:'date', dateFormat: 'time', useNull: true},
        {name: 'validationStatus', type:'boolean', useNull: true},
        {name: 'type', type:'string', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers'
    }
});