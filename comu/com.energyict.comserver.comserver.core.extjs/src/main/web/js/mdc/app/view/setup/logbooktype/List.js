Ext.define('Mdc.view.setup.logbooktype.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Ext.grid.column.Action'
    ],
    alias: 'widget.logbook-list',
    store: 'Mdc.store.Logbook',
    height: 395,
    forceFit: true,
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                header: 'Name',
                xtype: 'templatecolumn',
                tpl: '<a href="#/setup/logbooktypes/{id}"><tpl if="name">{name}</tpl></a>',
                flex: 5
            },
            {
                header: 'OBIS code',
                xtype: 'templatecolumn',
                tpl: '<tpl if="obis">{obis}</tpl>',
                flex: 5
            },
            {
                xtype: 'actioncolumn',
                iconCls: 'uni-actioncolumn-gear',
                columnWidth: 32,
                fixed: true,
                header: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                sortable: false,
                hideable: false
            }
        ]
    }
});
