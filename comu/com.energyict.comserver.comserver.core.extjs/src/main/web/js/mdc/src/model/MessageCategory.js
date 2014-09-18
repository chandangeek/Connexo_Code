Ext.define('Mdc.model.MessageCategory', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'description', type: 'string'}
    ]
});