Ext.define('Mdc.view.setup.commandrules.AddCommandsToRuleFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.AddCommandsToRuleFilter',

    store: 'Mdc.store.Commands',
    defaultFilters: null,

    requires: [
        'Mdc.store.Commands',
        'Mdc.store.CommandCategories'
    ],

    initComponent: function () {
        var me = this,
            selectedCommandsFilter;

        if (!Ext.isEmpty(me.defaultFilters)) {
            me.hasDefaultFilters = true;
            selectedCommandsFilter = me.defaultFilters.selectedcommands
                ? me.defaultFilters.selectedcommands
                : null;
        }

        me.filters = [
            {
                type: 'combobox',
                multiSelect: true,
                dataIndex: 'categories',
                emptyText: Uni.I18n.translate('general.category', 'MDC', 'Category'),
                displayField: 'name',
                valueField: 'id',
                itemId: 'mdc-commands-filter-category-combo',
                store: 'Mdc.store.CommandCategories'
            },
            {
                type: 'noui',
                dataIndex: 'selectedcommands',
                itemId: 'mdc-commands-filter-selectedCommandsFilter',
                initialValue: selectedCommandsFilter,
                value: selectedCommandsFilter
            }
        ];

        me.callParent(arguments);
    }
});