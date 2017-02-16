/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.DeviceTypePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceTypePreview',
    itemId: 'deviceTypePreview',
    purposeStore: null,

    requires: [
        'Mdc.model.DeviceType',
        'Mdc.view.setup.devicetype.DeviceTypeActionMenu',
        'Mdc.store.DeviceTypePurposes'
    ],

    title: Uni.I18n.translate('general.details','MDC','Details'),

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'device-type-action-menu'
            }
        }
    ],

    // todo: set empty text
    emptyText: '<h3>' + Uni.I18n.translate('devicetype.noDeviceTypeSelected', 'MDC', 'No device type selected') + '</h3><p>' + Uni.I18n.translate('devicetype.selectDeviceType', 'MDC', 'Select a device type to see its details') + '</p>',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                border: false,
                itemId: 'deviceTypePreviewForm',
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
                                        fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                                        name: 'deviceTypePurpose',
                                        renderer: function (value) {
                                            if (value) {
                                                return me.purposeStore ? me.purposeStore.findRecord('deviceTypePurpose', value).get('localizedValue') : value;
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceProtocolPluggableClass',
                                        fieldLabel: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Communication protocol'),
                                        renderer: function (value) {
                                            return value ? value : '-';
                                        }
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        fieldLabel: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                                        items: [
                                            {
                                                itemId: 'device-life-cycle-link',
                                                xtype: 'button',
                                                ui: 'link',
                                                href: 'javascript:void(0)'
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'canBeGateway',
                                        fieldLabel: Uni.I18n.translate('devicetype.canBeGateway', 'MDC', 'Device can be a gateway'),
                                        renderer: function (item) {
                                            return item ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                                        },
                                        readOnly: true
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'canBeDirectlyAddressed',
                                        fieldLabel: Uni.I18n.translate('devicetype.canBeDirectlyAddressable', 'MDC', 'Device can be directly addressable'),
                                        renderer: function (item) {
                                            return item ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                                        },
                                        readOnly: true
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
                                                text: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
                                                itemId: 'deviceTypeRegistersLink'
                                            },

                                            {
                                                name: 'loadProfileCount',
                                                text: Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'Load profile types'),
                                                itemId: 'deviceTypeLoadProfilesLink'
                                            },

                                            {
                                                name: 'logBookCount',
                                                text: Uni.I18n.translate('devicetype.logbooks', 'MDC', 'Logbook types'),
                                                itemId: 'deviceTypeLogBooksLink'
                                            }
                                        ]
                                    },

                                    {
                                        xtype: 'fieldcontainer',
                                        columnWidth: 0.5,
                                        fieldLabel: Uni.I18n.translate('devicetype.deviceConfigurations', 'MDC', 'Device configurations'),
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
                                                name: 'deviceConfigurationCount',
                                                text: ' ',
                                                itemId: 'deviceConfigurationsLink'
                                            }
                                        ]
                                    }
                                ]
                            }

                        ]
                    }
                ]
            }
        ];

        this.callParent();
    }
});
