Ext.define('Isu.model.CommunicationTasksProfiles', {
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
            name: 'obisCode',
            type: 'string'
        },
        {
            name: 'timeDuration',
            type: 'auto'
        },
        {
            name: 'measurementTypes',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/mds/loadprofiles',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

