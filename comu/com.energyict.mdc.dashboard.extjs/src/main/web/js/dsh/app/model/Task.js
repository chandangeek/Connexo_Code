Ext.define('Dsh.model.Task', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'string' },
        { name: 'title', type: 'string' },
        { name: 'protocol_dialect', type: 'string' },
        { name: 'urgency', type: 'string' },
        { name: 'allow_execute_on_inbound', type: 'string' },
        { name: 'startDateTime', type: 'string' },
        { name: 'endDateTime', type: 'string' },
        { name: 'result', type: 'string' },
        'device'
    ],
    hasOne: [
        {
            model: 'Dsh.model.Device',
            name: 'device'
        },
        {
            model: 'Dsh.model.Schedule',
            name: 'schedule'
        }
    ]
});
