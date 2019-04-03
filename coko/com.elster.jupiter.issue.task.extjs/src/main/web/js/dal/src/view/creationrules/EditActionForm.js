/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.creationrules.EditActionForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Itk.store.CreationRuleActions',
        'Itk.store.Clipboard',
        'Itk.model.Action'
    ],
    alias: 'widget.issues-creation-rules-edit-action-form',
    isEdit: false,
    returnLink: null,
    ui: 'large',
    defaults: {
        labelWidth: 260
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'form-errors',
                xtype: 'uni-form-error-message',
                hidden: true,
                width: 595
            },
            {
                xtype: 'radiogroup',
                itemId: 'phasesRadioGroup',
                name: 'phase',
                fieldLabel: Uni.I18n.translate('issueCreationRules.actions.whenToPerform', 'ITK', 'When to perform'),
                required: true,
                columns: 1,
                vertical: true,
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
                fieldLabel: Uni.I18n.translate('general.action', 'ITK', 'Action'),
                required: true,
                store: 'Itk.store.CreationRuleActions',
                queryMode: 'local',
                displayField: 'name',
                width: 595,
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
                xtype: 'displayfield',
                itemId: 'no-actions-displayfield',
                fieldLabel: Uni.I18n.translate('general.action', 'ITK', 'Action'),
                value: Uni.I18n.translate('issueCreationRules.actions.noActionsDefined', 'ITK', 'No actions defined'),
                fieldStyle: 'color: #eb5642',
                required: true,
                hidden: true
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
                        text: me.isEdit ? Uni.I18n.translate('general.save', 'ITK', 'Save') : Uni.I18n.translate('general.add', 'ITK', 'Add'),
                        action: 'saveRuleAction'
                    },
                    {
                        itemId: 'cancel',
                        text: Uni.I18n.translate('general.cancel', 'ITK', 'Cancel'),
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
                afterSubTpl: '<span style="display: inline-block; color: #686868; font-style: italic; margin-left: 19px; margin-top: 6px;">' + record.get('description') + '</span>'
            });
        });
        phasesRadioGroup.setValue({phase: records[0].get('uuid')});
        Ext.resumeLayouts(true);
    },

    onPhaseChange: function (radioGroup, newValue) {
        var me = this,
            actionTypesStore = Ext.getStore('Itk.store.CreationRuleActions'),
            actionTypesStoreProxy = actionTypesStore.getProxy(),
            rule = Ext.getStore('Itk.store.Clipboard').get('issuesCreationRuleState');

        Ext.suspendLayouts();
        me.down('[name=type]').reset();
        me.down('property-form').loadRecord(Ext.create('Itk.model.Action'));
        me.setLoading();
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
            me.down('#no-actions-displayfield').setVisible(!actionTypesStore.count());
            me.down('#actionType').setVisible(actionTypesStore.count());
            me.down('#actionOperation').setDisabled(!actionTypesStore.count());
            me.setLoading(false);
        });
        Ext.resumeLayouts(true);
    },

    onActionChange:  function (combo, newValue) {
        var me = this,
            action = combo.findRecordByValue(newValue);

        if (action) {
            me.down('property-form').loadRecord(action);
        }
    }
});