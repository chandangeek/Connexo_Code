Ext.define('Usr.model.Privilege', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'applicationName',
        {
            name: 'selected',
            type: 'boolean',
            defaultValue: false
        }
    ],
    idProperty: 'name'
});