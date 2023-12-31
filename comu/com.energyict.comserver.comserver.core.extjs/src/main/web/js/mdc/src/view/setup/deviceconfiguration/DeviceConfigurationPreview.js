/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceConfigurationPreview',
    itemId: 'deviceConfigurationPreview',
    requires: [
        'Mdc.model.DeviceConfiguration',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationActionMenu'
    ],
    frame: true,
    title: "Details",


    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'device-configuration-action-menu'
            }
        }
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'panel',
            border: false,
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('deviceconfiguration.noDeviceConfigurationSelected', 'MDC', 'No device configuration selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('deviceconfiguration.selectDeviceConfiguration', 'MDC', 'Select a device configuration to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            itemId: 'deviceConfigurationPreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
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
                                    name: 'isDefault',
                                    fieldLabel: Uni.I18n.translate('general.default', 'MDC', 'Default'),
                                    renderer: function (value) {
                                        return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                    name: 'active',
                                    renderer: function (value) {
                                        return value === true ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.isDirectlyAddressable', 'MDC', 'Directly addressable'),
                                    name: 'isDirectlyAddressable',
                                    itemId: 'mdc-deviceConfigurationPreview-directlyAddressable',
                                    renderer: function (value, field) {
                                        return value === true
                                            ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                            : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.Gateway', 'MDC', 'Gateway'),
                                    itemId: 'mdc-deviceConfigurationPreview-gateway',
                                    name: 'canBeGateway',
                                    renderer: function (value) {
                                        var text,
                                            record;

                                        if (value) {
                                            record = this.up('#deviceConfigurationPreviewForm').getRecord();
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
                                    itemId: 'mdc-deviceConfigurationPreview-dataLogger',
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
                                    itemId: 'mdc-deviceConfigurationPreview-multiElement',
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
                                    itemId: 'mdc-deviceConfigurationPreview-validateOnStore',
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
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.dataSources', 'MDC', 'Data sources'),
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
                                            text: Uni.I18n.translate('deviceconfiguration.registers', 'MDC', 'Register configurations'),
                                            itemId: 'deviceConfigurationRegistersLink'
                                        },

                                        {
                                            name: 'loadProfileCount',
                                            text: Uni.I18n.translate('general.loadProfileConfigurations', 'MDC', 'Load profile configurations'),
                                            itemId: 'deviceConfigurationLoadProfilesLink'
                                        },

                                        {
                                            name: 'logBookCount',
                                            text: Uni.I18n.translate('general.logbookConfigurations', 'MDC', 'Logbook configurations'),
                                            itemId: 'deviceConfigurationLogBooksLink'
                                        }
                                    ]
                                }
                            ]
                        }

                    ]
                }
            ]
        }
    ]

});
