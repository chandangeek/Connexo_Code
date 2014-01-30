Ext.define('Mdc.model.UserFileReference', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'userFileReferenceId', type: 'int', useNull: true},
        {name:'name', type: 'string'}
    ],
    idProperty: 'userFileReferenceId'
});