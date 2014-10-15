Ext.define('Mdc.model.RegisterData', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timeStamp', type:'number', useNull: true},
        {name: 'reportedDateTime', type:'date', dateFormat: 'time'},
        {name: 'editedDateTime', type:'date', dateFormat: 'time'},
        {name: 'validationStatus', type:'auto', useNull: true, persist: false},
        {name: 'type', type:'string', useNull: true},
        {name: 'value', type:'string', useNull: true, defaultValue: null},
        {name: 'dataValidated', type:'auto', persist: false},
        {name: 'suspectReason', type:'auto', persist: false},
        {name: 'validationResult', type:'auto', persist: false}
    ],
    idProperty: 'timeStamp',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers/{registerId}/data'
    }
});
