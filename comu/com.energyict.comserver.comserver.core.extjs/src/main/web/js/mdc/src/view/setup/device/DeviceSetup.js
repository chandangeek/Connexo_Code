Ext.define('Mdc.view.setup.device.DeviceSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSetup',
    device: null,
    itemId: 'deviceSetup',

    requires: [
        'Isu.privileges.Issue',
        'Mdc.view.setup.device.DeviceActionMenu',
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
                type: 'fit',
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
            iconCls: !!flag ? 'icon-star6' : 'icon-star4',
            ui: 'plain',
            style: 'font-size: 16px',
            flag: flag,
            pressed: !!flag,
            privileges: Mdc.privileges.Device.flagDevice,
            enableToggle: true,
            toggleHandler: function(button, state) {
                button.setIconCls(state ? 'icon-star6' : 'icon-star4');
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
            items: [ // Let's put an empty grid in just to illustrate fit layout
                {
                    xtype: 'form',
                    border: false,
                    items: [
                        {
                            xtype: 'textareafield',
                            name: 'comment',
                            fieldLabel: Uni.I18n.translate('device.flag.label.comment', 'MDC', 'Comment'),
                            anchor: '100%',
                            height: 100
                        },
                        {
                            xtype: 'fieldcontainer',
//                            ui: 'actions',
                            fieldLabel: '&nbsp',
                            layout: {
                                type: 'hbox'
                            },
                            items: [
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('device.flag.button.flag', 'MDC', 'Flag device'),
                                    ui: 'action',
                                    name: 'flag',
                                    handler: function () {
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
                                },
                                {
                                    xtype: 'button',
                                    ui: 'link',
                                    text: Uni.I18n.translate('device.flag.button.cancel', 'MDC', 'Cancel'),
                                    name: 'cancel',
                                    handler: function () {
                                        button.toggle(false, false);
                                        button.window.close();
                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
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
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        style: {
                            'background-color': '#71adc7'
                        },
                        iconCls: 'x-uni-action-iconD',
                        menu: {
                            xtype: 'device-action-menu',
                            itemId: 'deviceActionMenu'
                        }
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
                        xtype: 'deviceGeneralInformationPanel',
                        router: me.router
                    },
                    {
                        xtype: 'deviceCommunicationTopologyPanel',
                        privileges: Mdc.privileges.Device.deviceOperator,
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
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    },
                    flex: 1
                },
                items: [
                    {
                        xtype: 'deviceOpenIssuesPanel',
                        privileges: Isu.privileges.Issue.viewAdminDevice,
                        router: me.router
                    },
                    {
                        xtype: 'device-data-validation-panel',
                        privileges: Cfg.privileges.Validation.fineTuneOnDevice,
                        mRID: me.device.get('mRID')
                    }
                ]
            },
            {
                xtype: 'panel',
                privileges: Mdc.privileges.Device.deviceOperator,
                itemId: 'device-connections-panel',
                style: {
                    marginRight: '20px',
                    marginTop: '20px'
                },
                layout: 'fit',
                items: {
                    xtype: 'preview-container',
                    hasNotEmptyComponent: true,
                    grid: {
                        xtype: 'device-connections-list',
                        itemId: 'connectionslist',
                        store: me.device.connections(),
                        router: me.router,
                        viewConfig: {
                            disableSelection: true,
                            enableTextSelection: true
                        }
                    }
                }
            },
            {
                xtype: 'panel',
                privileges: Mdc.privileges.Device.deviceOperator,
                itemId: 'device-communications-panel',
                style: {
                    marginRight: '20px',
                    marginTop: '20px'
                },
                layout: 'fit',
                items: {
                    xtype: 'preview-container',
                    hasNotEmptyComponent: true,
                    grid: {
                        xtype: 'device-communications-list',
                        itemId: 'communicationslist',
                        title: '&nbsp;',
                        store: me.device.communications(),
                        router: me.router,
                        viewConfig: {
                            disableSelection: true,
                            enableTextSelection: true
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
                    }
                }
            }
        );
    }
});