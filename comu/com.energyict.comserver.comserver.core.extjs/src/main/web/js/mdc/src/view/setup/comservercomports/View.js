Ext.define('Mdc.view.setup.comservercomports.View', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comServerComPortsView',
    itemId: 'comServerComPortsView',
    serverId: null,
    requires: [
        'Mdc.view.setup.comservercomports.Grid',
        'Mdc.view.setup.comservercomports.Preview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.comservercomports.AddMenu',
        'Mdc.view.setup.comserver.SideMenu'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('comServerComPorts.communicationPorts', 'MDC', 'Communication ports'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'comServerComPortsGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('comServerComPorts.empty.title', 'MDC', 'No communication ports found'),
                        reasons: [
                            Uni.I18n.translate('comServerComPorts.empty.list.item1', 'MDC', 'No communication ports are associated to this communication server.')
                        ],
                        stepItems: [
                            {
                                action: 'addComPort',
                                text: Uni.I18n.translate('comServerComPorts.add', 'MDC', 'Add communication port'),
                                privileges: ['privilege.administrate.communicationAdministration'],
                                menu: {
                                    xtype: 'comServerComPortsAddMenu'
                                }
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'comServerComPortPreview'
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
                title: Uni.I18n.translate('comserver.title.communicationServers', 'MDC', 'Communication servers'),
                width: 300,
                items: [
                {
                    xtype: 'comserversidemenu',
                    itemId: 'comserversidemenu',
                    serverId: me.serverId
                }
            ]
        };
        me.callParent(arguments)
    }
});