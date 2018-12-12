/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.ChannelsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usage-point-channels-grid',
    requires: [
        'Uni.util.Common',
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'Mdc.usagepointmanagement.store.Channels',
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

                    routerArguments.channelId = record.getId();
                    return me.router.getRoute('usagepoints/usagepoint/channels/channeldata').buildUrl(routerArguments);
                }
            },
            {
                header: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                dataIndex: 'interval',
                flex: 1,
                renderer: function (value) {
                    return Ext.isObject(value)
                        ? Uni.util.Common.translateTimeUnit(value.count, value.timeUnit)
                        : '';
                }
            },
            {
                header: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                dataIndex: 'dataUntil',
                flex: 1,
                renderer: function (value) {
                    return value
                        ? Uni.DateTime.formatDateTimeShort(new Date(value))
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
                displayMsg: Uni.I18n.translate('usagePointChannels.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} channels'),
                displayMoreMsg: Uni.I18n.translate('usagePointChannels.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} channels'),
                emptyMsg: Uni.I18n.translate('usagePointChannels.pagingtoolbartop.emptyMsg', 'MDC', 'There are no channels to display'),
                noBottomPaging: true,
                usesExactCount: true
            }
        ];

        me.callParent(arguments);
    }
});