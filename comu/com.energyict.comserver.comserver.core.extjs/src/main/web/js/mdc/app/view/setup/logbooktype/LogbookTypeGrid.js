Ext.define('Mdc.view.setup.logbooktype.LogbookTypeGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.logbookTypeGrid',
    itemId: 'logbookTypeGrid',
    store: 'Mdc.store.Logbook',
    requires: [
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Mdc.view.setup.logbooktype.LogbookTypeActionMenu'
    ],
    dockedItems: [
        {
            xtype: 'toolbar',
            items: [
                {
                    xtype: 'container',
                    name: 'logbookTypeRangeCounter',
                    itemId: 'logbookTypeRangeCounter',
                    flex: 1
                },
                {
                    xtype: 'button',
                    itemId: 'logbookTypeCreateActionButton',
                    margin: '10 0 0 0',
                    text: Uni.I18n.translate('logbooktype.add', 'MDC', 'Add logbook type'),
                    hrefTarget: '',
                    href: '#/administration/logbooktypes/create'
                }
            ]
        }
    ],
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'nameColumn',
                header: Uni.I18n.translate('logbooktype.name', 'MDC', 'Name'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="name">{name}</tpl>',
                flex: 5
            },
            {
                itemID: 'obisColumn',
                header: Uni.I18n.translate('logbooktype.obis', 'MDC', 'OBIS code'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="obis">{obis}</tpl>',
                flex: 5
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.logbooktype.LogbookTypeActionMenu'
            }
        ]
    }
});
