Ext.define('Mdc.view.setup.deviceregisterconfiguration.RegisterReadingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceRegisterReadingsGrid',
    store: undefined,
    router: undefined,
    mRID: undefined,
    showDataLoggerSlaveColumn: false,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterReadings',
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.register', 'MDC', 'Register'),
                dataIndex: 'register',
                renderer: function (value, metaData, record) {
                    var to = moment(record.get('timeStamp')).add(1, 'minutes').valueOf(),
                        url = '#/devices/' + me.mRID + '/registers/' + value.id + '/data?interval=-' + to;
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                },
                flex: 15
            },
            {
                header: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement time'),
                flex: 10,
                dataIndex: 'timeStamp',
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                }
            },
            {
                header: Uni.I18n.translate('general.from', 'MDC', 'From'),
                flex: 10,
                dataIndex: 'intervalStart',
                itemId: 'mdc-readings-grid-from-column',
                renderer: function (value) {
                    return Ext.isEmpty(value) || value === 0 ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                },
                hidden: true
            },
            {
                header: Uni.I18n.translate('general.to', 'MDC', 'To'),
                flex: 10,
                dataIndex: 'intervalEnd',
                itemId: 'mdc-readings-grid-to-column',
                renderer: function (value) {
                    return Ext.isEmpty(value) || value === 0 ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                },
                hidden: true
            },
            {
                header: Uni.I18n.translate('general.value', 'MDC', 'Value'),
                flex: 10,
                dataIndex: 'valueAndUnit'
            }
        ];

        if (me.showDataLoggerSlaveColumn) {
            me.columns.push(
                {
                    dataIndex: 'dataloggerSlavemRID',
                    flex: 10,
                    header: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
                    renderer: function(value) {
                        if (Ext.isEmpty(value)) {
                            return '-';
                        }
                        var href = me.router.getRoute('devices/device/registers').buildUrl({mRID: encodeURIComponent(value)});
                        return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                    }
                }
            );
        }

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerreadings.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register readings'),
                displayMoreMsg: Uni.I18n.translate('registerreadings.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register readings'),
                emptyMsg: Uni.I18n.translate('registerreadings.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register readings')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('registerreadings.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register readings per page'),
                defaultPageSize: 100,
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    },

    showOrHideBillingColumns: function(showThem) {
        var fromColumn = this.down('#mdc-readings-grid-from-column'),
            toColumn = this.down('#mdc-readings-grid-to-column');

        if (fromColumn) {
            if (showThem) {
                fromColumn.show();
            } else {
                fromColumn.hide();
            }
        }
        if (toColumn) {
            if (showThem) {
                toColumn.show();
            } else {
                toColumn.hide();
            }
        }
    }
});