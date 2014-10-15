Ext.define('Dsh.view.Connections', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connections-details',
    itemId: 'connectionsdetails',

    requires: [
        'Dsh.view.widget.ConnectionsList',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.view.widget.SideFilter',
        'Dsh.view.widget.CommunicationsList',
        'Dsh.view.widget.PreviewCommunication'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.connections.title', 'DSH', 'Connections')
        },
        {
            xtype: 'filter-top-panel',
            itemId: 'dshconnectionsfilterpanel'
        },
        {
            xtype: 'panel',
            items: {
                xtype: 'preview-container',
                grid: {
                    xtype: 'connections-list',
                    itemId: 'connectionsdetails'
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('workspace.dataCommunication.connections.empty.title', 'DSH', 'No connections found'),
                    reasons: [
                        Uni.I18n.translate('workspace.dataCommunication.connections.empty.list.item1', 'DSH', 'No connections in the system.'),
                        Uni.I18n.translate('workspace.dataCommunication.connections.empty.list.item2', 'DSH', 'No connections found due to applied filters.')
                    ]
                },
                previewComponent: {
                    xtype: 'preview_connection',
                    itemId: 'connectionpreview',
                    hidden: true
                }
            }
        },
        {
            ui: 'medium',
            itemId: 'communicationspanel',
            padding: 0,
            margin: '16 0 0 0',
            hidden: true,
            title: '',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'communications-list',
                        itemId: 'communicationsdetails',
                        store: 'Dsh.store.Communications'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('communication.widget.details.empty.title', 'DSH', 'No communications found'),
                        reasons: [
                            Uni.I18n.translate('communication.widget.details.empty.list.item1', 'DSH', 'No communications in the system.'),
                            Uni.I18n.translate('communication.widget.details.empty.list.item2', 'DSH', 'No communications found due to applied filters.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'preview_communication',
                        itemId: 'communicationpreview'
                    }
                }
            ]
        }
    ],
    side: [
        {
            xtype: 'dsh-side-filter',
            itemId: 'dshconnectionssidefilter'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
