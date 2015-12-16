Ext.define('Idc.model.Issue', {
    extend: 'Isu.model.Issue',

    fields: [
        'deviceMRID',
        'comTaskId',
        'comTaskSessionId',
        'connectionTaskId',
        'comSessionId'
    ],

    proxy: {
        type: 'rest',
        url: '/api/idc/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});