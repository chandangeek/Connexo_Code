Ext.define('Apr.model.ImportPath', {
    extend: 'Ext.data.Model',
    fields: [
        'appServerName', 'directory',
        {
            name: 'id',
            persist: false,
            mapping: function (data) {
                return data.appServerName;
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/export/exportdirs',/*will be import rest*/
        reader: {
            type: 'json'
        }
    }
});
