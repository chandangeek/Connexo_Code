Ext.define('Dsh.view.Connections', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connections-details',
    itemId: 'connectionsdetails',
    overflowY: 'auto',
    requires: [
        'Dsh.view.widget.ConnectionsList',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.view.widget.SideFilter',
        'Dsh.view.widget.FilterPanel'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.connections.title', 'DSH', 'Connections'),
        },
        {
            xtype: 'connections-filter-panel',
            itemId: 'dshconnectionsfilterpanel'
        },
        {
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
                itemId: 'connectionpreview'
            }
        },
        {
            ui: 'medium',
            itemId: 'comtaskstitlepanel',
            title: ''
        },
        {
            xtype: 'container',
            itemId: 'communicationcontainer'
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
