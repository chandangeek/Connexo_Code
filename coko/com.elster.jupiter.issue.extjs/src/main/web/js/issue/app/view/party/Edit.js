Ext.define('Mtr.view.party.Edit', {
    extend: 'Ext.window.Window',
    alias: 'widget.partyEdit',
    title: 'Edit Party',
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
        'Mtr.store.Roles',
        'Mtr.store.Users',
        'Mtr.model.Role',
        'Mtr.model.User'
    ],

    activeIndex: 0,
    party: null,

    initComponent: function () {
        var me = this,
            availableRoles = me.id + 'availableRoles',
            activeRoles = me.id + 'activeRoles',
            availableDelegates = me.id + 'availableDelegates',
            activeDelegates = me.id + 'activeDelegates';

        var roleColumns = [
            {
                text: 'Name',
                flex: 1,
                dataIndex: 'name'
            },
            {
                text: 'Description',
                flex: 1,
                dataIndex: 'description'
            }
        ];

        var delegateColumns = [
            {
                text: 'Name',
                flex: 1,
                dataIndex: 'authenticationName'
            },
            {
                text: 'Description',
                flex: 1,
                dataIndex: 'description'
            }
        ];

        me.items = [
            {
                xtype: 'form',
                itemId: 'editform',
                layout: 'card',
                items: [
                    {
                        xtype: 'container',
                        layout: 'anchor',
                        anchor: '100%',
                        defaultType: 'textfield',
                        defaults: {
                            anchor: '100%'
                        },
                        items: [
                            {
                                name: 'mRID',
                                fieldLabel: 'mRID'
                            },
                            {
                                name: 'name',
                                fieldLabel: 'Name'
                            },
                            {
                                name: 'aliasName',
                                fieldLabel: 'Alias name'
                            },
                            {
                                xtype: 'textarea',
                                name: 'description',
                                fieldLabel: 'Description'
                            }
                            // TODO: Support editing the electronic address.
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: 'anchor',
                        anchor: '100%',
                        defaultType: 'textfield',
                        defaults: {
                            labelWidth: '50%',
                            anchor: '100%'
                        },
                        items: [
                            // TODO: Party details (organization or person specific).
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'gridpanel',
                                title: 'Available roles',
                                itemId: 'availableRoles',
                                flex: 1,
                                multiSelect: true,
                                viewConfig: {
                                    plugins: {
                                        ptype: 'gridviewdragdrop',
                                        dragGroup: availableRoles,
                                        dropGroup: activeRoles
                                    }
                                },
                                store: new Ext.data.Store({
                                    model: 'Mtr.model.Role'
                                }),
                                data: [],
                                columns: roleColumns
                            },
                            {
                                xtype: 'container',
                                itemId: 'roleActions',
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
                                title: 'Party has role',
                                itemId: 'activeRoles',
                                flex: 1,
                                multiSelect: true,
                                viewConfig: {
                                    plugins: {
                                        ptype: 'gridviewdragdrop',
                                        dragGroup: activeRoles,
                                        dropGroup: availableRoles
                                    }
                                },
                                store: new Ext.data.Store({
                                    model: 'Mtr.model.Role'
                                }),
                                data: [],
                                columns: roleColumns
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
                                xtype: 'gridpanel',
                                title: 'Available delegates',
                                itemId: 'availableDelegates',
                                flex: 1,
                                multiSelect: true,
                                viewConfig: {
                                    plugins: {
                                        ptype: 'gridviewdragdrop',
                                        dragGroup: availableDelegates,
                                        dropGroup: activeDelegates
                                    }
                                },
                                store: new Ext.data.Store({
                                    model: 'Mtr.model.User'
                                }),
                                data: [],
                                columns: delegateColumns
                            },
                            {
                                xtype: 'container',
                                itemId: 'delegateActions',
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
                                title: 'Party has delegate',
                                itemId: 'activeDelegates',
                                flex: 1,
                                multiSelect: true,
                                viewConfig: {
                                    plugins: {
                                        ptype: 'gridviewdragdrop',
                                        dragGroup: activeDelegates,
                                        dropGroup: availableDelegates
                                    }
                                },
                                store: new Ext.data.Store({
                                    model: 'Mtr.model.User'
                                }),
                                data: [],
                                columns: delegateColumns
                            }
                        ]
                    }
                ]
            }
        ];

        me.bbar = [
            {
                text: '&laquo; Previous',
                action: 'prev',
                scope: me,
                handler: me.prevStep
            },
            {
                text: 'Next &raquo;',
                action: 'next',
                scope: me,
                handler: me.nextStep
            },
            {
                xtype: 'component',
                flex: 1
            },
            {
                text: 'Cancel',
                scope: me,
                handler: me.close
            },
            {
                text: 'Save',
                action: 'update'
            }
        ];

        me.callParent(arguments);
    },

    prevStep: function () {
        var editForm = this.getEditForm(),
            layout = editForm.getLayout();

        if (this.activeIndex - 1 >= 0) {
            --this.activeIndex;
        }
        this.checkNavButtons();

        layout.setActiveItem(this.activeIndex);
        this.activeIndex = editForm.items.indexOf(layout.getActiveItem());
    },
    nextStep: function () {
        var editForm = this.getEditForm(),
            layout = editForm.getLayout();

        if (this.activeIndex + 1 < editForm.items.length) {
            ++this.activeIndex;
        }
        this.checkNavButtons();

        layout.setActiveItem(this.activeIndex);
        this.activeIndex = editForm.items.indexOf(layout.getActiveItem());
    },
    checkNavButtons: function () {
        var editForm = this.getEditForm(),
            prevButton = this.down('button[action=prev]'),
            nextButton = this.down('button[action=next]');

        if (this.activeIndex - 1 >= 0) {
            prevButton.enable();
        } else {
            prevButton.disable();
        }

        if (this.activeIndex + 1 < editForm.items.length) {
            nextButton.enable();
        } else {
            nextButton.disable();
        }
    },

    showParty: function (party) {
        var me = this,
            editForm = this.getEditForm(),
            layout = editForm.getLayout();

        me.party = party;

        editForm.loadRecord(party);

        layout.setActiveItem(0);
        me.activeIndex = editForm.items.indexOf(layout.getActiveItem());
        me.checkNavButtons();

        this.show();
        this.center();
    },

    getParty: function () {
        return this.party;
    },

    getEditForm: function () {
        return this.down('#editform');
    }

});