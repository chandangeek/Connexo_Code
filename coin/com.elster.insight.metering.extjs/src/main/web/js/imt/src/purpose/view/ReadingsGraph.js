Ext.define('Imt.purpose.view.ReadingsGraph', {
    extend: 'Uni.view.highstock.GraphView',
    alias: 'widget.readings-graph',
    itemId: 'readings-graph',

    store: 'Imt.purpose.store.Readings',

    requires: [
        'Uni.view.highstock.GraphView',
        'Cfg.privileges.Validation'
    ],

    mixins: {
        bindable: 'Ext.util.Bindable',
        graphWithGrid: 'Uni.util.GraphWithGrid'
    },

    router: null,

    items: [
        {
            xtype: 'container',
            itemId: 'graphContainer',
            style: {
                width: '100%'
            }
        }
    ],

    initComponent: function () {
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

        this.showGraphView(this.up('output-readings'), data);        
        this.setLoading(false);        
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    formatData: function () {
        var me = this,
            data = [],
            missedValues = [],
            output = me.output,
            unitOfMeasure = output.get('readingType').names.unitOfMeasure,
            okColor = "#70BB51",
            suspectColor = 'rgba(235, 86, 66, 1)',
            informativeColor = "#dedc49",
            notValidatedColor = "#71adc7",
            tooltipOkColor = 'rgba(255, 255, 255, 0.85)',
            tooltipSuspectColor = 'rgba(235, 86, 66, 0.3)',
            tooltipInformativeColor = 'rgba(222, 220, 73, 0.3)',
            tooltipNotValidatedColor = 'rgba(0, 131, 200, 0.3)';

        me.store.each(function (record) {
            var point = {},
                interval = record.get('interval'),
                properties = record.get('readingProperties');

            point.x = interval.start;
            point.id = point.x;
            point.y = parseFloat(record.get('value')) || null;
            point.intervalEnd = interval.end;
            point.value = record.get('value');
            point.unitOfMeasure = unitOfMeasure;
            point.color = okColor;
            point.tooltipColor = tooltipOkColor;

            point.validationRules = record.get('validationRules');

            if (properties.notValidated) {
                point.color = notValidatedColor;
                point.tooltipColor = tooltipNotValidatedColor
            } else if (properties.suspect) {
                point.color = suspectColor;
                point.tooltipColor = tooltipSuspectColor
            } else if (properties.informative) {
                point.color = informativeColor;
                point.tooltipColor = tooltipInformativeColor;
            }

            Ext.merge(point, properties);
            data.unshift(point);

            !point.y && (point.y = null);
            if (!point.y) {
                if (properties.suspect) {
                    missedValues.push({
                        id: record.get('interval').start,
                        from: record.get('interval').start,
                        to: record.get('interval').end,
                        color: 'rgba(235, 86, 66, 0.3)'
                    });
                    record.set('plotBand', true);
                }
            }
        });

        return {data: data, missedValues: missedValues};
    },

    createTooltip: function (tooltip) {
        var me = this,
            html = '<b>' + Highcharts.dateFormat('%A, %e %B %Y', tooltip.x),
            point = tooltip.points[0].point,
            icon,
            bgColor,
            value;

        if (point && point.suspect) {
            icon = '<span class="icon-flag5" style="margin-left:4px; display:inline-block; vertical-align:top; color:red"></span>';
        } else if (point && point.notValidated) {
            icon = '<span class="icon-flag6" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>';
        }

        if (point.value) {
            value = point.value ? point.value + ' ' + point.unitOfMeasure : Uni.I18n.translate('general.missing', 'IMT', 'Missing');
        } else {
            value = point.y ? point.y + ' ' + point.unitOfMeasure : Uni.I18n.translate('general.missing', 'IMT', 'Missing');
        }

        html += '<br/>' + Uni.I18n.translate('devicechannels.interval', 'IMT', 'Interval') + ' ' + Highcharts.dateFormat('%H:%M', point.x);
        html += ' - ' + Highcharts.dateFormat('%H:%M', point.intervalEnd) + '<br>';
        html += '<table style="margin-top: 10px"><tbody>';
        bgColor = point.tooltipColor;
        html += '<tr><td colspan="2"><b>' + Uni.I18n.translate('general.value', 'IMT', 'Value') + ':</b>&nbsp;' + value + (icon ? icon : '') + '</td></tr>';

        if (!Ext.isEmpty(point.validationRules)) {
            html += '<tr><td style="padding-right: 5px" valign="top">' + Uni.I18n.translate('channels.readingqualities.title', 'IMT', 'Reading qualities') + '</td><td style="font-weight: 500">' + me.getValidationRules(point.validationRules) + '</td></tr>';
        }

        html += '</tbody></table>';

        html = '<div style="background-color: ' + bgColor + '; padding: 8px">' + html + '</div>';
        return html;
    },

    getValidationRules: function (rules) {
        var str = '',
            prop,
            me = this,
            failEqualDataValue,
            intervalFlagsValue = '';

        Ext.Array.each(rules, function (rule) {
            var application = rule.application
                ? '<span class="application">(' + rule.application.name + ')</span>'
                : '';
            if (!Ext.isEmpty(rule.properties)) {
                switch (rule.implementation) {
                    case 'com.elster.jupiter.validators.impl.ThresholdValidator':
                        prop = ' - ' + Ext.String.capitalize(rule.properties[0].name) + ': ' + rule.properties[0].propertyValueInfo.value + ', ' +
                            Ext.String.capitalize(rule.properties[1].name) + ': ' + rule.properties[1].propertyValueInfo.value;
                        break;
                    case 'com.elster.jupiter.validators.impl.RegisterIncreaseValidator':
                        if (rule.properties[0].propertyValueInfo.value) {
                            failEqualDataValue = Uni.I18n.translate('general.yes', 'IMT', 'Yes');
                        } else {
                            failEqualDataValue = Uni.I18n.translate('general.no', 'IMT', 'No');
                        }
                        prop = ' - ' + Uni.I18n.translate('device.registerData.failEqualData', 'IMT', 'Fail equal data') + ': ' + failEqualDataValue;
                        break;
                    case 'com.elster.jupiter.validators.impl.IntervalStateValidator':
                        Ext.Array.each(rule.properties[0].propertyValueInfo.value, function (idValue) {
                            Ext.Array.each(rule.properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues, function (item) {
                                if (idValue === item.id) {
                                    intervalFlagsValue += item.name + ', ';
                                }
                            });
                        });
                        intervalFlagsValue = intervalFlagsValue.slice(0, -2);
                        prop = ' - ' + Uni.I18n.translate('deviceloadprofiles.intervalFlags', 'IMT', 'Interval flags') + ': ' + intervalFlagsValue;
                        break;
                    default:
                        prop = '';
                        break;
                }
            } else {
                prop = '';
            }
            if (rule.deleted) {
                str += '<span style="word-wrap: break-word; display: inline-block">' + rule.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'IMT', '(removed rule)') + prop + '</span>' + '&nbsp;' + application + '<br>';
            } else {
                str += '<span style="word-wrap: break-word; display: inline-block">';

                if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                    var url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({
                        ruleSetId: rule.ruleSetVersion.ruleSet.id,
                        versionId: rule.ruleSetVersion.id, ruleId: rule.id
                    });

                    str += me.makeLink(rule.application, url, rule.name);
                } else {
                    str += rule.name;
                }
                str += prop + '&nbsp;' + application + '</span><br>';
            }
        });

        return str;
    },

    makeLink: function (application, url, value) {
        var appUrl = application ? Uni.store.Apps.getAppUrl(application.name) : null;

        if (Ext.isObject(appUrl)) {
            appUrl = null;
        }

        return appUrl ? ('<a href="' + appUrl + url.slice(1) + '">' + value + '</a>') : value;
    }
});