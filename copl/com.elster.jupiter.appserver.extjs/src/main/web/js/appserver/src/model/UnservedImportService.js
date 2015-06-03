Ext.define('Apr.model.UnservedImportService', {
    extend: 'Ext.data.Model',
    fields: [
        'id','name',
        {
            name: 'importService',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        }
    ]
});