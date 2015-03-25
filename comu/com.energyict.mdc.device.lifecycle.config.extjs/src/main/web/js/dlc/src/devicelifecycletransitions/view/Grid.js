Ext.define('Dlc.devicelifecycletransitions.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-life-cycle-transitions-grid',
    store: 'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitions',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.transition', 'DLC', 'Transition'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.from', 'DLC', 'From'),
                dataIndex: 'fromState_name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.to', 'DLC', 'To'),
                dataIndex: 'toState_name',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceLifeCycleTransitions.pagingtoolbartop.displayMsg', 'DLC', '{0} - {1} of {2} transitions'),
                displayMoreMsg: Uni.I18n.translate('deviceLifeCycleTransitions.pagingtoolbartop.displayMoreMsg', 'DLC', '{0} - {1} of more than {2} transitions'),
                emptyMsg: Uni.I18n.translate('deviceLifeCycleTransitions.pagingtoolbartop.emptyMsg', 'DLC', 'There are no transitions to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('deviceLifeCycleTransitions.pagingtoolbarbottom.itemsPerPage', 'DLC', 'Transitions per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

