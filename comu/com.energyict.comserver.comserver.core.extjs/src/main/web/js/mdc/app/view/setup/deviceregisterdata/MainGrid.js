Ext.define('Mdc.view.setup.deviceregisterdata.MainGrid', {
    extend: 'Ext.grid.Panel',

    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    mRID: null,
    registerId: null,

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} readings'),
                displayMoreMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} readings'),
                emptyMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.emptyMsg', 'MDC', 'There are no readings to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {mRID: me.mRID},
                    {registerId: me.registerId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('device.registerData.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Readings per page')
            }
        ];

        me.callParent(arguments);
    }
});