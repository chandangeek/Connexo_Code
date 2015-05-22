Ext.define('Idv.model.Issue', {
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
        url: '/api/idc/issue',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});