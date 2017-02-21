/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comportpool.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comPortPoolOverview',
    requires: [
        'Mdc.view.setup.comportpool.ActionMenu',
        'Mdc.view.setup.comportpool.SideMenu'
    ],
    poolId: null,
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
                    title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                    flex: 1
                },
                {
                    xtype: 'uni-button-action',
                    privileges: Mdc.privileges.Communication.admin,
                    menu: {
                        xtype: 'comportpool-actionmenu'
                    }
                }
            ]
        },
        {
            xtype: 'form',
            itemId: 'comPortPoolOverviewForm',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 1
            },
            items: [
                {
                    xtype: 'fieldcontainer',
                    labelAlign: 'top',
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                            name: 'name'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comPortPool.preview.direction', 'MDC', 'Direction'),
                            name: 'direction'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                            name: 'comPortType',
                            renderer: function (value) {
                                return value && value.localizedValue;
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                            name: 'active',
                            renderer: function (val) {
                                val ? val = 'Active' : val = 'Inactive';
                                return val;
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comPortPool.preview.communicationPorts', 'MDC', 'Communication ports'),
                            htmlEncode: false,
                            name: 'comportslink'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comPortPool.preview.protocolDetection', 'MDC', 'Protocol detection'),
                            name: 'discoveryProtocolPluggableClassId',
                            renderer: function (val) {
                                var protDetect = val ? Ext.getStore('Mdc.store.DeviceDiscoveryProtocols').getById(val) : null;
                                return protDetect ? Ext.String.htmlEncode(protDetect.get('name')) : '';
                            }
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    hidden: true,
                    itemId: 'protocolDetectionDetails',
                    fieldLabel: Uni.I18n.translate('comportPool.protocolDetectionDetails', 'MDC', 'Protocol detection details'),
                    labelAlign: 'top',
                    layout: 'vbox'
                },
                {
                    xtype: 'property-form',
                    isEdit: false,
                    defaults: {
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 250
                    }
                }
            ]
        }


    ],

    initComponent: function () {
        var me = this;
        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'comportpoolsidemenu',
                    itemId: 'comportpoolsidemenu',
                    poolId: me.poolId
                }
            ]
        };
        me.callParent(arguments)
    }
});