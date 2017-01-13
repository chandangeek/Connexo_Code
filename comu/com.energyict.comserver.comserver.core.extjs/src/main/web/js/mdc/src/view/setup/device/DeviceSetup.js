Ext.define('Mdc.view.setup.device.DeviceSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSetup',
    device: null,
    router: undefined,
    actionsStore: undefined,
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
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.widget.WhatsGoingOn',
        'Mdc.view.setup.device.DataLoggerSlavesPanel'
        //'Mdc.view.setup.device.DeviceHealthCheckPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'deviceSetupPanel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            }
        }
    ],

    renderFlag: function (labelsStore) {
        var me = this,
            toolbar = me.down('#deviceSetupFlags'),
            flag = null;

        if (!me.rendered) return;

        if (labelsStore.count) {
            flag = labelsStore.getById('mdc.label.category.favorites');
        }

        toolbar.removeAll();
        toolbar.add({
            xtype: 'button',
            iconCls: !!flag ? 'icon-star-full' : 'icon-star-empty',
            ui: 'plain',
            style: 'font-size: 20px',
            flag: flag,
            pressed: !!flag,
            privileges: Mdc.privileges.Device.flagDevice,
            enableToggle: true,
            toggleHandler: function (button, state) {
                button.setIconCls(state ? 'icon-star-full' : 'icon-star-empty');
                button.setTooltip(state
                    ? Uni.I18n.translate('device.flag.tooltip.unflag', 'MDC', 'Click to remove from the list of flagged devices')
                    : Uni.I18n.translate('device.flag.tooltip.flag', 'MDC', 'Click to flag the device')
                );
            },
            handler: function (button) {
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
            isNotEdit: true,
            callback: function () {
                button.flag = null;
                button.toggle(false, false);
            }
        });
    },

    openFlagWindow: function (button, flag) {
        var me = this;
        button.window = Ext.create('Ext.window.Window', {
            title: Uni.I18n.translate('device.flag.title', 'MDC', 'Flag device {0}', [me.device.get('name')], false),
            closable: false,
            height: 200,
            alignTarget: button,
            defaultAlign: 'tl-br',
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
                                        flag.set('parent', {
                                            id: me.device.get('id'),
                                            version: me.device.get('version')
                                        });
                                        flag.set('category', {
                                            id: 'mdc.label.category.favorites',
                                            name: 'Favorites'
                                        });
                                        flag.save({
                                            isNotEdit: true,
                                            callback: function (rec, operation) {
                                                var json = Ext.decode(operation.response.responseText);
                                                flag.setId(flag.get('category').id);
                                                flag.set('creationDate', json.creationDate);
                                                flag.set('parent', json.parent);
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
            panel = me.content[0],
            isGateway = me.device.get('isGateway'),
            isDirectlyAddressable = me.device.get('isDirectlyAddressed'),
            disableChangeConfigSinceDataLoggerOrSlave = me.device.get('isDataLogger') || me.device.get('isDataLoggerSlave'),
            hasValidationRules = me.device.get('hasValidationRules');

        panel.tools = [
            {
                xtype: 'toolbar',
                margin: '0 20 0 0',
                width: '100%',
                items: [
                    {
                        xtype: 'displayfield',
                        value: me.device.get('name'),
                        fieldCls: 'x-panel-header-text-container-large',
                        style: 'margin-right: 10px'
                    },
                    {
                        xtype: 'container',
                        itemId: 'deviceSetupFlags',
                        layout: 'fit',
                        width: 20,
                        height: 20
                    },
                    {
                        xtype: 'tbfill'
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
                        text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'MDC', 'Refresh'),
                        iconCls: 'icon-spinner11'
                    },
                    {
                        xtype: 'uni-button-action',
                        itemId: 'device-landing-actions-btn',
                        style: {
                            'background-color': '#71adc7'
                        },
                        menu: {
                            xtype: 'device-action-menu',
                            itemId: 'deviceActionMenu',
                            router: me.router,
                            disableChangeConfigSinceDataLoggerOrSlave: disableChangeConfigSinceDataLoggerOrSlave,
                            deviceName: me.device.get('name'),
                            actionsStore: me.actionsStore
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

        me.down('#deviceSetupPanel').add(
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: {
                    flex: 1
                },
                items: [
                    {
                        xtype: 'container',
                        itemId: 'mdc-panel-container',
                        flex: 2,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'whatsgoingon',
                                deviceId: me.device.get('name'),
                                type: 'device',
                                router: me.router,
                                style: 'margin-bottom: 20px'
                            },
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                style: 'margin-bottom: 0px',
                                defaults: {
                                    flex: 1
                                },
                                items: [
                                    {
                                        xtype: 'deviceCommunicationTopologyPanel',
                                        title: Uni.I18n.translate('deviceCommunicationTopology.title', 'MDC', 'Communication topology'),
                                        privileges: Mdc.privileges.Device.deviceOperator,
                                        router: me.router,
                                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.topologyWidget,
                                        hidden: isDirectlyAddressable && !isGateway
                                    },
                                    {
                                        xtype: 'device-data-validation-panel',
                                        privileges: Mdc.privileges.Device.deviceOperator,
                                        deviceId: me.device.get('name'),
                                        router: me.router,
                                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationWidget,
                                        hidden: !hasValidationRules
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'deviceGeneralInformationPanel',
                        dataLoggerSlave: me.device.get('isDataLoggerSlave') ? me.device : undefined,
                        router: me.router,
                        minWidth: 300,
                        style: {
                            marginRight: '20px'
                        }
                    }
                ]
            },
            {
                xtype: 'panel',
                privileges: Mdc.privileges.Device.deviceOperator,
                itemId: 'device-connections-panel',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionWidget,
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
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksWidget,
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
                        title: ' ',
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
                                        text: Uni.I18n.translate('device.communications.activate', 'MDC', 'Activate all')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'deactivate-all',
                                        style: {
                                            'background-color': '#71adc7'
                                        },
                                        text: Uni.I18n.translate('device.communications.deactivate', 'MDC', 'Deactivate all')
                                    }
                                ]
                            }
                        ]
                    }
                }
            }
        );

        if (me.rendered) {
            me.addDataLoggerSlavesPanelIfNeeded();
        } else {
            me.on('afterrender', function() {
                me.addDataLoggerSlavesPanelIfNeeded();
            }, me, {single:true});

        }
    },

    addDataLoggerSlavesPanelIfNeeded: function() {
        var me = this,
            panelContainer = me.down('#mdc-panel-container');

        if ( Ext.isEmpty(me.device.get('isDataLogger')) || !me.device.get('isDataLogger') ) {
            return;
        }

        panelContainer.add({
            xtype: 'dataLoggerSlavesPanel',
            privileges: Mdc.privileges.Device.viewDevice,
            router: me.router,
            device: me.device
        });
    }
});