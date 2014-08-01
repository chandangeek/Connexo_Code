Ext.define('Usr.model.Privilege', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        {
            name: 'selected',
            type: 'boolean',
            defaultValue: false
        }
    ],
    idProperty: 'id'
});