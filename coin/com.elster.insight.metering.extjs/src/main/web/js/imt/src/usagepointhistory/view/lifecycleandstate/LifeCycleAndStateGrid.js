Ext.define('Imt.usagepointhistory.view.lifecycleandstate.LifeCycleAndStateGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.life-cycle-and-state-grid',
    xtype: 'life-cycle-and-state-grid',
    store: 'Imt.usagepointhistory.store.LifeCycleAndState',
    router: null,
    requires: [
        'Uni.grid.column.RemoveAction'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.type', 'IMT', 'Type'),
                dataIndex: 'type_name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.from', 'IMT', 'From'),
                dataIndex: 'fromStateName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.to', 'IMT', 'To'),
                dataIndex: 'toStateName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                dataIndex: 'status_name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.user', 'IMT', 'User'),
                dataIndex: 'user_name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.stateChangeTime', 'IMT', 'State change time'),
                dataIndex: 'transitionTime',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            },
            {
                xtype: 'uni-actioncolumn-remove',
                tooltip: Uni.I18n.translate('general.abort', 'IMT', 'Abort'),
                itemId: 'abort-transition',
                privileges: Imt.privileges.MetrologyConfig.adminValidation,
                showCondition: function (record) {
                    return record.get('userCanManageRequest');
                },
                handler: function (grid, rowIndex, colIndex, item, e, record) {
                    this.fireEvent('abortTransition', record);
                }
            }
        ];

        me.callParent(arguments);
    }
});

