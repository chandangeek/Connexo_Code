Ext.define('Mdc.model.CodeTable', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'codeTableId', type: 'int', useNull: true},
        {name:'name', type: 'string'}
    ],
    idProperty: 'codeTableId'
});