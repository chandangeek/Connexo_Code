Ext.define('Ddv.view.validations.Filter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'ddv-validations-filter',
    store: 'Ddv.store.Validations',
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
                text: Uni.I18n.translate('validations.filter.period', 'DDV', 'Period')
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
        ]
        me.callParent(arguments);
    }
});