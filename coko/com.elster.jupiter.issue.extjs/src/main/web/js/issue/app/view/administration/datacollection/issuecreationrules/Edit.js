Ext.define('Isu.view.administration.datacollection.issuecreationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.datacollection.issuecreationrules.EditActionsList'
    ],
    alias: 'widget.issues-creation-rules-edit',
    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Edit issue creation rule</h1>',
                    margin: '0 0 40 0'
                },
                {
                    xtype: 'form',
                    width: '70%',
                    defaults: {
                        labelWidth: 150,
                        labelAlign: 'right',
                        margin: '0 0 20 0',
                        anchor: '100%'
                    },
                    items: [
                        {
                            xtype: 'textfield',
                            name: 'name',
                            fieldLabel: 'Name *',
                            maxLength: 80
                        },
                        {
                            xtype: 'combobox',
                            name: 'type',
                            fieldLabel: 'Issue type *',
                            store: Ext.create('Ext.data.Store', {
                                fields: ['id', 'name'],
                                data: [
                                    {"id": "1", "name": "Data collection"}
                                ]
                            }),
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id'
                        },
                        {
                            xtype: 'combobox',
                            name: 'template',
                            fieldLabel: 'Rule template *',
                            store: Ext.create('Ext.data.Store', {
                                fields: ['abbr', 'name'],
                                data: [
                                    {"abbr": "AL", "name": "Alabama"},
                                    {"abbr": "AK", "name": "Alaska"},
                                    {"abbr": "AZ", "name": "Arizona"}
                                ]
                            }),
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'abbr'
                        },
                        {
                            xtype: 'component',
                            name: 'rule-template-description',
                            html: 'Unable to connect to meters of a concentrator. Threshold is configurable',
                            margin: '0 0 0 155'
                        },
                        {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'middle'
                            },
                            items: [
                                {
                                    xtype: 'textfield',
                                    name: 'parameters.threshold',
                                    fieldLabel: 'Threshold *',
                                    fieldStyle: 'text-align: right',
                                    labelWidth: 150,
                                    labelAlign: 'right',
                                    width: 200
                                },
                                {
                                    xtype: 'component',
                                    html: '%'
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
                                    store: Ext.create('Ext.data.Store', {
                                        fields: ['name'],
                                        data: [
                                            {"name": "days"},
                                            {"name": "weeks"},
                                            {"name": "months"}
                                        ]
                                    }),
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
                    xtype: 'component',
                    html: '<h3>Actions</h3>',
                    margin: '40 0 20 0'
                },
                {
                    xtype: 'issues-creation-rules-edit-actions-list',
                    margin: '0 15 0 0',
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'top',
                            items: [
                                {
                                    xtype: 'component',
                                    html: '2 actions',
                                    flex: 1
                                },
                                {
                                    xtype: 'button',
                                    text: 'Add action',
                                    action: 'add'
                                }
                            ]
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
                            text: 'Save',
                            name: 'save',
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