Ext.define('Isu.model.CreationRuleTemplate', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'displayName',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        }
    ],

    idProperty: 'name',

    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/templates',
        reader: {
            type: 'json'
        }
    }
});
