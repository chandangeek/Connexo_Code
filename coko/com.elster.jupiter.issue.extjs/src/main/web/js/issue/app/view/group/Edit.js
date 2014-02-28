Ext.define('Mtr.view.group.Edit', {
    extend: 'Ext.window.Window',
    alias: 'widget.groupEdit',
    title: 'Edit group',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    width: 600,
    height: 450,
    modal: true,
    constrain: true,
    autoShow: true,
    requires: [
        'Ext.grid.*',
        'Ext.util.Point', // Required for the drag and drop.
        'Mtr.store.Privileges',
        'Mtr.model.Privilege'
    ],

    initComponent: function () {
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
            {
                text: 'Component',
                dataIndex: 'componentName'
            },
            {
                text: 'Name',
                flex: 1,
                dataIndex: 'name'
            },
            {
                text: 'Description',
                flex: 1,
                dataIndex: 'description',
                hidden: true
            }
        ];

        var availableGroup = this.id + 'availableGroup',
            activeGroup = this.id + 'activeGroup';

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
                        fieldLabel: 'Id'
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        fieldLabel: 'Name'
                    },
                    {
                        xtype: 'textfield',
                        name: 'version',
                        fieldLabel: 'Version',
                        margin: '0'
                    }
                ]
            },
            {
                xtype: 'fieldset',
                title: 'Privileges',
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
                        itemId: 'availablePrivileges',
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
                            model: 'Mtr.model.Privilege'
                        }),
                        data: [],
                        columns: columns
                    },
                    {
                        xtype: 'container',
                        itemId: 'privilegeActions',
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
                        itemId: 'activePrivileges',
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
                            model: 'Mtr.model.Privilege'
                        }),
                        data: [],
                        columns: columns
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});