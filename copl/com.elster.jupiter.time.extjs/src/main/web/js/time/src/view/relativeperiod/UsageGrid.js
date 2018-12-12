/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.UsageGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.relative-period-usage-grid',
    store: 'Tme.store.RelativePeriodUsage',
    requires: [
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Tme.store.RelativePeriodUsage'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.usage', 'TME', 'Usage'),
                dataIndex: 'task',
                flex: 1
            },
            {
                header: Uni.I18n.translate('relativeperiod.usage.type', 'TME', 'Category'),
                dataIndex: 'type',
                flex: 1
            },
            {
                header: Uni.I18n.translate('relativeperiod.usage.application', 'TME', 'Application'),
                dataIndex: 'application',
                flex: 1
            },
            {
                header: Uni.I18n.translate('relativeperiod.usage.nextRun', 'TME', 'Next run'),
                dataIndex: 'nextRun',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                },
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('relativeperiod.usage.pagingtoolbartop.displayMsg', 'TME', '{0} - {1} of {2} usages'),
                displayMoreMsg: Uni.I18n.translate('relativeperiod.usage.pagingtoolbartop.displayMoreMsg', 'TME', '{0} - {1} of more than {2} usages')
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('relativeperiod.usage.pagingtoolbarbottom.usagesPerPage', 'TME', 'Usages per page')
            }
        ];

        me.callParent(arguments);
    }




});