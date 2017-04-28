/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.history.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Imt.purpose.util.TooltipRenderer'
    ],
    alias: 'widget.output-readings-history-grid',
    maxHeight: 450,
    output: null,
    store: null,

    initComponent: function () {
        var me = this,
            readingType = me.output.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined;

        me.columns = [
            {
                header: Uni.I18n.translate('historyGrid.changedOn', 'IMT', 'Changed on'),
                dataIndex: 'reportedDateTime',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                header: unit
                    ? Uni.I18n.translate('general.valueOf', 'IMT', 'Value ({0})', [unit])
                    : Uni.I18n.translate('general.value.empty', 'IMT', 'Value'),
                dataIndex: 'value',
                align: 'right',
                renderer: me.formatColumn,
                flex: 1
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'modificationState',
                width: 30,
                emptyText: ' '
            },
            {
                header: Uni.I18n.translate('historyGrid.changedBy', 'IMT', 'Changed by'),
                dataIndex: 'userName',
                flex: 1
            }
        ];

        if (me.output.get('outputType') === 'channel') {
            me.columns.unshift(me.getChannelIntervalColumn());
        } else {
            me.columns.unshift(me.getRegisterIntervalColumn());
        }

        me.callParent(arguments);
    },

    addProjectedFlag: function (icon) {
        icon += '<span style="margin-left:27px; position:absolute; font-weight:bold; cursor: default" data-qtip="'
            + Uni.I18n.translate('reading.estimated.projected', 'IMT', 'Projected') + '">P</span>';
        return icon;
    },

    formatColumn: function (v, metaData, record) {
        var status = record.get('validationResult') ? record.get('validationResult').split('.')[1] : '',
            value = Ext.isEmpty(v) ? '-' : v,
            estimatedByRule = record.get('estimatedByRule'),
            icon = '';

        if (status === 'notValidated') {
            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.notvalidated', 'IMT', 'Not validated') + '"></span>';
        } else if (status === 'suspect') {
            icon = '<span class="icon-flag5" style="margin-left:10px; color:red; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') + '"></span>';
        } else if (status === 'ok' && record.get('action') == 'WARN_ONLY') {
            icon = '<span class="icon-flag5" style="margin-left:10px; color: #dedc49; position:absolute;" data-qtip="'
                + Uni.I18n.translate('validationStatus.informative', 'IMT', 'Informative') + '"></span>';
        }
        if (!Ext.isEmpty(estimatedByRule)) {
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:#33CC33;" data-qtip="'
                + Uni.I18n.translate('reading.estimated', 'IMT', 'Estimated in {0} on {1} at {2}', [
                    estimatedByRule.application.name,
                    Uni.DateTime.formatDateLong(new Date(estimatedByRule.when)),
                    Uni.DateTime.formatTimeLong(new Date(estimatedByRule.when))
                ], false) + '"></span>';
            if (record.get('isProjected') === true) {
                icon = this.addProjectedFlag(icon);
            }
        } else if (record.get('ruleId') > 0) {
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:#33CC33;"></span>';
            if (record.get('isProjected') === true) {
                icon = this.addProjectedFlag(icon);
            }
        } else if (record.get('isConfirmed')) {
            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.confirmed', 'IMT', 'Confirmed') + '"></span>';
        } else if (record.get('modificationFlag') && record.get('modificationDate') && record.get('isProjected') === true) {
            icon = this.addProjectedFlag(icon);
        }
        return value + icon + '<span>&nbsp;&nbsp;&nbsp;</span>';
    },

    getChannelIntervalColumn: function () {
        return {
            header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'IMT', 'End of interval'),
            dataIndex: 'interval',
            renderer: function (interval, metaData, record, rowIndex, colIndex, store) {
                var text;

                if (rowIndex >= 1 && store.getAt(rowIndex - 1).get('interval').end == store.getAt(rowIndex).get('interval').end) {
                    return;
                }
                text = interval.end
                    ? Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}', [Uni.DateTime.formatDateShort(new Date(interval.end)), Uni.DateTime.formatTimeShort(new Date(interval.end))])
                    : '-';

                return text + Imt.purpose.util.TooltipRenderer.prepareIcon(record);
            },
            flex: 1
        };
    },

    getRegisterIntervalColumn: function () {
        var me = this;

        if ((me.output.get('deliverableType') === 'numerical' || me.output.get('deliverableType') === 'billing') && (me.output.get('isCummulative') || me.output.get('isBilling'))) {
            return {
                header: Uni.I18n.translate('general.measurementPeriod', 'IMT', 'Measurement period'),
                flex: 2,
                dataIndex: 'interval',
                renderer: function (value, meataData, record) {
                    if (!Ext.isEmpty(value)) {
                        var endDate = new Date(value.end);
                        if (!!value.start && !!value.end) {
                            var startDate = new Date(value.start);
                            return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate) + Imt.purpose.util.TooltipRenderer.prepareIcon(record);
                        } else {
                            return Uni.DateTime.formatDateTimeShort(endDate) + Imt.purpose.util.TooltipRenderer.prepareIcon(record);
                        }
                    }
                    return '-';
                }
            };
        } else if (!me.output.get('hasEvent')) {
            return {
                header: Uni.I18n.translate('general.measurementTime', 'IMT', 'Measurement time'),
                flex: 1,
                dataIndex: 'timeStamp',
                renderer: function (value, metaData, record) {
                    return Ext.isEmpty(value) ? '-' : Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',
                        [Uni.DateTime.formatDateShort(new Date(value)), Uni.DateTime.formatTimeShort(new Date(value))]) + Imt.purpose.util.TooltipRenderer.prepareIcon(record);
                }
            };
        }

        if (me.output.get('hasEvent')){
            me.columns.push(
                {
                    header: Uni.I18n.translate('device.registerData.eventTime', 'IMT', 'Event time'),
                    dataIndex: 'eventDate',
                    itemId: 'eventTime',
                    renderer: me.renderMeasurementTime,
                    flex: 1
                }
            );
        }
    },

    renderMeasurementTime: function (value, metaData, record) {
        if (Ext.isEmpty(value)) {
            return '-';
        }
        var date = new Date(value),
            showDeviceQualityIcon = false,
            tooltipContent = '',
            icon = '';

        if (!Ext.isEmpty(record.get('readingQualities'))) {
            Ext.Array.forEach(record.get('readingQualities'), function (readingQualityObject) {
                if (readingQualityObject.cimCode.startsWith('1.')) {
                    showDeviceQualityIcon |= true;
                    tooltipContent += readingQualityObject.indexName + '<br>';
                }
            });
            if (tooltipContent.length > 0) {
                tooltipContent += '<br>';
                tooltipContent += Uni.I18n.translate('general.deviceQuality.tooltip.moreMessage', 'IMT', 'View reading quality details for more information.');
            }
            if (showDeviceQualityIcon) {
                icon = '<span class="icon-price-tags" style="margin-left:10px; position:absolute;" data-qtitle="'
                    + Uni.I18n.translate('general.deviceQuality', 'IMT', 'Device quality') + '" data-qtip="'
                    + tooltipContent + '"></span>';
            }
        }
        return Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}', [Uni.DateTime.formatDateShort(date), Uni.DateTime.formatTimeShort(date)]) + icon;
    }
});