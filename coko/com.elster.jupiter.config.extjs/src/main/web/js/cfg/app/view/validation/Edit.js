Ext.define('Cfg.view.validation.Edit', {
    extend: 'Ext.window.Window',
    alias: 'widget.validationrulesetEdit',
    title: 'Edit Rule Set',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    width: 800,
    height: 600,
    modal: true,
    constrain: true,
    autoShow: true,
    requires: [
        'Ext.grid.*'    ,
        'Cfg.store.ValidationActions',
        'Cfg.store.Validators'
    ],

    initComponent: function () {
        this.cellEditing = new Ext.grid.plugin.CellEditing({
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
                    type: 'hbox',
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
                        plugins: [this.cellEditing],
                        columns: {
                            defaults: {
                                flex: 1
                            },
                            items: [
                                { header: 'Id', dataIndex: 'id' },
                                { header: 'Active', dataIndex: 'active', xtype: 'checkcolumn',
                                    editor: {
                                        xtype: 'checkbox',
                                        cls: 'x-grid-checkheader-editor'
                                    } } ,
                                {   header: 'Action',
                                    dataIndex: 'action',
                                    width: 130,
                                    editor: new Ext.form.field.ComboBox({
                                        typeAhead: true,
                                        queryMode: 'local',
                                        displayField: 'action',
                                        valueField: 'action',
                                        triggerAction: 'all',
                                        store: Ext.create('Cfg.store.ValidationActions')
                                    })
                                },
                                { header: 'Implementation', dataIndex: 'implementation',
                                    width: 130,
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
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});