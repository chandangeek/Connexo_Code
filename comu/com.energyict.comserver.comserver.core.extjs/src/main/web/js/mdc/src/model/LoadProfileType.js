Ext.define('Mdc.model.LoadProfileType', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'obisCode', type: 'string'},
        {name:'timeDuration', type: 'auto'},
        {name:'measurementTypes', type: 'auto'}
    ]
});