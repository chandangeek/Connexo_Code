Ext.define('Dsh.view.Connections', {
//    extend: 'Uni.view.container.ContentContainer',
    extend: 'Ext.container.Container',
    alias: 'widget.connections-details',
    itemId: 'connectionsdetails',
    requires: [
        'Dsh.view.widget.ConnectionsList'
    ],
    items: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.connections.title', 'DSH', 'Connections'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'connections-list'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('workspace.dataCommunication.connections.empty.title', 'DSH', 'No connections found'),
                        reasons: [
                            Uni.I18n.translate('workspace.dataCommunication.connections.empty.list.item1', 'DSH', 'No connections in the system.'),
                            Uni.I18n.translate('workspace.dataCommunication.connections.empty.list.item2', 'DSH', 'No connections found due to applied filters.')
                        ]
                    },
                    previewComponent: {}
                }
            ]
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});
