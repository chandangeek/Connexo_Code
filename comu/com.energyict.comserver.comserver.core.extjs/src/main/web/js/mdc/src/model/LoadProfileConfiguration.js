Ext.define('Mdc.model.LoadProfileConfiguration', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'obisCode', type: 'string'},
        {name:'overruledObisCode', type: 'string'},
        {name:'timeDuration', type: 'auto'},
        {name:'channels', type: 'auto'}
    ]
});