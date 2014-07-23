Ext.define('Mdc.view.setup.deviceregisterdata.MainGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceregisterreportmaingrid',
    itemId: 'deviceRegisterReportMainGrid',
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            dock: 'top',
            displayMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} readings'),
            displayMoreMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} readings'),
            emptyMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.emptyMsg', 'MDC', 'There are no readings to display')
        },
        {
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            deferLoading: true,
            itemsPerPageMsg: Uni.I18n.translate('device.registerData.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Readings per page')
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