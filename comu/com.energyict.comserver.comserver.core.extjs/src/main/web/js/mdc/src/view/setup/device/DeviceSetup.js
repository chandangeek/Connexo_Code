Ext.define('Mdc.view.setup.device.DeviceSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSetup',
    device: null,
    deviceLabelsStore: null,
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
        'Uni.view.button.MarkedButton',
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
                        value: me.device.get('mRID'),
                        fieldCls: 'x-panel-header-text-container-large',
                        style: 'margin-right: 10px'
                    },
                    {
                        xtype: 'marked-button',
                        itemId: 'device-favorite-flag',
                        store: me.deviceLabelsStore,
                        labelId: 'mdc.label.category.favorites',
                        markedTooltip: Uni.I18n.translate('device.flag.tooltip.unflag', 'MDC', 'Click to remove from the list of flagged devices'),
                        unmarkedTooltip: Uni.I18n.translate('device.flag.tooltip.flag', 'MDC', 'Click to flag the device'),
                        width: 20,
                        height: 20,
                        getParent: function () {
                            return {
                                id: me.device.get('id'),
                                version: me.device.get('version')
                            };
                        }
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
                        hidden: true,
                        style: {
                            'background-color': '#71adc7'
                        },
                        menu: {
                            xtype: 'device-action-menu',
                            itemId: 'deviceActionMenu',
                            router: me.router,
                            disableChangeConfigSinceDataLoggerOrSlave: disableChangeConfigSinceDataLoggerOrSlave
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
                                mrId: me.device.get('mRID'),
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
                                        mRID: me.device.get('mRID'),
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