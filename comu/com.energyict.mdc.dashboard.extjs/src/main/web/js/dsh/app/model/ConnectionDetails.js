Ext.define('Dsh.model.ConnectionDetails', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'auto' },
        { name: 'title', type: 'auto' },
        { name: 'direction', type: 'auto' },
        { name: 'type', type: 'auto' },
        { name: 'method', type: 'auto' },
        { name: 'strategy', type: 'auto' },
        { name: 'state', type: 'auto' },
        { name: 'startDateTime', type: 'date', dateFormat: 'time'},
        { name: 'endDateTime', type: 'date', dateFormat: 'time'},
        { name: 'duration', type: 'auto' },
        'task',
        'server',
        'device',
        {
            name: 'latestStatus',
            persist: false,
            mapping: function (data) {
                return 'under dev'
            }
        },
        {
            name: 'latestResult',
            persist: false,
            mapping: function (data) {
                return 'under dev'
            }
        },
        {
            name: 'commTasks',
            persist: false,
            mapping: function (data) {
                return 'under dev'
            }
        },
        {
            name: 'deviceTitle',
            persist: false,
            mapping: function (data) {
                return data.device.title
            }
        }
    ],
    hasMany: [
        {
            model: 'Dsh.model.Task',
            name: 'tasks'
        }
    ],
    hasOne: [
        {
            model: 'Dsh.model.Server',
            name: 'server'
        },
        {
            model: 'Dsh.model.Device',
            name: 'device'
        }
    ]
});
