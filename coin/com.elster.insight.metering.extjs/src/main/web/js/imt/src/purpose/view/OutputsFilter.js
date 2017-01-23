Ext.define('Imt.purpose.view.OutputsFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.outputs-filter',
    store: 'Imt.purpose.store.Outputs',
    hasDefaultFilters: true,
    defaultPeriod: null,

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                itemId: 'periods-combo',
                dataIndex: 'periodId',
                valueField: 'id',
                store: 'Imt.usagepointmanagement.store.Periods',
                displayField: 'name',
                queryMode: 'local',
                editable: false,
                forceSelection: true,
                loadStore: false,
                value: me.defaultPeriod
            }
        ];

        me.callParent(arguments);
    }
});