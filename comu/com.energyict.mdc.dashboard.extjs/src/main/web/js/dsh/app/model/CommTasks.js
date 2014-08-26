Ext.define('Dsh.model.CommTasks', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'count', type: 'auto' },
        'communicationTasks'
    ],
    hasMany: [
        {
            model: 'Dsh.model.CommunicationTask',
            name: 'communicationsTasks'
        }
    ]
});
