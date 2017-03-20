/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.RegistersGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usage-point-registers-grid',
    requires: [
        'Uni.util.Common',
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'Mdc.usagepointmanagement.store.Registers',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType',
                flex: 2,
                makeLink: function (record) {
                    var routerArguments = Ext.clone(me.router.arguments);

                    routerArguments.registerId = record.getId();
                    return me.router.getRoute('usagepoints/usagepoint/registers/registerdata').buildUrl(routerArguments);
                }
            },
            {
                header: Uni.I18n.translate('general.measurementTime', 'MDC', 'measurementTime'),
                dataIndex: 'measurementTime',
                flex: 1,
                renderer: function (value) {
                    return value
                        ? Uni.DateTime.formatDateTimeShort(value)
                        : '-';
                }
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('usagePointRegisters.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} registers'),
                displayMoreMsg: Uni.I18n.translate('usagePointRegisters.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} registers'),
                emptyMsg: Uni.I18n.translate('usagePointRegisters.pagingtoolbartop.emptyMsg', 'MDC', 'There are no registers to display'),
                noBottomPaging: true,
                usesExactCount: true
            }
        ];

        me.callParent(arguments);
    }
});