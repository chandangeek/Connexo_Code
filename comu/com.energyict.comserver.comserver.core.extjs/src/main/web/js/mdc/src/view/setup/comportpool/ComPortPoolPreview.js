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
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
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
                        name: 'direction'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comPortPool.preview.type', 'MDC', 'Type'),
                        name: 'type'
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
                            val ? val = 'Active' : val = 'Inactive';
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
