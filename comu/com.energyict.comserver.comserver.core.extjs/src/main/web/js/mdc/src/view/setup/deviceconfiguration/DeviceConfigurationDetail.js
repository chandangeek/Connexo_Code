/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationDetail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationDetail',
    itemId: 'deviceConfigurationDetail',
    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationsGrid',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationPreview',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu'
    ],
    deviceTypeId: null,
    deviceConfigurationId: null,

    content: [
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    ui: 'large',
                    itemId: 'device-configuration-detail-panel',
                    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                    flex: 1
                },
                {
                    xtype: 'uni-button-action',
                    privileges: Mdc.privileges.DeviceType.admin,
                    menu: {
                        xtype: 'device-configuration-action-menu'
                    }
                }
            ]
        },
        {
            xtype: 'form',
            border: false,
            itemId: 'deviceConfigurationDetailForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column',
                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'container',
                            columnWidth: 0.5,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                    itemId: 'deviceName'

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'description',
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.description', 'MDC', 'Description'),
                                    itemId: 'deviceConfigurationDescription',
                                    renderer: function(value) {
                                        return value ? value : '-'
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'active',
                                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                    itemId: 'deviceConfigurationStatus',
                                    renderer: function (item) {
                                        return item ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.isDirectlyAddressable', 'MDC', 'Directly addressable'),
                                    name: 'isDirectlyAddressable',
                                    renderer: function (value) {
                                        return value === true
                                            ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                            : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.Gateway', 'MDC', 'Gateway'),
                                    name: 'canBeGateway',
                                    renderer: function (value) {
                                        var text,
                                            record;

                                        if (value) {
                                            record = this.up('#deviceConfigurationDetailForm').getRecord();
                                            text = Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                                            if (record) {
                                                text += ' (' + record.get('gatewayType') + ')';
                                            }
                                        } else {
                                            text = Uni.I18n.translate('general.no', 'MDC', 'No');
                                        }
                                        return text;
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.dataLoggerFunctionality', 'MDC', 'Data logger functionality'),
                                    name: 'dataloggerEnabled',
                                    renderer: function (value) {
                                        return value === true
                                            ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                            : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.multiElementFunctionality', 'MDC', 'Multi-element functionality'),
                                    name: 'multiElementEnabled',
                                    renderer: function (value) {
                                        return value === true
                                            ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                            : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.validateOnStore', 'MDC', 'Validate data on storage'),
                                    name: 'validateOnStore',
                                    renderer: function (value) {
                                        return value === true
                                            ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                            : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.5,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'fieldcontainer',
                                    columnWidth: 0.5,
                                    fieldLabel: Uni.I18n.translate('devicetype.dataSources', 'MDC', 'Data sources'),
                                    labelAlign: 'right',
                                    layout: {
                                        type: 'vbox'
                                    },
                                    defaults: {
                                        xtype: 'button',
                                        ui: 'link',
                                        href: 'javascript:void(0)'
                                    },
                                    items: [
                                        {
                                            name: 'registerCount',
                                            text: Uni.I18n.translate('deviceconfig.registerconfigs', 'MDC', 'Register configurations'),
                                            itemId: 'deviceConfigurationDetailRegistersLink'
                                        },
                                        {
                                            name: 'loadProfileCount',
                                            text: Uni.I18n.translate('general.loadProfileConfigurations', 'MDC', 'Load profile configurations'),
                                            itemId: 'deviceConfigurationDetailLoadProfilesLink'
                                        },
                                        {
                                            name: 'logBookCount',
                                            text: Uni.I18n.translate('general.logbookConfigurations', 'MDC', 'Logbook configurations'),
                                            itemId: 'deviceConfigurationDetailLogBooksLink'
                                        }
                                    ]
                                }
                            ]
                        }

                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigurationId
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});