Ext.define('Mdc.view.setup.devicechannels.GraphView', {
    extend: 'Uni.view.highstock.GraphView',

    alias: 'widget.deviceLoadProfileChannelGraphView',
    itemId: 'deviceLoadProfileChannelGraphView',

    requires: [
        'Uni.view.highstock.GraphView'
    ],

    mentionDataLoggerSlave: false,

    mixins: {
        bindable: 'Ext.util.Bindable',
        graphWithGrid: 'Uni.util.GraphWithGrid'
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

    initComponent: function() {
        var me = this;

        me.callParent(arguments);
        me.bindStore(me.store || 'ext-empty-store', true);
        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            load: this.onLoad
        };
    },

    onBeforeLoad: function () {
        this.setLoading(true);
    },

    onLoad: function () {
        var data;
        if (this.store.getTotalCount() > 0) {
            data = this.formatData();
        }
        this.showGraphView(this, data);
        this.setLoading(false);
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    formatData: function () {
        var me = this,
            data = [],
            missedValues = [],
            collectedUnitOfMeasure = me.channel.get('readingType').names.unitOfMeasure,
            calculatedUnitOfMeasure = me.channel.get('calculatedReadingType') ? me.channel.get('calculatedReadingType').names.unitOfMeasure : collectedUnitOfMeasure,
            okColor = '#70BB51',
            estimatedColor = '#568343',
            suspectColor = 'rgba(235, 86, 66, 1)',
            informativeColor = '#dedc49',
            notValidatedColor = '#71adc7',
            dataSlaveColor = '#d2d2d2',
            tooltipOkColor = 'rgba(255, 255, 255, 0.85)',
            tooltipSuspectColor = 'rgba(235, 86, 66, 0.3)',
            tooltipEstimatedColor = 'rgba(86, 131, 67, 0.3)',
            tooltipInformativeColor = 'rgba(222, 220, 73, 0.3)',
            tooltipNotValidatedColor = 'rgba(0, 131, 200, 0.3)',
            tooltipDataSlaveColor = 'rgba(210, 210, 210, 0.3)';

        me.store.each(function (record) {
            var point = {},
                interval = record.get('interval'),
                mainValidationInfo = record.get('mainValidationInfo'),
                bulkValidationInfo = record.get('bulkValidationInfo'),
                properties = record.get('readingProperties'),
                slaveChannelInfo = record.get('slaveChannel');

            point.x = interval.start;
            point.id = point.x;
            point.y = parseFloat(record.get('value'));
            point.yValueFormatted = point.y ? Uni.Number.formatNumber(
                record.get('value').toString(),
                this.channel && !Ext.isEmpty(this.channel.get('overruledNbrOfFractionDigits')) ? this.channel.get('overruledNbrOfFractionDigits') : -1
            ) : '';
            point.intervalEnd = interval.end;
            point.collectedValue = record.get('collectedValue');
            point.collectedValueFormatted = point.collectedValue ? Uni.Number.formatNumber(
                point.collectedValue.toString(),
                this.channel && !Ext.isEmpty(this.channel.get('overruledNbrOfFractionDigits')) ? this.channel.get('overruledNbrOfFractionDigits') : -1
            ) : '';
            point.collectedUnitOfMeasure = collectedUnitOfMeasure;
            point.calculatedUnitOfMeasure = calculatedUnitOfMeasure;
            point.color = okColor;
            point.tooltipColor = tooltipOkColor;
            point.multiplier = record.get('multiplier');
            point.showDeviceQualityIcon = !Ext.isEmpty(record.get('readingQualities'));

            if (mainValidationInfo.valueModificationFlag == 'EDITED') {
                point.edited = true;
            }
            if (mainValidationInfo.estimatedByRule) {
                point.color = estimatedColor;
                point.tooltipColor = tooltipEstimatedColor;
            } else if (properties.delta.notValidated) {
                point.color = notValidatedColor;
                point.tooltipColor = tooltipNotValidatedColor
            } else if (properties.delta.suspect) {
                point.color = suspectColor;
                point.tooltipColor = tooltipSuspectColor
            } else if (properties.delta.informative) {
                point.color = informativeColor;
                point.tooltipColor = tooltipInformativeColor;
            }

            if (!Ext.isEmpty(slaveChannelInfo)) {
                point.dataLoggerSlave = Ext.String.htmlEncode(slaveChannelInfo.mrid);
                point.color = dataSlaveColor;
                point.tooltipColor = tooltipDataSlaveColor;
            }

            if (bulkValidationInfo.valueModificationFlag == 'EDITED') {
                point.bulkEdited = true;
            }

            Ext.merge(point, properties);
            data.unshift(point);

            !point.y && (point.y = null);
            if (!point.y) {
                if (properties.delta.suspect) {
                    missedValues.push({
                        id: record.get('interval').start,
                        from: record.get('interval').start,
                        to: record.get('interval').end,
                        color: 'rgba(235, 86, 66, 0.3)'
                    });
                    record.set('plotBand', true);
                }
            }
        }, me);
        return {data: data, missedValues: missedValues};
    },

    createTooltip: function (tooltip) {        
        var me = this,
            html = '<b>' + Highcharts.dateFormat('%A, %e %B %Y', tooltip.x),
            point = tooltip.points[0].point,
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
        html += '<br/>' + Uni.I18n.translate('general.interval', 'MDC', 'Interval') + ' ' + Highcharts.dateFormat('%H:%M', point.x);
        html += ' - ' + Highcharts.dateFormat('%H:%M', point.intervalEnd) + deviceQualityIcon + '<br>';
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