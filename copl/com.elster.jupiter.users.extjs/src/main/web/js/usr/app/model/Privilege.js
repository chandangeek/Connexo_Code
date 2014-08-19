Ext.define('Usr.model.Privilege', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        {
            name: 'selected',
            type: 'boolean',
            defaultValue: false
        }
    ],
    idProperty: 'name'
});