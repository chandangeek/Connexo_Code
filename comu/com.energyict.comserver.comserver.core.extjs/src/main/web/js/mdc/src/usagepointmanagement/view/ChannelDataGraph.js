Ext.define('Mdc.usagepointmanagement.view.ChannelDataGraph', {
    extend: 'Uni.view.highstock.GraphView',
    alias: 'widget.channel-data-graph',
    channel: null,
    zoomLevels: null,

    listeners: {
        resize: {
            fn: function (graphView, width, height) {
                if (this.chart) {
                    this.chart.setSize(width, height, false);
                }
            }
        }
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
        var html = '<b>' + Highcharts.dateFormat('%A, %e %B %Y', tooltip.x),
            point = tooltip.points[0].point,
            bgColor,
            editedIconSpan = '<span class="uni-icon-edit"' + 'style="height: 13px; ' + 'width: 13px; ' +
                'display: inline-block; ' + 'vertical-align: top; ' + 'margin-left: 4px"></span>',
            value;

        value = !Ext.isEmpty(point.y) ? point.y + ' ' + point.unit : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
        html += '<br/>' + Uni.I18n.translate('devicechannels.interval', 'MDC', 'Interval') + ' ' + Highcharts.dateFormat('%H:%M', point.x);
        html += ' - ' + Highcharts.dateFormat('%H:%M', point.intervalEnd) + '<br>';
        html += '<table style="margin-top: 10px"><tbody>';
        bgColor = point.tooltipColor;
        html += '<tr><td><b>' + Uni.I18n.translate('general.value', 'MDC', 'Value') + ':</b></td><td>' + value + (point.edited ? editedIconSpan : '') + ' ' + point.icon + '</td></tr>';
        if (point.multiplier) {
            html += '<tr><td><b>' + Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier') + ':</b></td><td>' + point.multiplier + '</td></tr>';
        }
        html += '</tbody></table>';
        html = '<div style="background-color: ' + bgColor + '; padding: 8px">' + html + '</div>';
        return html;
    }
});