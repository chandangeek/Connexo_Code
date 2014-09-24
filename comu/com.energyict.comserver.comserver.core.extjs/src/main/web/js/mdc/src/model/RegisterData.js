Ext.define('Mdc.model.RegisterData', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timeStamp', type:'date', dateFormat: 'time'},
        {name: 'reportedDateTime', type:'date', dateFormat: 'time'},
        {name: 'validationStatus', type:'auto', useNull: true},
        {name: 'type', type:'string', useNull: true},
        {name: 'value', type:'string', useNull: true},
        {name: 'dataValidated', type:'auto'},
        {name: 'suspectReason', type:'auto'},
        {name: 'validationResult', type:'auto'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers/{registerId}/data'
    }
});
