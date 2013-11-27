Ext.define('Cfg.view.validation.Edit', {
    extend: 'Ext.window.Window',
    alias: 'widget.validationrulesetEdit',
    title: 'Edit Rule Set',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    width: 1000,
    height: 800,
    modal: true,
    constrain: true,
    autoShow: true,
    requires: [
        'Ext.grid.*'    ,
        'Cfg.store.ValidationActions',
        'Cfg.store.Validators',
        'Cfg.store.ValidationRuleProperties',
        'Cfg.store.ReadingTypesForRule',
        'Cfg.store.AvailableReadingTypes'
    ],

    initComponent: function () {
        var cellEditing = new Ext.grid.plugin.CellEditing({
            clicksToEdit: 1
        });
        var cellEditing2 = new Ext.grid.plugin.CellEditing({
            clicksToEdit: 1
        });

        this.buttons = [
            {
                text: 'Clone',
                action: 'clone'
            },
            {
                text: 'Save',
                action: 'save'
            },
            {
                text: 'Cancel',
                scope: this,
                handler: this.close
            }
        ];

        var columns = [
            { header: 'Name', dataIndex: 'name'}
        ];


        this.items = [
            {
                xtype: 'form',
                border: false,
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    anchor: '100%',
                    margins: '0 0 5 0'
                },

                items: [
                    {
                        xtype: 'textfield',
                        name: 'id',
                        fieldLabel: 'Id',
                        readOnly: true
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        fieldLabel: 'Name'
                    },
                    {
                        xtype: 'textfield',
                        name: 'description',
                        fieldLabel: 'Description',
                        margin: '0'
                    }
                ]
            },
            {
                xtype: 'fieldset',
                title: 'Rules',
                margin: '0 10 10 10',
                padding: '0 5 5 5',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                flex: 1,
                items: [
                    {
                        xtype: 'gridpanel',
                        title: 'Rules',
                        itemId: 'validationruleList',
                        flex: 1,
                        store: 'ValidationRules',
                        selType: 'rowmodel',
                        selModel: {
                            mode: 'SINGLE'
                        },
                        plugins: [cellEditing],
                        columns: {
                            defaults: {
                                flex: 1
                            },
                            items: [
                                { header: 'Id', dataIndex: 'id', flex: 0.20 },
                                { header: 'Active', dataIndex: 'active', flex: 0.20, xtype: 'checkcolumn',
                                    editor: {
                                        xtype: 'checkbox',
                                        cls: 'x-grid-checkheader-editor'
                                    } } ,
                                {   header: 'Action',
                                    dataIndex: 'action',
                                    flex: 0.3,
                                    editor: new Ext.form.field.ComboBox({
                                        typeAhead: true,
                                        queryMode: 'local',
                                        displayField: 'action',
                                        valueField: 'action',
                                        triggerAction: 'all',
                                        store: Ext.create('Cfg.store.ValidationActions')
                                    })
                                },
                                { header: 'Validation', dataIndex: 'implementation',
                                    editor: new Ext.form.field.ComboBox({
                                        typeAhead: true,
                                        queryMode: 'local',
                                        displayField: 'implementation',
                                        valueField: 'implementation',
                                        triggerAction: 'all',
                                        store: Ext.create('Cfg.store.Validators')
                                    })
                                }
                            ]
                        }
                    },
                    {
                        xtype: 'fieldset',
                        title: 'Properties',
                        margin: '0 10 10 10',
                        padding: '0 5 5 5',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        flex: 1,
                        items: [
                            {
                                xtype: 'gridpanel',
                                title: 'Properties',
                                itemId: 'validationrulepropertiesList',
                                flex: 1,
                                store: 'ValidationRuleProperties',
                                plugins: [cellEditing2],
                                columns: {
                                    defaults: {
                                        flex: 1
                                    },
                                    items: [
                                        { header: 'Name', dataIndex: 'name'},
                                        { header: 'Value', dataIndex: 'value'}
                                    ]
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'fieldset',
                        title: 'Reading Types',
                        margin: '0 10 10 10',
                        padding: '0 5 5 5',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        flex: 1,
                        items: [
                            {
                                xtype: 'gridpanel',
                                title: 'Available',
                                itemId: 'availableReadingTypes',
                                flex: 1,
                                multiSelect: true,
                                /*viewConfig: {
                                    plugins: {
                                        ptype: 'gridviewdragdrop',
                                        dragGroup: availableGroup,
                                        dropGroup: activeGroup
                                    }
                                },  */
                                store: new Ext.data.Store({
                                    model: 'Cfg.model.ReadingType'
                                }),
                                data: [],
                                columns: columns
                            },
                            {
                                xtype: 'container',
                                itemId: 'readingTypesActions',
                                layout: {
                                    type: 'vbox',
                                    align: 'center',
                                    pack: 'center'
                                },
                                defaults: {
                                    margin: '5'
                                },
                                items: [
                                    {
                                        xtype: 'button',
                                        action: 'activate',
                                        glyph: 'xe015@icomoon'
                                    },
                                    {
                                        xtype: 'button',
                                        action: 'deactivate',
                                        glyph: 'xe016@icomoon'
                                    },
                                    {
                                        xtype: 'button',
                                        action: 'reset',
                                        glyph: 'xe01b@icomoon'
                                    }
                                ]
                            },
                            {
                                xtype: 'gridpanel',
                                title: 'Active',
                                itemId: 'activeReadingTypes',
                                flex: 1,
                                multiSelect: true,
                                /*viewConfig: {
                                    plugins: {
                                        ptype: 'gridviewdragdrop',
                                        dragGroup: activeGroup,
                                        dropGroup: availableGroup
                                    }
                                },   */
                                store: new Ext.data.Store({
                                    model: 'Cfg.model.ReadingType'
                                }),
                                data: [],
                                columns:columns
                            }
                        ]
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});