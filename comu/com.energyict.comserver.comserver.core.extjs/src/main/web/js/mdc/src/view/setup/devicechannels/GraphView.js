/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.GraphView', {
    extend: 'Uni.view.highstock.GraphView',

    alias: 'widget.deviceLoadProfileChannelGraphView',
    itemId: 'deviceLoadProfileChannelGraphView',

    requires: [
        'Uni.view.highstock.GraphView'
    ],

    linkPurpose: Mdc.util.LinkPurpose.properties[Mdc.util.LinkPurpose.NOT_APPLICABLE],

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    items: [
        {
            xtype: 'container',
            itemId: 'graphContainer',
            style: {
                width: '100%'
            }
        }
    ],

    createTooltip: function (tooltip) {
        var me = this,
            html = '<b style=" color: #74af74; font-size: 14px; ">',
            point = tooltip.point,
            isInterval = tooltip.point.channelPeriodType === 'interval',
            isMonth = tooltip.point.channelPeriodType === 'monthly',
            isYear = tooltip.point.channelPeriodType === 'yearly',
            deltaIcon = '',
            bulkIcon = '',
            deviceQualityIcon = '',
            editedIcon = '<span class="icon-pencil4" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>',
            calculatedValue,
            collectedValue;

        if (isInterval) {
            html += Uni.DateTime.formatDateLong(new Date(tooltip.x)) + '<br/>';
        }
        if (point.delta && point.delta.suspect) {
            deltaIcon = '<span class="icon-flag5" style="margin-left:4px; display:inline-block; vertical-align:top; color:red"></span>';
        } else if (point.delta && point.delta.notValidated) {
            deltaIcon = '<span class="icon-flag6" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>';
        }

        if (point.bulk && point.bulk.suspect) {
            bulkIcon = '<span class="icon-flag5" style="margin-left:4px; display:inline-block; vertical-align:top; color:red"></span>';
        } else if (point.bulk && point.bulk.notValidated) {
            bulkIcon = '<span class="icon-flag6" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>';
        }

        if (point.showDeviceQualityIcon) {
            deviceQualityIcon = '<span class="icon-price-tags" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>';
        }

        if (point.collectedValue) {
            calculatedValue = point.y
                ? point.yValueFormatted + ' ' + point.calculatedUnitOfMeasure
                : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
            collectedValue = point.collectedValue
                ? point.collectedValueFormatted + ' ' + point.collectedUnitOfMeasure
                : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
        } else {
            // If there's a value (point.y) but no point.collectedValue, then we should call the value "Collected value" (and there's no "Calculated value")
            collectedValue = point.y
                ? point.yValueFormatted + ' ' + point.collectedUnitOfMeasure
                : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
            calculatedValue = null;
        }
        if (isInterval) {
            html += Uni.I18n.translate('general.interval', 'MDC', 'Interval') + ' ' + Uni.DateTime.formatTimeShort(new Date(point.x));
            html += ' - ' + Uni.DateTime.formatTimeShort(new Date(point.intervalEnd));
        } else if (isMonth) {
            html += Ext.Date.format(new Date(point.x), 'M Y');
        } else if (isYear) {
            html += Ext.Date.format(new Date(point.x), 'Y');
        } else {
            html += Uni.DateTime.formatDateTime(new Date(point.x), Uni.DateTime.LONG, Uni.DateTime.SHORT);
            html += ' - ' + Uni.DateTime.formatDateTime(new Date(point.intervalEnd), Uni.DateTime.LONG, Uni.DateTime.SHORT);
        }
        html += (deviceQualityIcon + '</b><br>');
        html += '<table style="margin-top: 10px; color: #686868; font-size: 14px;"><tbody>';
        if (me.linkPurpose.value !== Mdc.util.LinkPurpose.NOT_APPLICABLE) {
            html += '<tr><td><b>' + me.linkPurpose.channelGridSlaveColumn + '</b></td><td>'
                + (Ext.isEmpty(point.dataLoggerSlave) ? '-' : point.dataLoggerSlave) + '</td></tr>';
        }
        if (calculatedValue) {
            html += '<tr><td><b>' + Uni.I18n.translate('general.calculated.Value', 'MDC', 'Calculated value') + '</b></td><td style="font-weight: lighter">' + calculatedValue +
                deltaIcon + (point.edited ? editedIcon : '') + '</td></tr>';
            html += '<tr><td><b>' + Uni.I18n.translate('general.collected.Value', 'MDC', 'Collected value') + '</b></td><td style="font-weight: lighter">' + collectedValue +
                bulkIcon + (point.bulkEdited ? editedIcon : '') + '</td></tr>';
        } else {
            html += '<tr><td><b>' + Uni.I18n.translate('general.collected.Value', 'MDC', 'Collected value') + '</b></b></td><td style="font-weight: lighter">' + collectedValue +
                deltaIcon + (point.edited ? editedIcon : '') + '</td></tr>';
        }
        if (point.multiplier) {
            html += '<tr><td><b>' + Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier') + '</b></td><td>' + point.multiplier + '</td></tr>';
        }
        html += '</tbody></table>';
        return html;
    }
});