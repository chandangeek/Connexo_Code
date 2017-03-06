/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.property.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    requires: [],
    alias: 'widget.device-channels-history-grid',
    maxHeight: 450,

    initComponent: function () {
        var me = this,
            readingType = me.channelRecord.get('readingType'),
            unitOfCollectedValues = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined,
            calculatedReadingType = me.channelRecord.get('calculatedReadingType'),
            unitOfCalculatedValues = calculatedReadingType && calculatedReadingType.names ? calculatedReadingType.names.unitOfMeasure : undefined;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                renderer: function (value, metaData, record, rowIndex, colIndex, store) {
                    if (rowIndex >= 1 && store.getAt(rowIndex - 1).get('interval').end == store.getAt(rowIndex).get('interval').end) {
                        return;
                    }

                    var readingQualitiesPresent = !Ext.isEmpty(record.get('readingQualities')),
                        text = value
                            ? Uni.I18n.translate(
                            'general.dateAtTime', 'MDC', '{0} at {1}',
                            [Uni.DateTime.formatDateShort(value), Uni.DateTime.formatTimeShort(value)])
                            : '-',
                        tooltipContent = '',
                        icon = '';

                    if (readingQualitiesPresent) {
                        Ext.Array.forEach(record.get('readingQualities'), function (readingQualityName) {
                            tooltipContent += (readingQualityName + '<br>');
                        });
                        if (tooltipContent.length > 0) {
                            tooltipContent += '<br>';
                            tooltipContent += Uni.I18n.translate('general.deviceQuality.tooltip.moreMessage', 'MDC', 'View reading quality details for more information.');

                            icon = '<span class="icon-price-tags" style="margin-left:10px; position:absolute;" data-qtitle="'
                                + Uni.I18n.translate('general.deviceQuality', 'MDC', 'Device quality') + '" data-qtip="'
                                + tooltipContent + '"></span>';
                        }
                    }
                    return text + icon;
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('historyGrid.changedOn', 'MDC', 'Changed on'),
                dataIndex: 'reportedDateTime',
                flex: 1,
                renderer: function (value, metaData, record) {
                    var val = record.get('version') == 1 ? record.get('interval_end') : value;
                    return val ? Uni.DateTime.formatDateTimeShort(val) : '';
                }
            },
            {
                header: unitOfCalculatedValues
                    ? Uni.I18n.translate('general.calculated', 'MDC', 'Calculated') + ' (' + unitOfCalculatedValues + ')'
                    : Uni.I18n.translate('general.collected', 'MDC', 'Collected') + ' (' + unitOfCollectedValues + ')',
                dataIndex: 'value',
                align: 'right',
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, record.get('mainValidationInfo'));
                },
                flex: 1
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'mainModificationState',
                width: 30,
                emptyText: ' '
            },
            {
                header: Uni.I18n.translate('general.collected', 'MDC', 'Collected') + (unitOfCollectedValues ? ' (' + unitOfCollectedValues + ')' : ''),
                dataIndex: 'collectedValue',
                flex: 1,
                align: 'right',
                hidden: Ext.isEmpty(calculatedReadingType),
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, record.get('bulkValidationInfo'));
                }
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'bulkModificationState',
                width: 30,
                emptyText: ' '
            },
            {
                header: Uni.I18n.translate('historyGrid.changedBy', 'MDC', 'Changed by'),
                dataIndex: 'userName',
                flex: 1
            }
        ];

        me.callParent(arguments);
    },

    formatColumn: function (v, metaData, record, validationInfo) {
        var me = this,
            status = validationInfo.validationResult ? validationInfo.validationResult.split('.')[1] : '',
            icon = '',
            app,
            date,
            tooltipText,
            formattedDate,
            value = Ext.isEmpty(v)
                ? '-'
                : Uni.Number.formatNumber(
                v.toString(),
                me.channelRecord && !Ext.isEmpty(me.channelRecord.get('overruledNbrOfFractionDigits')) ? me.channelRecord.get('overruledNbrOfFractionDigits') : -1
            );

        if (status === 'notValidated') {
            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('general.notValidated', 'MDC', 'Not validated') + '"></span>';
        } else if (validationInfo.confirmedNotSaved) {
            metaData.tdCls = 'x-grid-dirty-cell';
        } else if (status === 'suspect') {
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:red;" data-qtip="'
                + Uni.I18n.translate('general.suspect', 'MDC', 'Suspect') + '"></span>';
        }

        if (validationInfo.estimatedByRule && !record.isModified('value')) {
            date = Ext.isDate(record.get('readingTime')) ? record.get('readingTime') : new Date(record.get('readingTime'));
            formattedDate = Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateLong(date), Uni.DateTime.formatTimeLong(date)]
            );
            app = validationInfo.editedInApp ? validationInfo.editedInApp.name : null;
            tooltipText = !Ext.isEmpty(app)
                ? Uni.I18n.translate('general.estimatedOnXApp', 'MDC', 'Estimated in {0} on {1}', [app, formattedDate])
                : Uni.I18n.translate('general.estimatedOnX', 'MDC', 'Estimated on {0}', formattedDate);
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:#33CC33;" data-qtip="'
                + tooltipText + '"></span>';
        } else if (validationInfo.isConfirmed && !record.isModified('value')) {
            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.confirmed', 'MDC', 'Confirmed') + '"></span>';
        }
        return value + icon;
    }
});