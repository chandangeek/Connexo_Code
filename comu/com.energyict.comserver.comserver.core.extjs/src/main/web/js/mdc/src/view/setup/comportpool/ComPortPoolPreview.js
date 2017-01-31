/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comportpool.ComPortPoolPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comPortPoolPreview',
    requires: [
        'Mdc.store.ComPortPools',
        'Mdc.store.DeviceDiscoveryProtocols'
    ],
    itemId: 'comportpoolpreview',
    layout: 'fit',
    title: Uni.I18n.translate('comserver.details', 'MDC', 'Details'),
    frame: true,
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.Communication.admin,
            menu: {
                xtype: 'comportpool-actionmenu',
                itemId: 'comportpoolViewMenu'
            }
        }
    ],
    items: {
        xtype: 'form',
        itemId: 'comServerDetailsForm',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comPortPool.preview.direction', 'MDC', 'Direction'),
                        name: 'direction',
                        renderer: function (value) {
                            if (value === 'Inbound') {
                                return Uni.I18n.translate('general.inbound', 'MDC', 'Inbound');
                            } else {
                                return Uni.I18n.translate('general.outbound', 'MDC', 'Outbound');
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                        name: 'comPortType',
                        renderer: function (value) {
                            return value && value.localizedValue;
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                        name: 'active',
                        renderer: function (val) {
                            val ? val = Uni.I18n.translate('general.active', 'MDC', 'Active') : val = Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                            return val;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comPortPool.preview.protocolDetection', 'MDC', 'Protocol detection'),
                        name: 'discoveryProtocolPluggableClassId',
                        renderer: function (val) {
                            var protDetect = val ? Ext.getStore('Mdc.store.DeviceDiscoveryProtocols').getById(val) : null;
                            return protDetect ? Ext.String.htmlEncode(protDetect.get('name')) : '';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comPortPool.preview.communicationPorts', 'MDC', 'Communication ports'),
                        htmlEncode: false,
                        name: 'comportslink'
                    }
                ]
            }
        ]
    }

});
