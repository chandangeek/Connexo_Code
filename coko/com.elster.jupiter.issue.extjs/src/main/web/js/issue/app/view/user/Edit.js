Ext.define('Mtr.view.user.Edit', {
    extend: 'Ext.window.Window',
    alias: 'widget.userEdit',
    title: 'Edit user',
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
        'Mtr.store.Groups',
        'Mtr.model.Group'
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
                text: 'Name',
                flex: 1,
                dataIndex: 'name'
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
                        name: 'authenticationName',
                        fieldLabel: 'Authentication name'
                    },
                    {
                        xtype: 'textfield',
                        name: 'description',
                        fieldLabel: 'Description'
                    },
                    {
                        xtype: 'textfield',
                        name: 'version',
                        fieldLabel: 'Version'
                    }
                ]
            },
            {
                xtype: 'fieldset',
                title: 'Groups',
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
                        itemId: 'availableGroups',
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
                            model: 'Mtr.model.Group'
                        }),
                        data: [],
                        columns: columns
                    },
                    {
                        xtype: 'container',
                        itemId: 'groupActions',
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
                        title: 'User belongs to',
                        itemId: 'activeGroups',
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
                            model: 'Mtr.model.Group'
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

