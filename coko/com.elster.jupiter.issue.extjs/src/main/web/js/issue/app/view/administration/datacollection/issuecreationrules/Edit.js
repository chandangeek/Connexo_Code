Ext.define('Isu.view.administration.datacollection.issuecreationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.datacollection.issuecreationrules.ActionsList',
        'Isu.util.FormErrorMessage'
    ],
    alias: 'widget.issues-creation-rules-edit',
    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    xtype: 'panel',
                    name: 'pageTitle',
                    margin: '0 0 40 32',
                    ui: 'large',
                    title: 'Create issue creation rule'
                },
                {
                    xtype: 'form',
                    width: '75%',
                    bodyPadding: '0 30 0 0',
                    defaults: {
                        labelWidth: 150,
                        margin: '0 0 20 0',
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            hidden: true,
                            margin: '0 0 20 50'
                        },
                        {
                            itemId: 'name',
                            xtype: 'textfield',
                            name: 'name',
                            fieldLabel: 'Name',
                            labelSeparator: ' *',
                            allowBlank: false,
                            maxLength: 80
                        },
                        {
                            itemId: 'issueType',
                            xtype: 'combobox',
                            name: 'issueType',
                            fieldLabel: 'Issue type',
                            labelSeparator: ' *',
                            store: 'Isu.store.IssueType',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'uid',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            itemId: 'ruleTemplate',
                            xtype: 'combobox',
                            name: 'template',
                            fieldLabel: 'Rule template',
                            labelSeparator: ' *',
                            store: 'Isu.store.CreationRuleTemplate',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'uid',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            itemId: 'templateDetails',
                            xtype: 'container',
                            name: 'templateDetails',
                            layout: 'fit',
                            margin: 0,
                            anchor: '70%',
                            defaults: {
                                labelWidth: 150,
                                margin: '0 0 20 0',
                                validateOnChange: false,
                                validateOnBlur: false,
                                anchor: '100%'
                            }
                        },
                        {
                            itemId: 'issueReason',
                            xtype: 'combobox',
                            name: 'reason',
                            fieldLabel: 'Issue reason',
                            labelSeparator: ' *',
                            store: 'Isu.store.IssueReason',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            xtype: 'container',
                            layout: {
                                type: 'hbox'
                            },
                            items: [
                                {
                                    xtype: 'label',
                                    text: 'Due date',
                                    itemId: 'dueDate',
                                    cls: 'x-form-item-label x-form-item-label-right',
                                    width: 140
                                },
                                {
                                    xtype: 'container',
                                    margin: '0 16 0 26',
                                    layout: {
                                        type: 'hbox'
                                    },
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
                                                    boxLabel: 'No due date',
                                                    inputValue: false
                                                },
                                                {
                                                    itemId: 'dueIn',
                                                    boxLabel: 'Due in',
                                                    inputValue: true
                                                }
                                            ],
                                            listeners: {
                                                change: {
                                                    fn: function (radioGroup, newValue, oldValue) {
                                                        this.up('issues-creation-rules-edit').dueDateTrigger(radioGroup, newValue, oldValue);
                                                    }
                                                }
                                            }
                                        },
                                        {
                                            itemId: 'dueDateValues',
                                            xtype: 'container',
                                            name: 'dueDateValues',
                                            layout: {
                                                type: 'hbox'
                                            },
                                            margin: '32 0 0 16',
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
                                                            fn: function (field) {
                                                                var radioButton = Ext.ComponentQuery.query('issues-creation-rules-edit [boxLabel=Due in]')[0];
                                                                radioButton.setValue(true);
                                                            }
                                                        }
                                                    }
                                                },
                                                {
                                                    itemId: 'dueIn.type',
                                                    xtype: 'combobox',
                                                    name: 'dueIn.type',
                                                    store: 'Isu.store.DueinType',
                                                    queryMode: 'local',
                                                    displayField: 'displayValue',
                                                    valueField: 'name',
                                                    editable: false,
                                                    width: 100,
                                                    listeners: {
                                                        focus: {
                                                            fn: function (field) {
                                                                var radioButton = Ext.ComponentQuery.query('issues-creation-rules-edit [boxLabel=Due in]')[0];
                                                                radioButton.setValue(true);
                                                            }
                                                        }
                                                    }
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            itemId: 'comment',
                            xtype: 'textareafield',
                            name: 'comment',
                            fieldLabel: 'Comment',
                            emptyText: 'Provide a comment (optionally)'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            itemId: 'actionsL',
                            xtype: 'component',
                            html: '<b>Actions</b>',
                            width: 150,
                            cls: 'x-form-item-label uni-form-item-bold x-form-item-label-right'
                        },
                        {
                            itemId: 'actionToolbar',
                            xtype: 'toolbar',
                            border: false,
                            items: [
                                {
                                    itemId: 'addAction',
                                    text: 'Add action',
                                    disabled: true,
                                    ui: 'action',
                                    margin: '0 5 0 -5'
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'issues-creation-rules-actions-list',
                    margin: '15 20 0 165'
                },
                {
                    xtype: 'container',
                    layout: 'hbox',
                    defaultType: 'button',
                    margin: '20 165',
                    items: [
                        {
                            itemId: 'ruleAction',
                            name: 'ruleAction',
                            ui: 'action',
                            formBind: false
                        },
                        {
                            itemId: 'cancel',
                            text: 'Cancel',
                            ui: 'link',
                            name: 'cancel',
                            href: '#/administration/issue/creationrules'
                        }
                    ]
                }
            ]
        }
    ],

    dueDateTrigger: function (radioGroup, newValue) {
        var dueDateValues = this.down('form [name=dueDateValues]'),
            dueInNumberField = this.down('form [name=dueIn.number]'),
            dueInTypeField = this.down('form [name=dueIn.type]');

        if (!newValue.dueDate) {
            dueInNumberField.reset();
            dueInTypeField.setValue(dueInTypeField.getStore().getAt(0).get('name'));
        }
    }
});