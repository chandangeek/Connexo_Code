Ext.define('Isu.model.CreationRule', {
    extend: 'Ext.data.Model',
    requires: [
        'Isu.model.CreationRuleAction',
        'Uni.property.model.Property',
        'Isu.model.IssueType',
        'Isu.model.CreationRuleTemplate',
        'Isu.model.IssueReason'
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
            name: 'issueType_name',
            persist: false,
            mapping: 'issueType.name'
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
            name: 'due_in',
            persist: false,
            mapping: function (data) {
                var dueIn = '';

                if (data.dueIn && data.dueIn.number) {
                    dueIn =   Uni.I18n.translatePlural('general.period.' + data.dueIn.type, data.dueIn.number, 'ISU', '{0} ' + data.dueIn.type);
                }

                return dueIn;
            }
        }
    ],

    associations: [
        {
            name: 'actions',
            type: 'hasMany',
            model: 'Isu.model.CreationRuleAction',
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
            model: 'Isu.model.IssueType',
            associatedName: 'issueType',
            associationKey: 'issueType',
            getterName: 'getIssueType',
            setterName: 'setIssueType'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.CreationRuleTemplate',
            associatedName: 'template',
            associationKey: 'template',
            getterName: 'getTemplate',
            setterName: 'setTemplate'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.IssueReason',
            associatedName: 'reason',
            associationKey: 'reason',
            getterName: 'getReason',
            setterName: 'setReason'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/creationrules',
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
