Ext.define('Dsh.view.Connections', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connections-details',
    itemId: 'connectionsdetails',

    requires: [
        'Dsh.view.widget.ConnectionsList',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.view.widget.ConnectionsTopFilter',
        'Dsh.view.widget.connection.CommunicationsList',
        'Dsh.view.widget.connection.PreviewCommunication',
        'Dsh.store.ConnectionTasks'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.connections', 'DSH', 'Connections'),
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'dsh-view-widget-connectionstopfilter'
                }
            ]
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
                    ],
                    margins: '16 0 0 0'
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
                        xtype: 'connection-communications-list',
                        itemId: 'communicationsdetails',
                        store: 'Dsh.store.Communications'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('communication.empty.title', 'DSH', 'No communications found'),
                        reasons: [
                            Uni.I18n.translate('communication.empty.list.item1', 'DSH', 'No communications in the system.'),
                            Uni.I18n.translate('communication.empty.list.item2', 'DSH', 'No communications found due to applied filters.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'preview-connection-communication',
                        itemId: 'communicationpreview'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
