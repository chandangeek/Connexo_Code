Ext.define('Isu.view.creationrules.EditActionForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Isu.store.CreationRuleActions',
        'Isu.store.Clipboard',
        'Isu.model.Action'
    ],
    alias: 'widget.issues-creation-rules-edit-action-form',
    isEdit: false,
    returnLink: null,
    defaults: {
        labelWidth: 260,
        width: 595
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'form-errors',
                xtype: 'uni-form-error-message',
                hidden: true
            },
            {
                xtype: 'radiogroup',
                itemId: 'phasesRadioGroup',
                name: 'phase',
                fieldLabel: Uni.I18n.translate('issueCreationRules.actions.whenToPerform', 'ISU', 'When to perform'),
                required: true,
                columns: 1,
                vertical: true,
                width: 700,
                listeners: {
                    change: {
                        fn: Ext.bind(me.onPhaseChange, me)
                    }
                }
            },
            {
                itemId: 'actionType',
                xtype: 'combobox',
                name: 'type',
                fieldLabel: Uni.I18n.translate('general.action', 'ISU', 'Action'),
                required: true,
                store: 'Isu.store.CreationRuleActions',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                forceSelection: true,
                allowBlank: false,
                listeners: {
                    change: {
                        fn: Ext.bind(me.onActionChange, me)
                    }
                }
            },
            {
                itemId: 'property-form',
                xtype: 'property-form',
                defaults: {
                    labelWidth: me.defaults.labelWidth,
                    width: 320,
                    resetButtonHidden: true
                }
            },
            {
                xtype: 'fieldcontainer',
                ui: 'actions',
                fieldLabel: ' ',
                defaultType: 'button',
                items: [
                    {
                        itemId: 'actionOperation',
                        ui: 'action',
                        text: me.isEdit ? Uni.I18n.translate('general.save', 'ISU', 'Save') : Uni.I18n.translate('general.add', 'ISU', 'Add'),
                        action: 'saveRuleAction'
                    },
                    {
                        itemId: 'cancel',
                        text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                        action: 'cancel',
                        ui: 'link',
                        href: me.returnLink
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;

        me.callParent(arguments);
        me.down('property-form').loadRecord(record);

        Ext.suspendLayouts();
        record.associations.each(function (association) {
            var field,
                associatedRecord;

            if (association.type === 'hasOne') {
                field = me.down('[name="' + association.associatedName + '"]');
                try {
                    associatedRecord = record[association.getterName].call(record);
                } catch (e) {}

                if (field && associatedRecord) {
                    field.setValue(associatedRecord.getId());
                }
            }
        });
        me.updateLayout();
        Ext.resumeLayouts(true);
    },

    updateRecord: function () {
        var me = this,
            propertyForm = me.down('property-form'),
            phaseField = me.down('[name=phase]'),
            record,
            phase;

        me.callParent(arguments);
        record = me.getRecord();
        record.beginEdit();
        record.associations.each(function (association) {
            var combo,
                value = null;

            if (association.type === 'hasOne') {
                combo = me.down('[name="' + association.associatedName + '"]');
                if (combo) {
                    value = combo.findRecordByValue(combo.getValue());
                }
                record[association.setterName].call(record, value);
            }
        });
        phase = phaseField.getValue();
        record.set('phase', {
            uuid: phase.phase,
            title: phaseField.down('#when-to-perform-radio-button-' + phase.phase).boxLabel
        });
        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            record.propertiesStore = propertyForm.getRecord().properties();
        }
        record.endEdit();
        record.commit();
    },

    addPhases: function (records) {
        var me = this,
            phasesRadioGroup = me.down('#phasesRadioGroup');

        Ext.suspendLayouts();
        Ext.Array.each(records, function (record, index) {
            phasesRadioGroup.add({
                boxLabel: record.get('title'),
                name: 'phase',
                inputValue: record.get('uuid'),
                itemId: 'when-to-perform-radio-button-'+record.get('uuid'),
                afterSubTpl: '<span style="color: #686868; font-style: italic; margin-left: 19px">' + record.get('description') + '</span>'
            });
        });
        phasesRadioGroup.setValue({phase: records[0].get('uuid')});
        Ext.resumeLayouts(true);
    },

    onPhaseChange: function (radioGroup, newValue) {
        var me = this,
            actionTypesStore = Ext.getStore('Isu.store.CreationRuleActions'),
            actionTypesStoreProxy = actionTypesStore.getProxy(),
            rule = Ext.getStore('Isu.store.Clipboard').get('issuesCreationRuleState');

        me.down('[name=type]').reset();
        me.down('property-form').loadRecord(Ext.create('Isu.model.Action'));
        me.setLoading();
        actionTypesStoreProxy.setExtraParam('issueType', rule.getIssueType().getId());
        actionTypesStoreProxy.setExtraParam('createdActions', _.map(rule.actions().getRange(), function (value) {
            if (value.get('phase').uuid === newValue.phase) {
                return value.getType().getId();
            }
        }));
        actionTypesStoreProxy.setExtraParam('phase', newValue.phase);
        try {
            actionTypesStoreProxy.setExtraParam('reason', rule.getReason().getId());
        } catch (e) {}
        actionTypesStore.load(function () {
            me.setLoading(false);
        });
    },

    onActionChange:  function (combo, newValue) {
        var me = this,
            action = combo.findRecordByValue(newValue);

        if (action) {
            me.down('property-form').loadRecord(action);
        }
    }
});