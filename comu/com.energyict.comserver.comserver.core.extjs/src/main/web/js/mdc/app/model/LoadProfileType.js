Ext.define('Mdc.model.LoadProfileType', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'loadProfileTypeId', type: 'int', useNull: true},
        {name:'name', type: 'string'}
    ],
    idProperty: 'loadProfileTypeId'
});