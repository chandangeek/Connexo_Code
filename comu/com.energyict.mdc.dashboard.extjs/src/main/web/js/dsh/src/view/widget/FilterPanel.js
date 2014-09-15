Ext.define('Dsh.view.widget.FilterPanel', {
    extend: 'Ext.panel.Panel',
    alias: "widget.connections-filter-panel",
    border: true,
    mixins: [
        'Uni.component.filter.view.RecordBounded'
    ],
    header: false,
    collapsible: false,
    hidden: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            title: Uni.I18n.translate('', 'MDC', 'Filter'),
            xtype: 'filter-toolbar',

            emptyText: 'None'
        },
        {
            xtype: 'menuseparator'
        }
    ]

});
