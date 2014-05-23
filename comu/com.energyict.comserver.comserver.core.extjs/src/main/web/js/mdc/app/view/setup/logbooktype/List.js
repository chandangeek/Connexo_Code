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
                itemId: 'nameColumn',
                header: 'Name',
                xtype: 'templatecolumn',
                tpl: '<a href="#/administration/logbooktypes/{id}"><tpl if="name">{name}</tpl></a>',
                flex: 5
            },
            {
                itemID: 'OBIScolumn',
                header: 'OBIS code',
                xtype: 'templatecolumn',
                tpl: '<tpl if="obis">{obis}</tpl>',
                flex: 5
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.logbooktype.ActionMenu'
            }
        ]
    }
});
