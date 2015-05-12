Ext.define('Dsh.view.Communications', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communications-details',
    itemId: 'communicationsdetails',
    requires: [
        'Dsh.view.widget.CommunicationsList',
        'Dsh.view.widget.PreviewCommunication',
        'Dsh.view.widget.CommunicationsTopFilter',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.store.CommunicationTasks'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.communication.title', 'DSH', 'Communications'),
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'dsh-view-widget-communicationstopfilter'
                }
            ]
        },
        {
            xtype: 'preview-container',
            grid: {
                xtype: 'communications-list',
                itemId: 'communicationslist'
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('workspace.dataCommunication.communication.empty.title', 'DSH', 'No communications found'),
                reasons: [
                    Uni.I18n.translate('workspace.dataCommunication.communication.empty.list.item1', 'DSH', 'No communications in the system.'),
                    Uni.I18n.translate('workspace.dataCommunication.communication.empty.list.item2', 'DSH', 'No communications found due to applied filters.')
                ],
                margins: '16 0 0 0'
            },
            previewComponent: {
                hidden: true,
                items: [
                    {
                        xtype: 'preview_communication',
                        itemId: 'communicationdetails'
                    },
                    {
                        style: {
                            'margin-top': '32px'
                        },
                        xtype: 'preview_connection',
                        itemId: 'connectiondetails'
                    }
                ]
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

