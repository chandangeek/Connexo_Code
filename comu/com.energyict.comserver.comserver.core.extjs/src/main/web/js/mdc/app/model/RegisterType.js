Ext.define('Mdc.model.RegisterType', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'registerTypeId', type: 'int', useNull: true},
        {name:'name', type: 'string'}
    ],
    idProperty: 'registerTypeId'
});