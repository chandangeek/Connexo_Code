Ext.define('Apr.model.ExportPath', {
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
        url: '/api/export/exportdirs',
        reader: {
            type: 'json'
        }
    }
});
