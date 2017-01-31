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

    mentionDataLoggerSlave: false,

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
            html = '<b>' + Uni.DateTime.formatDateLong(new Date(tooltip.x)),
            point = tooltip.point,
            deltaIcon = '',
            bulkIcon = '',
            deviceQualityIcon = '',
            bgColor,
            editedIcon = '<span class="icon-pencil4" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>',
            calculatedValue,
            collectedValue;

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
        html += '<br/>' + Uni.I18n.translate('general.interval', 'MDC', 'Interval') + ' ' + Uni.DateTime.formatTimeShort(new Date(point.x));
        html += ' - ' + Uni.DateTime.formatTimeShort(new Date(point.intervalEnd)) + deviceQualityIcon + '<br>';
        html += '<table style="margin-top: 10px"><tbody>';
        bgColor = point.tooltipColor;
        if (me.mentionDataLoggerSlave) {
            html += '<tr><td><b>' + Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave') + ':</b></td><td>'
                + (Ext.isEmpty(point.dataLoggerSlave) ? '-' : point.dataLoggerSlave) + '</td></tr>';
        }
        if (calculatedValue) {
            html += '<tr><td><b>' + Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated value') + ':</b></td><td>' + calculatedValue +
                deltaIcon + (point.edited ? editedIcon : '') + '</td></tr>';
            html += '<tr><td><b>' + Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value') + ':</b></td><td>' + collectedValue +
                bulkIcon + (point.bulkEdited ? editedIcon : '') + '</td></tr>';
        } else {
            html += '<tr><td><b>' + Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value') + ':</b></td><td>' + collectedValue +
                deltaIcon + (point.edited ? editedIcon : '') + '</td></tr>';
        }
        if (point.multiplier) {
            html += '<tr><td><b>' + Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier') + ':</b></td><td>' + point.multiplier + '</td></tr>';
        }
        html += '</tbody></table>';
        html = '<div style="background-color: ' + bgColor + '; padding: 8px">' + html + '</div>';
        return html;
    }
});