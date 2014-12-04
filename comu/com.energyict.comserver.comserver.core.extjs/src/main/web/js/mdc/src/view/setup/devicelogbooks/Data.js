Ext.define('Mdc.view.setup.devicelogbooks.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLogbookData',
    itemId: 'deviceLogbookData',
    requires: [
        'Mdc.view.setup.devicelogbooks.SubMenuPanel',
        'Mdc.view.setup.devicelogbooks.SideFilter',
        'Mdc.view.setup.devicelogbooks.DataSortingToolbar',
        'Mdc.view.setup.devicelogbooks.DataTableView',
        'Uni.component.filter.view.FilterTopPanel'
    ],

    router: null,

    content: {
        ui: 'large',
        items: [
            {
                xtype: 'filter-top-panel',
                itemId: 'device-logbook-data-filter-toolbar',
                emptyText: Uni.I18n.translate('general.none', 'MDC', 'None'),
                hideEmpty: false
            },
            {
                xtype: 'menuseparator'
            },
            {
                xtype: 'deviceLogbookDataSortingToolbar',
                itemId: 'deviceLogbookDataSortingToolbar'
            },
            {
                xtype: 'deviceLogbookDataTableView',
                itemId: 'deviceLogbookDataTableView'
            }
        ]
    },

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    }
});