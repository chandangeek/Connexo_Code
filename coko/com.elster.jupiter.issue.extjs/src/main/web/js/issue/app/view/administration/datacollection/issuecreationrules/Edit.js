Ext.define('Isu.view.administration.datacollection.issuecreationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.datacollection.issuecreationrules.ActionsList'
    ],
    alias: 'widget.issues-creation-rules-edit',
    content: [
        {
            name: 'pageTitle',
            ui: 'large',
            items: [
                {
                    xtype: 'form',
                    defaults: {
                        labelWidth: 150,
                        validateOnChange: false,
                        validateOnBlur: false,
                        width: 700
                    },
                    items: [
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
                            fieldLabel: 'Name',
                            required: true,
                            allowBlank: false,
                            maxLength: 80
                        },
                        {
                            itemId: 'issueType',
                            xtype: 'combobox',
                            name: 'issueType',
                            fieldLabel: 'Issue type',
                            required: true,
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
                            required: true,
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
                            defaults: {
                                labelWidth: 150,
                                margin: '0 0 10 0',
                                validateOnChange: false,
                                validateOnBlur: false,
                                width: 700
                            }
                        },
                        {
                            itemId: 'issueReason',
                            xtype: 'combobox',
                            name: 'reason',
                            fieldLabel: 'Issue reason',
                            required: true,
                            store: 'Isu.store.IssueReason',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'Due date',
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
                                                    fn: function () {
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
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('issues-creation-rules-edit [boxLabel=Due in]')[0];
                                                        radioButton.setValue(true);
                                                    }
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
                            fieldLabel: 'Comment',
                            emptyText: 'Provide a comment (optionally)',
                            height: 100
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'Actions',
                            width: 900,
                            items: [
                                {
                                    xtype: 'button',
                                    itemId: 'addAction',
                                    text: 'Add action',
                                    action: 'addAction',
                                    ui: 'action',
                                    margin: '0 0 10 0'
                                },
                                {
                                    xtype: 'issues-creation-rules-actions-list',
                                    hidden: true
                                },
                                {
                                    name: 'noactions',
                                    html: 'There are no actions added yet to this rule',
                                    hidden: true
                                }
                            ]
                        },
                        {
                            xtype: 'fieldcontainer',
                            ui: 'actions',
                            fieldLabel: '&nbsp',
                            defaultType: 'button',
                            items: [
                                {
                                    itemId: 'ruleAction',
                                    name: 'ruleAction',
                                    ui: 'action',
                                    action: 'save'
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