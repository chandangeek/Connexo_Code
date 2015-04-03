Ext.define('Dlc.devicelifecyclestates.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-life-cycle-states-grid',
    store: 'Dlc.devicelifecyclestates.store.DeviceLifeCycleStates',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dlc.devicelifecyclestates.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.states', 'DLC', 'States'),
                dataIndex: 'sorted_name',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'device-life-cycle-states-action-menu',
                    itemId: 'statesActionMenu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceLifeCycleStates.pagingtoolbartop.displayMsg', 'DLC', '{0} - {1} of {2} states'),
                displayMoreMsg: Uni.I18n.translate('deviceLifeCycleStates.pagingtoolbartop.displayMoreMsg', 'DLC', '{0} - {1} of more than {2} states'),
                emptyMsg: Uni.I18n.translate('deviceLifeCycleStates.pagingtoolbartop.emptyMsg', 'DLC', 'There are no states to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('deviceLifeCycleStates.add', 'DLC', 'Add state'),
                        action: 'addState'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('deviceLifeCycleStates.pagingtoolbarbottom.itemsPerPage', 'DLC', 'States per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

