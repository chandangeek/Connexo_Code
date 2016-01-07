Ext.define('Ddv.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.ddv-validationoverview-grid',
    store: 'Ddv.store.ValidationOverview',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('validation.validationOverview.mRID', 'DDV', 'MRID'),
                dataIndex: 'mrid',
                renderer: function (value, b, record) {
                    return '<a href="#/devices/' + record.get('mrid') + '/validationresults/data">' + Ext.String.htmlEncode(value) + '</a>';
                },
                fixed: true,
                flex: 1
            },
            {
                header: Uni.I18n.translate('validation.validationOverview.serialNumber', 'DDV', 'Serial number'),
                dataIndex: 'serialNumber',
                fixed: true,
                flex: 1
            },
            {
                header: Uni.I18n.translate('validation.validationOverview.deviceType', 'DDV', 'Type'),
                dataIndex: 'deviceType',
                fixed: true,
                flex: 1
            },
            {
                header: Uni.I18n.translate('validation.validationOverview.configuration', 'DDV', 'Configuration'),
                dataIndex: 'deviceConfig',
                fixed: true,
                flex: 1
            }
        ];

        if (!me.isHidden()) {
            me.dockedItems = [
                {
                    xtype: 'pagingtoolbartop',
                    store: me.store,
                    dock: 'top',
                    displayMsg: Uni.I18n.translate('validation.validationOverview.pagingtoolbartop.displayMsg', 'DDV', '{0} - {1} of {2} devices with suspects'),
                    displayMoreMsg: Uni.I18n.translate('validation.validationOverview.pagingtoolbartop.displayMoreMsg', 'DDV', '{0} - {1} of more than {2} devices with suspects'),
                    emptyMsg: Uni.I18n.translate('validation.validationOverview.pagingtoolbartop.emptyMsg', 'DDV', 'There are no devices with suspects to display')
                },
                {
                    xtype: 'pagingtoolbarbottom',
                    store: me.store,
                    itemsPerPageMsg: Uni.I18n.translate('validation.validationOverview.pagingtoolbarbottom.itemsPerPage', 'DDV', 'Devices with suspects per page'),
                    dock: 'bottom'
                }
            ];
        }

        me.callParent(arguments);
    }
});