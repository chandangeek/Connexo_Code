/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comservercomports.AddComPortPool', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'addComPortPool',
    itemId: 'addComPortPoolToComPort',

    requires: [
        'Mdc.view.setup.comservercomports.AddComPortPoolsGrid'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('comPortPool.addComPortPool', 'MDC', 'Add communication port pool'),
                ui: 'large',
                items: {
                    xtype: 'emptygridcontainer',
                    grid: {
                        xtype: 'add-com-port-pools-grid',
                        itemId: 'addComPortPoolsGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('comServerComPorts.addComPools.empty.title', 'MDC', 'No communication port pools found'),
                        reasons: [
                            Uni.I18n.translate('comServerComPorts.addComPools.empty.list.item1', 'MDC', 'No communication port pools have been defined yet.'),
                            Uni.I18n.translate('comServerComPorts.addComPools.empty.list.item2', 'MDC', 'All communication port pools have already been added to the port.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('comServerComPorts.addPools.,manageComPorts', 'MDC', 'Manage communication port pools'),
                                href: me.router.getRoute('administration/comportpools').buildUrl()
                            }
                        ]
                    }
                },
                bbar: [
                    {
                        xtype: 'button',
                        itemId: 'cancel-add-communication-port-pool',
                        ui: 'link',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        href: me.router.getRoute().buildUrl(),
                        hidden: true
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    updateCancelHref: function (comServerId, comPortId) {
        var me = this;

        me.down('add-com-port-pools-grid').updateCancelHref(comServerId, comPortId);
        me.down('#cancel-add-communication-port-pool').setHref(Ext.isDefined(comPortId)
            ? me.router.getRoute('administration/comservers/detail/comports/edit').buildUrl({id: comServerId, direction: 'outbound', comPortId: comPortId})
            : me.router.getRoute('administration/comservers/detail/comports/addOutbound').buildUrl({id: comServerId}));
    }
});


