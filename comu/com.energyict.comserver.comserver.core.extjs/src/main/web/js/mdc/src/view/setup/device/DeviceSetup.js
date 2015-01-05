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
        'Mdc.view.setup.device.DeviceCommunications',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
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
        var me = this,
            panel = me.content[0];

        panel.title = me.router.getRoute().getTitle();
        panel.tools = [
            {
                xtype: 'toolbar',
                margin: '0 20 0 0',
                items: [
                    '->',
                    {
                        xtype: 'container',
                        itemId: 'deviceSetupFlags',
                        layout: 'fit',
                        width: 20,
                        height: 20
                    },
                    {
                        xtype: 'component',
                        itemId: 'last-updated-field',
                        width: 150,
                        style: {
                            'font': 'normal 13px/17px Lato',
                            'color': '#686868',
                            'margin-right': '10px'
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'refresh-btn',
                        style: {
                            'background-color': '#71adc7'
                        },
                        text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                        icon: '/apps/sky/resources/images/form/restore.png'
                    }
                ]
            }
        ];

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
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-connections-list',
                        itemId: 'connectionslist',
                        store: me.device.connections(),
                        router: me.router,
                        viewConfig: {
                            disableSelection: true
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'device-connections-no-items-found-panel',
                        title: Uni.I18n.translate('device.connections.empty.title', 'MDC', 'No connections found'),
                        reasons: [
                            Uni.I18n.translate('device.connections.empty.list.item1', 'MDC', 'No connections for device have been created yet.')
                        ]
                    }
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
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-communications-list',
                        itemId: 'communicationslist',
                        title: '&nbsp;',
                        store: me.device.communications(),
                        router: me.router,
                        viewConfig: {
                            disableSelection: true
                        },
                        tools: [
                            {
                                xtype: 'toolbar',
                                items: [
                                    '->',
                                    {
                                        xtype: 'button',
                                        itemId: 'activate-all',
                                        style: {
                                            'background-color': '#71adc7'
                                        },
                                        text: Uni.I18n.translate('device.communications.activate', 'DSH', 'Activate all')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'deactivate-all',
                                        style: {
                                            'background-color': '#71adc7'
                                        },
                                        text: Uni.I18n.translate('device.communications.deactivate', 'DSH', 'Deactivate all')
                                    }
                                ]
                            }
                        ]
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'device-communications-no-items-found-panel',
                        title: Uni.I18n.translate('device.communicationTasks.empty.title', 'MDC', 'No communication tasks found'),
                        reasons: [
                            Uni.I18n.translate('device.communicationTasks.empty.list.item1', 'MDC', 'No communication tasks for device have been created yet.')
                        ]
                    }
                }
            }
        );
    }
});