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
        'Cfg.store.ValidationRules',
        'Cfg.store.ValidationRuleProperties',
        'Ext.util.Point', // Required for the drag and drop.
        'Cfg.store.ReadingTypes',
        'Cfg.model.ReadingType',
        'Cfg.store.ValidationPropertySpecsForRule'
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
            { header: 'Name', dataIndex: 'name', flex: 1}
        ];

        var availableGroup = this.id + 'availableGroup',
            activeGroup = this.id + 'activeGroup';

        var rulesGrid = Ext.create('Ext.grid.GridPanel', {
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
                        //{ header: 'Id', dataIndex: 'id', flex: 0.20 },
                        { header: 'Validator', dataIndex: 'implementation',
                            editor: new Ext.form.field.ComboBox({
                                typeAhead: true,
                                queryMode: 'local',
                                displayField: 'implementation',
                                valueField: 'implementation',
                                triggerAction: 'all',
                                store: Ext.create('Cfg.store.Validators')
                            })
                        },
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
                        }
                    ]
                },
                tbar: [{
                    text: 'Add Rule',
                    itemId: 'addRule',
                    action: 'addRule'
                    },
                    {
                        itemId: 'removeRule',
                        text: 'Remove Rule',
                        action: 'removeRule',
                        disabled: true
                    }],
                listeners: {
                    'selectionchange': function(view, records) {
                        rulesGrid.down('#removeRule').setDisabled(!records.length);
                    }
                }
            }
        );

        var rulePropertiesGrid = Ext.create('Ext.grid.GridPanel', {
                title: 'Properties',
                itemId: 'validationrulepropertiesList',
                flex: 1,
                store: 'ValidationRuleProperties',
                selType: 'rowmodel',
                selModel: {
                    mode: 'SINGLE'
                },
                plugins: [cellEditing2],
                columns: {
                    defaults: {
                        flex: 1
                    },
                    items: [
                        { header: 'Name', dataIndex: 'name',
                            editor: new Ext.form.field.ComboBox({
                                typeAhead: true,
                                mode: 'local',
                                itemId : 'availablepropertyspecscombo',
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'name',
                                triggerAction: 'all',
                                store: 'ValidationPropertySpecsForRule'
                            })},
                        { header: 'Value', dataIndex: 'value', editor: {
                            xtype: 'numberfield',
                            allowBlank: false
                        }}
                    ]
                },
                tbar: [{
                    text: 'Add Property',
                    itemId: 'addRuleProperty',
                    action: 'addRuleProperty'
                },
                    {
                        itemId: 'removeRuleProperty',
                        text: 'Remove Property',
                        action: 'removeRuleProperty',
                        disabled: true
                    }],
                listeners: {
                    'selectionchange': function(view, records) {
                        rulePropertiesGrid.down('#removeRuleProperty').setDisabled(!records.length);
                    }
                }
            }
        );


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
                    /*{
                        xtype: 'textfield',
                        name: 'id',
                        fieldLabel: 'Id',
                        readOnly: true
                    },       */
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
                    rulesGrid,
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
                            rulePropertiesGrid
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
                                hideHeaders: true,
                                flex: 1,
                                multiSelect: true,
                                viewConfig: {
                                    plugins: {
                                        ptype: 'gridviewdragdrop',
                                        dragGroup: availableGroup,
                                        dropGroup: activeGroup
                                    }
                                },
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
                                hideHeaders: true,
                                flex: 1,
                                multiSelect: true,
                                viewConfig: {
                                    plugins: {
                                        ptype: 'gridviewdragdrop',
                                        dragGroup: activeGroup,
                                        dropGroup: availableGroup
                                    }
                                },
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