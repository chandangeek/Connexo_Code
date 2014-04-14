Ext.define('Isu.view.administration.datacollection.issuecreationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-creation-rules-edit',
    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    xtype: 'component',
                    name: 'pageTitle',
                    margin: '0 0 40 0'
                },
                {
                    xtype: 'form',
                    width: '70%',
                    defaults: {
                        labelWidth: 150,
                        labelAlign: 'right',
                        margin: '0 0 20 0',
                        msgTarget: 'under',
                        anchor: '100%'
                    },
                    items: [
                        {
                            xtype: 'component',
                            name: 'form-errors',
                            html: '<div class="isu-error-panel">There are errors on this page that require your attention.</div>',
                            hidden: true,
                            margin: '0 0 20 155'
                        },
                        {
                            xtype: 'textfield',
                            name: 'name',
                            fieldLabel: 'Name *',
                            allowBlank: false,
                            validateOnChange: false,
                            validateOnBlur: false,
                            maxLength: 80
                        },
                        {
                            xtype: 'combobox',
                            name: 'type',
                            fieldLabel: 'Issue type *',
                            store: 'Isu.store.IssueType',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            allowBlank: false,
                            validateOnChange: false,
                            validateOnBlur: false
                        },
                        {
                            xtype: 'combobox',
                            name: 'template',
                            fieldLabel: 'Rule template *',
                            store: 'Isu.store.CreationRuleTemplate',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'uid',
                            allowBlank: false,
                            validateOnChange: false,
                            validateOnBlur: false,
                            margin: 0
                        },
                        {
                            xtype: 'container',
                            name: 'templateDetails',
                            defaults: {
                                labelWidth: 150,
                                labelAlign: 'right',
                                margin: '20 0 0 0',
                                msgTarget: 'under'
                            }
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
                                    name: 'duein.number',
                                    minValue: 1,
                                    width: 60,
                                    margin: '0 10 0 0'
                                },
                                {
                                    xtype: 'combobox',
                                    name: 'duein.type',
                                    store: 'Isu.store.DueinType',
                                    queryMode: 'local',
                                    displayField: 'name',
                                    valueField: 'name',
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
                    margin: '20 0',
                    items: [
                        {
                            name: 'ruleAction',
                            formBind: false
                        },
                        {
                            text: 'Cancel',
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