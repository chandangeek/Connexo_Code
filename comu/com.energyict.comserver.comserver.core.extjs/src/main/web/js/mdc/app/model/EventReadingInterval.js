Ext.define('Mdc.model.EventReadingInterval', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'start', type:'date', dateFormat: 'time', useNull: true},
        {name: 'end', type:'date', dateFormat: 'time', useNull: true}
    ]
});
