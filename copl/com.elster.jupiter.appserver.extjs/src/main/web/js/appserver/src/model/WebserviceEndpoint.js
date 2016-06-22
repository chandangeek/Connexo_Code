Ext.define('Apr.model.WebserviceEndpoint', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'name',
        {
            name: 'isActive',
            persist: false
        },
        {
            name: 'webServiceName',
            persist: false
        }
    ]
});