Ext.define('Dal.model.BulkChangeAlarms', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'operation', type: 'string'},
        {name: 'status', type: 'string'},
        {name: 'comment', type: 'string'},
        {name: 'assignee', type: 'auto'}
    ],

    hasMany: {model: 'Dal.model.BulkAlarms', name: 'alarms'}
});
