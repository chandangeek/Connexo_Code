Ext.define('Mdc.view.setup.comportpoolcomports.AddComPortGrid', {
    extend: 'Uni.view.container.EmptyGridContainer',
    xtype:'comport-selection-grid',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.grid.SelectionGrid',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    config: {
        selectionListeners: undefined
    },
    itemId: 'addComportToComportPoolGrid',

    grid: {
        xtype: 'selection-grid',
        itemId: 'comportGrid',
        counterTextFn: function (count) {
            return Uni.I18n.translatePlural(
                'general.nrOfCommunicationPorts.selected', count, 'MDC',
                'No communication ports selected', '{0} communication port selected', '{0} communication ports selected'
            );
        },
        columns: [
            {
                header: Uni.I18n.translate('general.comServer', 'MDC', 'Communication server'),
                dataIndex: 'comServerName',
                flex: 2
            },
            {
                header: Uni.I18n.translate('comPortPoolComPort.communicationPort', 'MDC', 'Communication port'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'active',
                flex: 3,
                renderer: function (value) {
                    if (value === true) {
                        return Uni.I18n.translate('general.active', 'MDC', 'Active');
                    } else {
                        return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                    }
                }
            }
        ]
    },
    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'no-comports',
        title: Uni.I18n.translate('comServerComPorts.empty.title', 'MDC', 'No communication ports found.'),
        reasons: [Uni.I18n.translate('comServerComPorts.empty.list.item2', 'MDC', 'No communication ports defined for any communication server.'),
                  Uni.I18n.translate('comServerComPorts.empty.list.item3', 'MDC', 'All communication ports are already assigned to this communication port pool.')
        ]
    },
    applySelectionListeners: function(selectionListeners){
        var me = this;
        if (selectionListeners && Ext.isArray(selectionListeners)){
            Ext.Array.each(selectionListeners, function(listener) {
                me.down('grid').view.getSelectionModel().addListener('selectionchange', listener.onSelectChange );
            })
        }
        return selectionListeners;
    },
    getSelection: function(){
        return this.down('grid').view.getSelectionModel().getSelection();
    }
});