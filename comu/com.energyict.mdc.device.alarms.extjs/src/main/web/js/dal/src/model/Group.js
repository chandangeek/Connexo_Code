Ext.define('Dal.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'description',
            type: 'text'
        },
        {
            name: 'number',
            type: 'int'
        },
        {
            name: 'href',
            persist: false
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/dal/alarms/groupedlist',
        reader: {
            type: 'json',
            root: 'alarmGroups'
        }
    }
});