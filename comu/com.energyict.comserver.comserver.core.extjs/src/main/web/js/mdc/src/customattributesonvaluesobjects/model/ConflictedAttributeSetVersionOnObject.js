Ext.define('Mdc.customattributesonvaluesobjects.model.ConflictedAttributeSetVersionOnObject', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.DateTime.formatDateTimeShort',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject'
    ],

    fields: [
        {name: 'id', type: 'integer', useNull: true},
        {name: 'conflictType', type: 'string'},
        {name: 'editable', type: 'boolean'},
        {name: 'message', type: 'string'},
        {name: 'conflictAtStart', type: 'boolean'},
        {name: 'conflictAtEnd', type: 'boolean'},
        {
            name: 'startTime',
            type: 'timestamp',
            persist: false,
            mapping: function (data) {
                if (data.customPropertySet) {
                    return data.customPropertySet.startTime;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'endTime',
            type: 'timestamp',
            persist: false,
            mapping: function (data) {
                if (data.customPropertySet) {
                    return data.customPropertySet.endTime;
                } else {
                    return null;
                }

            }
        }
    ],

    associations: [
        {name: 'customPropertySet', type: 'hasMany', model: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject', associationKey: 'customPropertySet', foreignKey: 'customPropertySet',
            getTypeDiscriminator: function () {
                return 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject';
            }
        }
    ]
});