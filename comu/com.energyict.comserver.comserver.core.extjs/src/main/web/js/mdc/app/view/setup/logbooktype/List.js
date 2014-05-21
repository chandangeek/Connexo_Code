Ext.define('Mdc.view.setup.logbooktype.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Mdc.view.setup.logbooktype.ActionMenu'
    ],
    alias: 'widget.logbook-list',
    store: 'Mdc.store.Logbook',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                header: 'Name',
                xtype: 'templatecolumn',
                tpl: '<a href="#/administration/logbooktypes/{id}"><tpl if="name">{name}</tpl></a>',
                flex: 5
            },
            {
                header: 'OBIS code',
                xtype: 'templatecolumn',
                tpl: '<tpl if="obis">{obis}</tpl>',
                flex: 5
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.logbooktype.ActionMenu'
            }
        ]
    }
});
