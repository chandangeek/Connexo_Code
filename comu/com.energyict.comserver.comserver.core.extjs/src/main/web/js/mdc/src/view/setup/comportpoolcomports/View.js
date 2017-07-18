/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comportpoolcomports.View', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comPortPoolsComPortsView',
    itemId: 'comPortPoolsComPortsView',

    requires: [
        'Mdc.view.setup.comportpoolcomports.Grid',
        'Mdc.view.setup.comportpoolcomports.Preview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.comportpool.SideMenu'
    ],
    poolId: null,

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('comServerComPorts.communicationPorts', 'MDC', 'Communication ports'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'comPortPoolComPortsGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-comports',
                        title: Uni.I18n.translate('comPortPoolPorts.empty.title', 'MDC', 'No communication ports found'),
                        reasons: [
                            Uni.I18n.translate('comPortPoolPorts.empty.list.item1', 'MDC', 'No communication ports are associated to this communication port pool.')
                        ],
                        stepItems: [
                            {
                                action: 'addComPort',
                                text: Uni.I18n.translate('comServerComPorts.add', 'MDC', 'Add communication port'),
                                privileges: Mdc.privileges.Communication.admin
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'comPortPoolComPortPreview'
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