Ext.define('Dsh.view.Communications', {
    extend: 'Uni.view.container.ContentContainer',
    //   extend: 'Ext.container.Container',
    alias: 'widget.communications-details',
    itemId: 'communicationsdetails',
    overflowY: 'auto',
    requires: [
        'Dsh.view.widget.CommunicationsList',
        'Dsh.view.widget.PreviewCommunication'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.communication.title', 'DSH', 'Communications'),
            items: [
                {
                    xtype: 'filter-top-panel'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'communications-list',
                        itemId: 'communicationslist'
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
                        items: [
                            {
                                xtype: 'preview_communication',
                                itemId: 'communicationdetails'
                            },
                            {
                                style: {
                                    'margin-top' : '32px'
                                },
                                xtype: 'preview_connection',
                                itemId: 'connectiondetails'
                            }
                        ]
                    }
                }
            ]
        }
    ],

    side: [
        {
            xtype: 'dsh-side-filter',
            itemId: 'dshcommunicationssidefilter'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});

