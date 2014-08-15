Ext.define('Dsh.model.Schedule', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'title', type: 'string'},
        { name: 'expression', type: 'string'},
        { name: 'startDateTime', type: 'date', dateFormat: 'time'},
        { name: 'nextOccurence', type: 'date', dateFormat: 'time'}
    ]
});

