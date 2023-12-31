/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.registersData.event.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.register-data-event-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    register: null,

    initComponent: function () {
        var me = this,
            readingType = me.register.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined;

        me.columns = [
            {
                header: Uni.I18n.translate('general.register.eventDate', 'MDC', 'Event date'),
                dataIndex: 'eventDate',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.collectedValueWithUnit', 'MDC', 'Collected ({0})', unit),
                dataIndex: 'collectedValue',
                flex: 1,
                align: 'right'
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

        if (me.register.get('registerType') == 'EVENT_BILLING_VALUE') {
            me.columns.unshift(
                {
                    header: Uni.I18n.translate('general.measurementPeriod', 'MDC', 'Measurement period'),
                    dataIndex: 'measurementPeriod',
                    flex: 2,
                    renderer: function (value) {
                        if (!Ext.isEmpty(value)) {
                            var endDate = new Date(value.end);
                            if (value.start && value.end) {
                                var startDate = new Date(value.start);
                                return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate);
                            } else {
                                return Uni.DateTime.formatDateTimeShort(endDate);
                            }
                        }
                        return '-';
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
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {usagePointId: me.usagePointId},
                    {registerId: me.register.get('id')}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('device.registerData.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Readings per page')
            }
        ];

        me.callParent(arguments);
    }
});