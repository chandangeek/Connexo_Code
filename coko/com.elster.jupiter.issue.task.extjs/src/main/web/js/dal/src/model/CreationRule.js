Ext.define('Itk.model.CreationRule', {
    extend: 'Uni.model.Version',
    requires: [
        'Itk.model.CreationRuleAction',
        'Uni.property.model.Property',
        'Itk.model.CreationRuleTemplate',
        'Itk.model.IssueReason'
    ],
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int',
            useNull: true
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'comment',
            type: 'string'
        },
        {
            name: 'priority',
            type: 'auto'
        },
        {
            name: 'creationDate',
            dateFormat: 'time',
            type: 'date',
            persist: false
        },
        {
            name: 'modificationDate',
            dateFormat: 'time',
            type: 'date',
            persist: false
        },
        {
            name: 'version',
            type: 'int'
        },
        {
            name: 'dueIn',
            type: 'auto'
        },
        {
            name: 'title',
            persist: false,
            mapping: 'name'
        },
        {
            name: 'reason_name',
            persist: false,
            mapping: 'reason.name'
        },
        {
            name: 'template_name',
            persist: false,
            mapping: 'template.displayName'
        },
        {
            name: 'active'
        }
    ],

    associations: [
        {
            name: 'actions',
            type: 'hasMany',
            model: 'Itk.model.CreationRuleAction',
            associationKey: 'actions'
        },
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
        },
        {
            type: 'hasOne',
            model: 'Itk.model.CreationRuleTemplate',
            associatedName: 'template',
            associationKey: 'template',
            getterName: 'getTemplate',
            setterName: 'setTemplate'
        },
        {
            type: 'hasOne',
            model: 'Itk.model.IssueReason',
            associatedName: 'reason',
            associationKey: 'reason',
            getterName: 'getReason',
            setterName: 'setReason'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/itk/creationrules',
        reader: {
            type: 'json'
        }
    },

    // the method has been overridden because ExtJs has a bug:
    // auto generated associationId is equals for actions.properties() item and properties() item
    // which causes properties data is missed
    prepareAssociatedData: function () {
        var me = this,
            result = me.callParent(arguments),
            associatedStore = me.propertiesStore,
            properties = [],
            associatedRecords,
            associatedRecordCount,
            associatedRecord,
            j;

        if (associatedStore && associatedStore.getCount() > 0) {
            associatedRecords = associatedStore.data.items;
            associatedRecordCount = associatedRecords.length;

            for (j = 0; j < associatedRecordCount; j++) {
                associatedRecord = associatedRecords[j];
                properties[j] = Ext.merge(associatedRecord.getWriteData(false,arguments[2]), associatedRecord.getAssociatedData());
            }
        }

        result.properties = properties;

        return result;
    }
});
