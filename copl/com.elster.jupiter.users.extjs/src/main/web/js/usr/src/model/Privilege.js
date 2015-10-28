Ext.define('Usr.model.Privilege', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'applicationName',
        'translatedName',
        'translatedApplicationName',
        {
            name: 'selected',
            type: 'boolean',
            defaultValue: false
        },
        {
            name: 'id', type: 'string', convert: function (value, record) {
            return record.get('name') +'.'+ record.get('applicationName')
        }
        }
    ],
    idProperty: 'id'
});