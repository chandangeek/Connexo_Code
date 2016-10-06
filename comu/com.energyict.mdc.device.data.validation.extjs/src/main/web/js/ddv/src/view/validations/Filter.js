Ext.define('Ddv.view.validations.Filter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'ddv-validations-filter',
    store: 'Ddv.store.Validations',
    filterDefault: {},
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'deviceGroups',
                emptyText: Uni.I18n.translate('validations.filter.deviceGroups', 'DDV', 'Device groups'),
                itemId: 'validations-topfilter-device-group',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                width: 181,
                store: 'Ddv.store.DeviceGroups'
            },
            {
                type: 'interval',
                dataIndex: 'between',
                dataIndexFrom: 'from',
                dataIndexTo: 'to',
                itemId: 'validations-topfilter-between',
                text: Uni.I18n.translate('validations.filter.period', 'DDV', 'Period'),
                defaultFromDate: me.filterDefault.from,
                defaultToDate: me.filterDefault.to
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfSuspects',
                itemId: 'validations-topfilter-amount-of-suspects',
                text: Uni.I18n.translate('validations.filter.amountOfSuspects', 'DDV', 'Amount of suspects')
            },
            {
                type: 'combobox',
                width: 181,
                dataIndex: 'validator',
                emptyText: Uni.I18n.translate('validations.filter.validator', 'DDV', 'Validator'),
                itemId: 'validations-topfilter-validator',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Ddv.store.Validators'
            }
        ];
        me.callParent(arguments);
    },

    enableClearAll: function (filters) {
        var me = this,
            from = _.find(filters, function (item) {
                return item.property == 'from'
            }),
            to = _.find(filters, function (item) {
                return item.property == 'to'
            }),
            isDefault = from && me.filterDefault.from && from.value == me.filterDefault.from.getTime()
                && to && me.filterDefault.to && to.value == me.filterDefault.to.getTime();

        Ext.suspendLayouts();
        me.down('button[action=clearAll]').setDisabled(isDefault);
        me.down('#validations-topfilter-between button[action=clear]').setDisabled(isDefault);
        Ext.resumeLayouts(true);
    }
});