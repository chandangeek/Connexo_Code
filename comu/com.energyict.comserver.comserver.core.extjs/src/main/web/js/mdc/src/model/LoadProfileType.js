Ext.define('Mdc.model.LoadProfileType', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'obisCode', type: 'string'},
        {name:'timeDuration', type: 'auto', useNull: true},
        {name:'registerTypes', type: 'auto'},
        {name:'isLinkedToActiveDeviceConf', type: 'boolean'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/mds/loadprofiles',
        reader: {
            type: 'json'
        }
    }
});