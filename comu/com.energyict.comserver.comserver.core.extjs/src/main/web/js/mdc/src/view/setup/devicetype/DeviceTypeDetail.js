/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.DeviceTypeDetail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTypeDetail',
    itemId: 'deviceTypeDetail',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview',
        'Mdc.view.setup.devicetype.SideMenu'
    ],
    deviceTypeId: null,
    purposeStore: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },

                items: [
                    {
                        ui: 'large',
                        itemId: 'device-type-detail-panel',
                        title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                        flex: 1
                    },
                    {
                        xtype: 'uni-button-action',
                        privileges: Mdc.privileges.DeviceType.admin,
                        menu: {
                            xtype: 'device-type-action-menu'
                        }
                    }
                ]
            },
            {
                xtype: 'form',
                border: false,
                itemId: 'deviceTypeDetailForm',
                layout: 'fit',
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
                                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceTypePurpose',
                                        fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
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
                                        width: 500,
                                        fieldLabel: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                                        items: [
                                            {
                                                itemId: 'details-device-life-cycle-link',
                                                xtype: 'button',
                                                style: {
                                                    paddingLeft: 0
                                                },
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
                                                itemId: 'deviceTypeDetailRegistersLink'
                                            },

                                            {
                                                name: 'loadProfileCount',
                                                text: Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'Load profile types'),
                                                itemId: 'deviceTypeDetailLoadProfilesLink'
                                            },

                                            {
                                                name: 'logBookCount',
                                                text: Uni.I18n.translate('devicetype.logbooks', 'MDC', 'Logbook types'),
                                                itemId: 'deviceTypeDetailLogBooksLink'
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        columnWidth: 0.5,
                                        fieldLabel: Uni.I18n.translate('devicetype.deviceConfigurations', 'MDC', 'Device configurations'),
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
                                                text: '',
                                                itemId: 'deviceConfigurationsDetailLink'
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
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});


