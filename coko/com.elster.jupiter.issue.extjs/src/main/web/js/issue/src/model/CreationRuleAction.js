Ext.define('Isu.model.CreationRuleAction', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property',
        'Isu.model.Action'
    ],
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'phase',
            type: 'auto'
        }
    ],
    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.Action',
            associatedName: 'type',
            associationKey: 'type',
            getterName: 'getType',
            setterName: 'setType'
        }
    ]
});