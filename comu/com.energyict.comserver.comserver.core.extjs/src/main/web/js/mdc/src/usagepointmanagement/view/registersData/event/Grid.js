/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.registersData.event.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.register-data-event-grid',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],
    plugins: [
        {
            ptype: 'bufferedrenderer'
        }
    ],
    register: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                dataIndex: 'collectedValue',
                flex: 1,
                align: 'right'
            },
            {
                header: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement time'),
                dataIndex: 'eventDate',
                flex: 1,
                renderer: function (value) {
                    return Uni.DateTime.formatDateShort(new Date(value));
                }
            },
            {
                dataIndex: 'validationResult',
                flex: 1,
                renderer: function (value) {
                    var validationMap = {
                        NOT_VALIDATED: '<span class="icon-flag6" style="margin-left: -15px; line-height: 12px" data-qtip="' + Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated') + '"></span>',
                        SUSPECT: '<span class="icon-flag5" style="margin-left: -15px; color:red; line-height: 12px" data-qtip="' + Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect') + '"></span>'
                    };
                    return validationMap[value];
                }
            }
        ];

        if (true) {
            me.columns.unshift(
                {
                    header: Uni.I18n.translate('general.measurementPeriod', 'MDC', 'Measurement period'),
                    dataIndex: 'measurementPeriod',
                    flex: 1,
                    renderer: function (interval) {
                        return Uni.I18n.translate(
                            'general.dateAtTime', 'MDC', '{0} at {1}',
                            [Uni.DateTime.formatDateLong(new Date(interval.start)), Uni.DateTime.formatTimeLong(new Date(interval.end))], false);
                    }
                }
            );
        }

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                isFullTotalCount: true,
                noBottomPaging: false,
                usesExactCount: true,
                displayMsg: Uni.I18n.translate('usagePointChannelData.pagingtoolbartop.displayMsg', 'MDC', '{2} readings'),
                emptyMsg: Uni.I18n.translate('usagePointChannelData.pagingtoolbartop.emptyMsg', 'MDC', 'There are no readings to display')
            }
        ];

        me.callParent(arguments);
    }
});