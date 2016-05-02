Ext.define('Mdc.model.Interval', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'asSeconds', type: 'int'}
    ]
});