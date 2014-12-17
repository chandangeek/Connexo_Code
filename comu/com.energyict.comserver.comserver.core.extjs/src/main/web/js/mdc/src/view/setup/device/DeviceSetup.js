Ext.define('Mdc.view.setup.device.DeviceSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSetup',
    device: null,
    itemId: 'deviceSetup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.device.DeviceCommunicationTopologyPanel',
        'Mdc.view.setup.device.DeviceGeneralInformationPanel',
        'Mdc.view.setup.device.DeviceOpenIssuesPanel',
        'Mdc.view.setup.device.DeviceDataValidationPanel',
        'Mdc.view.setup.device.DeviceConnections',
        'Mdc.view.setup.device.DeviceCommunications'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'deviceSetupPanel',
            //title: Uni.I18n.translate('devicesetup.deviceConfigurations', 'MDC', 'deviceName'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    itemId: 'DeviceContainer'
                }
            ]
        }
    ],

    renderFlag: function(labelsStore) {
        var me = this;
        var toolbar = me.down('#deviceSetupFlags');
        var flag = null;

        if (labelsStore.count) {
            flag = labelsStore.getById('mdc.label.category.favorites');
        }

        toolbar.removeAll();
        toolbar.add({
            xtype: 'button',
            iconCls: 'device-flag',
            ui: 'icon',
            pressed: !!flag,
            flag: flag,
            hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceData','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication']),
            enableToggle: true,
            toggleHandler: function(button, state) {
                button.setTooltip(state
                    ? Uni.I18n.translate('device.flag.tooltip.unflag', 'MDC', 'Click to remove from the list of flagged devices')
                    : Uni.I18n.translate('device.flag.tooltip.flag', 'MDC', 'Click to flag the device')
                );
            },
            handler: function(button) {
                if (!button.flag) {
                    button.window && button.window.isVisible()
                        ? button.window.close()
                        : me.openFlagWindow(button, new labelsStore.model());
                } else {
                    me.removeFlag(button);
                }
            }
        });

        var button = toolbar.down('button');
        button.toggleHandler(button, button.pressed);
    },

    removeFlag: function (button) {
        button.flag.destroy({
            callback: function () {
                button.flag = null;
                button.toggle(false, false);
            }
        });
    },

    openFlagWindow: function(button, flag) {
        var me = this;
        button.window = Ext.create('Ext.window.Window', {
            title: Uni.I18n.translate('device.flag.title', 'MDC', 'Flag device') + ' ' + me.router.getRoute().getTitle(),
            closable: false,
            height: 200,
            alignTarget: button,
            defaultAlign: 'tr-br',
            width: 400,
            layout: 'fit',
            items: {  // Let's put an empty grid in just to illustrate fit layout
                xtype: 'form',
                border: false,
                items: {
                    xtype: 'textareafield',
                    name: 'comment',
                    fieldLabel: Uni.I18n.translate('device.flag.label.comment', 'MDC', 'Comment'),
                    anchor: '100%',
                    height: 100
                }
            },
            buttons: [{
                text: Uni.I18n.translate('device.flag.button.flag', 'MDC', 'Flag device'),
                name: 'flag',
                handler: function() {
                    var form = button.window.down('form');
                    var flag = form.getRecord();
                    form.updateRecord();
                    flag.set('category', {
                        id: 'mdc.label.category.favorites',
                        name: 'Favorites'
                    });
                    flag.save({
                        callback: function () {
                            flag.setId(flag.get('category').id);
                            button.flag = flag;
                            button.toggle(true, false);
                        }
                    });
                    button.window.close();
                }
            }, {
                ui: 'link',
                text: Uni.I18n.translate('device.flag.button.cancel', 'MDC', 'Cancel'),
                name: 'cancel',
                handler: function() {
                    button.toggle(false, false);
                    button.window.close();
                }
            }]
        });

        button.window.show();
        button.window.down('form').loadRecord(flag);
    },

    initComponent: function () {
        var me = this;

        me.content[0].tbar = {
            margin: '0 20 0 0',
            items: [
                {
                    xtype: 'container',
                    itemId: 'deviceSetupPanelTitle',
                    cls: 'x-panel-header-text-container-large',
                    html: me.router.getRoute().getTitle()
                },
                '->',
                {
                    xtype: 'container',
                    itemId: 'deviceSetupFlags',
                    layout: 'fit',
                    width: 20,
                    height: 20
                }
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'deviceOverviewLink'
                    }
                ]
            }
        ];
        me.callParent(arguments);

        me.down('#DeviceContainer').add(
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    },
                    flex: 1
                },
                items: [
                    {
                        xtype: 'deviceGeneralInformationPanel'
                    },
                    {
                        xtype: 'deviceCommunicationTopologyPanel',
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication']),
                        router: me.router
                    }
                ]
            },
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: {
                    flex: 1
                },
                items: [
                    {
                        xtype: 'deviceOpenIssuesPanel',
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue']),
                        router: me.router,
                        style: {
                            marginRight: '20px',
                            padding: '20px'
                        }
                    },
                    {
                        xtype: 'container',
                        style: {
                            marginRight: '20px'
                        },
                        items: {
                            xtype: 'device-data-validation-panel',
                            hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration','privilege.view.fineTuneValidationConfiguration.onDevice']),
                            mRID: me.device.get('mRID')
                        }

                    }
                ]
            },
            {
                xtype: 'panel',
                hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication']),
                itemId: 'device-connections-panel',
                style: {
                    marginRight: '20px',
                    marginTop: '20px'
                },
                layout: 'fit',
                items: {
                    xtype: 'device-connections-list',
                    router: me.router
                },
                bindStore: function(store) {
                    var me = this;
                    me.down('device-connections-list').reconfigure(store);
                    me.setTitle(Uni.I18n.translatePlural('device.connections.title', store.count(), 'DSH', 'Connections ({0})'));
                }
            },
            {
                xtype: 'panel',
                hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication']),
                itemId: 'device-communications-panel',
                style: {
                    marginRight: '20px',
                    marginTop: '20px'
                },
                layout: 'fit',
                items: {
                    xtype: 'device-communications-list',
                    router: me.router
                },
                tools: [
                    {
                        xtype: 'toolbar',
                        items: [
                            '->',
                            {
                                xtype: 'button',
                                itemId: 'activate-all',
                                text: Uni.I18n.translate('device.communications.activate', 'DSH', 'Activate all')
                            },
                            {
                                xtype: 'button',
                                itemId: 'deactivate-all',
                                text: Uni.I18n.translate('device.communications.deactivate', 'DSH', 'Deactivate all')
                            }
                        ]
                    }
                ],

                bindStore: function(store) {
                    var me = this;
                    me.down('device-communications-list').reconfigure(store);
                    me.setTitle(Uni.I18n.translatePlural('device.communications.title', store.count(), 'DSH', 'Communication tasks ({0})'));
                }
            }
        );
    }
});