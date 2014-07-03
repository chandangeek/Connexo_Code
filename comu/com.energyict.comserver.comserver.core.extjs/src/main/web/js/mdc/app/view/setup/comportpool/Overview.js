Ext.define('Mdc.view.setup.comportpool.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comPortPoolOverview',
    requires: [
        'Mdc.view.setup.comportpool.ActionMenu',
        'Mdc.view.setup.comportpool.SubMenu'
    ],
    side: {
        xtype: 'panel',
        ui: 'medium',
        title: Uni.I18n.translate('', 'MDC', 'Communication port pools'),
        width: 350,
        items: [{
            xtype: 'comportpoolsubmenu',
            itemId: 'comportpoolsubmenu'
        }]
    },
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
                    title: 'Overview',
                    flex: 1
                },
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    iconCls: 'x-uni-action-iconD',
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
                            name: 'direction'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comPortPool.preview.type', 'MDC', 'Type'),
                            name: 'type'
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
                            fieldLabel: Uni.I18n.translate('comPortPool.preview.protocolDetection', 'MDC', 'Protocol detection'),
                            name: 'discoveryProtocolPluggableClassId',
                            renderer: function (val) {
                                var protDetect = val ? Ext.getStore('Mdc.store.DeviceDiscoveryProtocols').getById(val) : null;
                                return protDetect ? protDetect.get('name') : '';
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comPortPool.preview.communicationPorts', 'MDC', 'Communication ports'),
                            name: 'comportslink'
                        }
                    ]
                }
            ]
        }
    ]
});