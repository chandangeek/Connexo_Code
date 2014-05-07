Ext.define('Isu.model.CommunicationTasks', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'commands',
            type: 'auto'
        },
        {
            name: 'commandsString',
            type: 'string',
            convert: function (value, record) {
                var str = '';

                Ext.Array.each(record.get('commands'), function (command) {
                    str += command.category + ' ' + command.action + '<br/>';
                });

                return str;
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/cts/comtasks',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});