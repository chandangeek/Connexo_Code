Ext.define('Isu.view.creationrules.EditForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Isu.view.creationrules.ActionsList',
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Isu.model.CreationRuleTemplate'
    ],
    alias: 'widget.issues-creation-rule-edit-form',
    router: null,
    isEdit: false,
    defaults: {
        labelWidth: 260,
        width: 595,
        validateOnChange: false,
        validateOnBlur: false
    },
    initComponent: function () {
        var me = this;

        me.title = me.isEdit ? ' ' : Uni.I18n.translate('general.issueCreationRules.add', 'ISU', 'Add issue creation rule');
        me.items = [
            {
                itemId: 'form-errors',
                xtype: 'uni-form-error-message',
                name: 'form-errors',
                hidden: true
            },
            {
                itemId: 'name',
                xtype: 'textfield',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.title.name', 'ISU', 'Name'),
                required: true,
                allowBlank: false,
                maxLength: 80
            },
            {
                itemId: 'issueType',
                xtype: 'combobox',
                name: 'issueType',
                fieldLabel: Uni.I18n.translate('general.title.issueType', 'ISU', 'Issue type'),
                required: true,
                store: 'Isu.store.IssueTypes',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'uid',
                forceSelection: true,
                listeners: {
                    change: {
                        fn: Ext.bind(me.onTypeChange, me)
                    }
                }
            },
            {
                itemId: 'rule-template-container',
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.title.ruleTemplate', 'ISU', 'Rule template'),
                required: true,
                layout: 'hbox',
                width: 650,
                items: [
                    {
                        itemId: 'ruleTemplate',
                        xtype: 'combobox',
                        name: 'template',
                        store: 'Isu.store.CreationRuleTemplates',
                        queryMode: 'local',
                        displayField: 'displayName',
                        valueField: 'name',
                        width: 320,
                        forceSelection: true,
                        listeners: {
                            change: {
                                fn: Ext.bind(me.onTemplateChange, me)
                            }
                        }
                    },
                    {
                        itemId: 'rule-template-info',
                        xtype: 'displayfield',
                        margin: '0 0 -8 10',
                        htmlEncode: false,
                        hidden: true,
                        value: '<div class="uni-icon-info-small" style="width: 16px; height: 16px;"></div>',
                        setTooltip: function (tooltip) {
                            if (tooltip) {
                                this.getEl().down('.uni-icon-info-small').set({
                                    'data-qtip': tooltip
                                });
                                this.show();
                            } else {
                                this.hide();
                            }
                        }
                    }
                ]
            },
            {
                itemId: 'property-form',
                xtype: 'property-form',
                width: 1000,
                defaults: {
                    labelWidth: me.defaults.labelWidth,
                    width: 320,
                    resetButtonHidden: true
                }
            },
            {
                itemId: 'issueReason',
                xtype: 'combobox',
                name: 'reason',
                fieldLabel: Uni.I18n.translate('general.title.issueReason', 'ISU', 'Issue reason'),
                required: true,
                store: 'Isu.store.IssueReasons',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                forceSelection: true
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'issues-creation-rules-edit-field-container-due-date',
                fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                layout: 'hbox',
                items: [
                    {
                        itemId: 'dueDateTrigger',
                        xtype: 'radiogroup',
                        name: 'dueDateTrigger',
                        formBind: false,
                        columns: 1,
                        vertical: true,
                        width: 100,
                        defaults: {
                            name: 'dueDate',
                            formBind: false,
                            submitValue: false
                        },
                        items: [
                            {
                                itemId: 'noDueDate',
                                boxLabel: Uni.I18n.translate('issueCreationRules.noDueDate', 'ISU', 'No due date'),
                                inputValue: false
                            },
                            {
                                itemId: 'dueIn',
                                boxLabel: Uni.I18n.translate('general.title.dueIn', 'ISU', 'Due in'),
                                inputValue: true
                            }
                        ],
                        listeners: {
                            change: {
                                fn: Ext.bind(me.dueDateTrigger, me)
                            }
                        }
                    },
                    {
                        itemId: 'dueDateValues',
                        xtype: 'container',
                        name: 'dueDateValues',
                        margin: '30 0 10 0',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                itemId: 'dueIn.number',
                                xtype: 'numberfield',
                                name: 'dueIn.number',
                                minValue: 1,
                                width: 60,
                                margin: '0 10 0 0',
                                listeners: {
                                    focus: {
                                        fn: Ext.bind(me.chooseDueInRadio, me)
                                    },
                                    change: {
                                        fn: function (field, newValue) {
                                            if (newValue < 0) {
                                                field.setValue(Math.abs(newValue));
                                            } else if (newValue > Math.pow(10,12)) {
                                                field.setValue(Math.pow(10,12));
                                            }
                                        }
                                    }
                                }
                            },
                            {
                                itemId: 'dueIn.type',
                                xtype: 'combobox',
                                name: 'dueIn.type',
                                store: 'Isu.store.DueinTypes',
                                queryMode: 'local',
                                displayField: 'displayValue',
                                valueField: 'name',
                                editable: false,
                                width: 100,
                                listeners: {
                                    focus: {
                                        fn: Ext.bind(me.chooseDueInRadio, me)
                                    }
                                }
                            }
                        ]
                    }
                ]
            },
            {
                itemId: 'comment',
                xtype: 'textareafield',
                name: 'comment',
                fieldLabel: Uni.I18n.translate('general.comment', 'ISU', 'Comment'),
                emptyText: Uni.I18n.translate('general.provideComment','ISU','Provide a comment (optionally)'),
                height: 160
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
                itemId: 'issues-creation-rules-edit-field-container-actions',
                width: 1000,
                layout: 'hbox',
                items: [
                    {
                        xtype: 'issues-creation-rules-actions-list',
                        itemId: 'issues-creation-rules-actions-grid',
                        width: 500,
                        padding: 0,
                        maxHeight: 323,
                        hidden: true
                    },
                    {
                        xtype: 'displayfield',
                        name: 'noactions',
                        itemId: 'issues-creation-rule-no-actions',
                        value: Uni.I18n.translate('issueCreationRules.noActionsAddedYet', 'ISU', 'There are no actions added yet to this rule')
                    },
                    {
                        xtype: 'button',
                        itemId: 'addAction',
                        text: Uni.I18n.translate('general.addAction', 'ISU', 'Add action'),
                        action: 'addAction',
                        margin: '0 0 0 10'
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                ui: 'actions',
                fieldLabel: ' ',
                defaultType: 'button',
                items: [
                    {
                        itemId: 'ruleAction',
                        text: me.isEdit ? Uni.I18n.translate('general.save', 'ISU', 'Save') : Uni.I18n.translate('general.add', 'ISU', 'Add'),
                        ui: 'action',
                        action: 'save'
                    },
                    {
                        itemId: 'cancel',
                        text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                        ui: 'link',
                        href: me.router.getRoute('administration/creationrules').buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            templateCombo = me.down('[name=template]'),
            typeCombo = me.down('[name=issueType]'),
            actionsGrid = me.down('issues-creation-rules-actions-list'),
            dueIn = record.get('dueIn'),
            actions = record.actions(),
            template;

        typeCombo.suspendEvent('change');
        templateCombo.suspendEvent('change');
        me.callParent(arguments);
        me.down('property-form').loadRecord(record);

        Ext.suspendLayouts();
        if (me.isEdit) {
            me.setTitle(Uni.I18n.translate('administration.issueCreationRules.title.editIssueCreationRule', 'ISU', 'Edit \'{0}\'', [record.get('name')]));
        }
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
        if (dueIn.number) {
            me.down('#dueDateTrigger').setValue({dueDate: true});
            me.down('[name=dueIn.number]').setValue(dueIn.number);
            me.down('[name=dueIn.type]').setValue(dueIn.type || me.down('[name=dueIn.type]').getStore().getAt(0).get('name'));
        } else {
            me.down('#dueDateTrigger').setValue({dueDate: false});
        }
        if (actions.count()) {
            actionsGrid.bindStore(actions);
            actionsGrid.show();
            me.down('#issues-creation-rule-no-actions').hide();
        } else {
            actionsGrid.hide();
            me.down('#issues-creation-rule-no-actions').show();
        }
        template = templateCombo.findRecordByValue(templateCombo.getValue());
        if (template) {
            me.down('#rule-template-info').setTooltip(template.get('description'));
        }
        me.updateLayout();
        Ext.resumeLayouts(true);

        typeCombo.resumeEvent('change');
        templateCombo.resumeEvent('change');
    },

    updateRecord: function () {
        var me = this,
            propertyForm = me.down('property-form'),
            record;

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
        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            record.propertiesStore = propertyForm.getRecord().properties();
        }
        if (me.down('#dueDateTrigger')) {
            record.set('dueIn', {
                number: me.down('[name=dueIn.number]').getValue(),
                type: me.down('[name=dueIn.type]').getValue()
            });
        } else {
            record.set('dueIn', null);
        }
        record.endEdit();
    },

    onTemplateChange: function (combo, newValue) {
        var me = this,
            template = combo.findRecordByValue(newValue);

        if (template) {
            me.down('#rule-template-info').setTooltip(template.get('description'));
            me.down('property-form').loadRecord(template);
            me.hideResetButtons();
        }
    },

    onTypeChange: function (combo, newValue) {
        var me = this,
            type = combo.findRecordByValue(newValue),
            templateCombo = me.down('[name=template]'),
            templatesStore = templateCombo.getStore(),
            issueReasonCombo = me.down('[name=reason]'),
            issueReasonsStore = issueReasonCombo.getStore(),
            counter = 2,
            callback = function () {
                counter--;
                if (!counter) {
                    me.setLoading(false);
                }
            };

        if (type) {
            Ext.suspendLayouts();
            templateCombo.reset();
            me.down('#rule-template-info').setTooltip(null);
            issueReasonCombo.reset();
            me.down('property-form').loadRecord(Ext.create('Isu.model.CreationRuleTemplate'));
            me.hideResetButtons();
            Ext.resumeLayouts(true);
            me.setLoading();
            templatesStore.getProxy().setExtraParam('issueType', type.getId());
            templatesStore.load(callback);
            issueReasonsStore.getProxy().setExtraParam('issueType', type.getId());
            issueReasonsStore.load(callback);
        }
    },

    dueDateTrigger: function (radioGroup, newValue) {
        var me = this,
            dueDateValues = me.down('[name=dueDateValues]'),
            dueInNumberField = me.down('[name=dueIn.number]'),
            dueInTypeField = me.down('[name=dueIn.type]');

        if (!newValue.dueDate) {
            dueInNumberField.reset();
            dueInTypeField.setValue(dueInTypeField.getStore().getAt(0).get('name'));
        }
    },

    chooseDueInRadio: function () {
        var me = this;

        me.down('#dueDateTrigger').setValue({dueDate: true});
    },

    hideResetButtons: function() {
        var buttons = Ext.ComponentQuery.query("uni-default-button");
        Ext.Array.each(buttons, function(item) {
            item.up('container').resetButtonHidden = true;
            item.hide();
        })
    }
});