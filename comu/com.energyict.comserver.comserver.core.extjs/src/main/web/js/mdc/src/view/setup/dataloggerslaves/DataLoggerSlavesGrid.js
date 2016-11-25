Ext.define('Mdc.view.setup.dataloggerslaves.DataLoggerSlavesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dataLoggerSlavesGrid',
    itemId: 'mdc-dataloggerslaves-grid',
    store: 'Mdc.store.DataLoggerSlaves',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DataLoggerSlaves',
        'Mdc.view.setup.dataloggerslaves.DataLoggerSlavesActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                renderer: function (value, meta, record) {
                    var href = me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(record.get('name'))});
                    return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                },
                getSortParam: function() { Ext.emptyFn }, // We don't want a sort icon in the header
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.serialNumber', 'MDC', 'Serial number'),
                dataIndex: 'serialNumber',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'deviceTypeName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.configuration', 'MDC', 'Configuration'),
                dataIndex: 'deviceConfigurationName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.linkedOn', 'MDC', 'Linked on'),
                dataIndex: 'linkingTimeStamp',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'dataloggerslaves-action-menu',
                    itemId: 'mdc-dataloggerslaves-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                deferLoading: true,
                dock: 'top',
                displayMsg: Uni.I18n.translate('dataLoggerSlaves.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} data logger slaves'),
                displayMoreMsg: Uni.I18n.translate('dataLoggerSlaves.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} data logger slaves'),
                emptyMsg: Uni.I18n.translate('dataLoggerSlaves.pagingtoolbartop.emptyMsg', 'MDC', 'There are no data logger slaves to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.linkDataLoggerSlave', 'MDC', 'Link data logger slave'),
                        itemId: 'mdc-dataloggerslavesgrid-link-slave-btn',
                        privileges: Mdc.privileges.Device.administrateDevice
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                deferLoading: true,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('dataLoggerSlaves.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Data logger slaves per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});