Ext.define('Isu.view.administration.datacollection.issuecreationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
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
                                    html: '<b>Due in</b>',
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
                                    xtype: 'combobox',
                                    name: 'dueIn.type',
                                    store: 'Isu.store.DueinType',
                                    queryMode: 'local',
                                    displayField: 'name',
                                    valueField: 'name',
                                    editable: false,
                                    width: 100
                                }
                            ]
                        },
                        {
                            xtype: 'textareafield',
                            name: 'comment',
                            fieldLabel: 'Comment'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    layout: 'hbox',
                    defaultType: 'button',
                    margin: '20 0 0 20',
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
                            href: '#/issue-administration/datacollection/issuecreationrules'
                        }
                    ]
                }
            ]
        }
    ]
});