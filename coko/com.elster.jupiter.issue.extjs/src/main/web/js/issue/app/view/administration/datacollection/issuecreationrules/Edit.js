Ext.define('Isu.view.administration.datacollection.issuecreationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.datacollection.issuecreationrules.ActionsList'
    ],
    alias: 'widget.issues-creation-rules-edit',
    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    xtype: 'panel',
                    name: 'pageTitle',
                    margin: '0 0 40 0'
                },
                {
                    xtype: 'form',
                    width: '70%',
                    defaults: {
                        labelWidth: 150,
                        margin: '0 0 20 0',
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            hidden: true,
                            margin: '0 0 20 50'
                        },
                        {
                            xtype: 'textfield',
                            name: 'name',
                            fieldLabel: 'Name',
                            labelSeparator: ' *',
                            allowBlank: false,
                            maxLength: 80
                        },
                        {
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
                            xtype: 'combobox',
                            name: 'template',
                            fieldLabel: 'Rule template',
                            labelSeparator: ' *',
                            store: 'Isu.store.CreationRuleTemplate',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'uid',
                            allowBlank: false,
                            editable: false,
                            margin: 0
                        },
                        {
                            xtype: 'container',
                            name: 'templateDetails',
                            defaults: {
                                labelWidth: 150,
                                labelAlign: 'right',
                                margin: '20 0 0 0',
                                msgTarget: 'under',
                                validateOnChange: false,
                                validateOnBlur: false
                            }
                        },
                        {
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
                                    xtype: 'component',
                                    html: '<b>Due date</b>',
                                    width: 150,
                                    style: 'margin-right: 5px',
                                    cls: 'x-form-item-label uni-form-item-bold x-form-item-label-right'
                                },
                                {
                                    xtype: 'numberfield',
                                    name: 'dueIn.number',
                                    minValue: 1,
                                    width: 60,
                                    margin: '0 10 0 10'
                                },
                                {
//                                    xtype: 'combobox',
//                                    name: 'dueIn.type',
//                                    store: 'Isu.store.DueinType',
//                                    queryMode: 'local',
//                                    displayField: 'name',
//                                    valueField: 'name',
//                                    editable: false,
//                                    width: 100
                                    xtype: 'container',
                                    layout: {
                                        type: 'hbox'
                                    },
                                    items: [
                                        {
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
                                                    boxLabel: 'No due date',
                                                    inputValue: false
                                                },
                                                {
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
                                            xtype: 'container',
                                            name: 'dueDateValues',
                                            layout: {
                                                type: 'hbox'
                                            },
                                            margin: '26 0 0 -30',
                                            items: [
                                                {
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
                            xtype: 'component',
                            html: '<b>Actions</b>',
                            width: 150,
                            style: 'margin-right: 5px',
                            cls: 'x-form-item-label uni-form-item-bold x-form-item-label-right'
                        },
                        {
                            xtype: 'toolbar',
                            border: false,
                            padding: 0,
                            items: [
                                {
                                    text: 'Add action',
                                    disabled: true
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'issues-creation-rules-actions-list',
                    margin: '15 20 0 150'
                },
                {
                    xtype: 'container',
                    layout: 'hbox',
                    defaultType: 'button',
                    margin: '20 0',
                    items: [
                        {
                            name: 'ruleAction',
                            formBind: false
                        },
                        {
                            text: 'Cancel',
                            ui: 'link',
                            name: 'cancel',
                            cls: 'isu-btn-link',
                            hrefTarget: '',
                            href: '#/issue-administration/issuecreationrules'
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