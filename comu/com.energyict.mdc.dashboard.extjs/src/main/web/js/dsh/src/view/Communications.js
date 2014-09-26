Ext.define('Dsh.view.Communications', {
    extend: 'Uni.view.container.ContentContainer',
    //   extend: 'Ext.container.Container',
    alias: 'widget.communications-details',
    itemId: 'communicationsdetails',
    overflowY: 'auto',
    requires: [
        'Dsh.view.widget.CommunicationsList',
        'Dsh.view.widget.PreviewCommunication',
        'Dsh.view.widget.CommunicationSideFilter',
        'Dsh.view.widget.PreviewConnection'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.communication.title', 'DSH', 'Communications'),
        },
        {
            xtype: 'filter-top-panel',
            itemId: 'dshcommunicationsfilterpanel'
        },
        {
            xtype: 'preview-container',
            grid: {
                xtype: 'communications-list',
                itemId: 'communicationslist',
                dockedItems: [
                    {
                        itemId: 'pagingtoolbartop',
                        xtype: 'pagingtoolbartop',
                        dock: 'top',
                        store: 'Dsh.store.CommunicationTasks',
                        displayMsg: Uni.I18n.translate('communication.widget.details.displayMsg', 'DDSH', '{0} - {1} of {2} communications'),
                        displayMoreMsg: Uni.I18n.translate('communication.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} communications'),
                        emptyMsg: Uni.I18n.translate('communication.widget.details.emptyMsg', 'DSH', 'There are no communications to display')
                    },
                    {
                        itemId: 'pagingtoolbarbottom',
                        xtype: 'pagingtoolbarbottom',
                        store: 'Dsh.store.CommunicationTasks',
                        dock: 'bottom',
                        deferLoading: true,
                        itemsPerPageMsg: Uni.I18n.translate('communication.widget.details.itemsPerPage', 'DSH', 'Communications per page')
                    }
                ]
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('workspace.dataCommunication.communication.empty.title', 'DSH', 'No communications found'),
                reasons: [
                    Uni.I18n.translate('workspace.dataCommunication.communication.empty.list.item1', 'DSH', 'No communications in the system.'),
                    Uni.I18n.translate('workspace.dataCommunication.communication.empty.list.item2', 'DSH', 'No communications found due to applied filters.')
                ]
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

    side: [
        {
            xtype: 'dsh-comm-side-filter',
            itemId: 'dshcommunicationssidefilter'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});

