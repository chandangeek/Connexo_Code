
Ext.define('Mdc.model.MessageActivate', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'messageIds', type: 'auto'},
        {name: 'privileges', type: 'auto'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/devicemessageenablements',
        reader: {
            type: 'json',
            root: 'categories'
        }
    }
});