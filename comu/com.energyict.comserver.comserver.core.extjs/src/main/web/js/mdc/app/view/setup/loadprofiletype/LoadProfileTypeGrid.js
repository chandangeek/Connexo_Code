Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileTypeGrid',
    itemId: 'loadProfileTypeGrid',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    columns: {
        items: [
            {
                header: 'Name',
                dataIndex: 'name',
                flex: 1
            },
            {
                header: 'OBIS code',
                dataIndex: 'obisCode',
                flex: 1
            },
            {
                header: 'Interval',
                dataIndex: 'timeDuration',
                renderer: function (value) {
                    var intervalRecord = Ext.getStore('Mdc.store.Intervals').getById(value.id);

                    return intervalRecord.get('name');
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu'
            }
        ]
    },
    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            dock: 'top',
            displayMsg: Uni.I18n.translate('loadProfileTypes.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} load profile types'),
            displayMoreMsg: Uni.I18n.translate('loadProfileTypes.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} load profile types'),
            emptyMsg: Uni.I18n.translate('loadProfileTypes.pagingtoolbartop.emptyMsg', 'MDC', 'There are no load profile types to display'),
            items: [
                '->',
                {
                    text: Uni.I18n.translate('loadProfileTypes.add', 'MDC', 'Add load profile type'),
                    itemId: 'addLoadProfileType',
                    xtype: 'button',
                    action: 'addloadprofiletypeaction',
                    hrefTarget: '',
                    href: '#/administration/loadprofiletypes/create'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('loadProfileTypes.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Load profile types per page')
        }
    ],

    initComponent: function () {
        var store = this.store,
            pagingToolbarTop = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbartop';
            }),
            pagingToolbarBottom = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbarbottom';
            });

        pagingToolbarTop && (pagingToolbarTop.store = store);
        pagingToolbarBottom && (pagingToolbarBottom.store = store);

        this.callParent(arguments);
    }
});
