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
        bindable: 'Ext.util.Bindable'        
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

    createTooltip: function (tooltip) {
        var me = this,
            html = '<b>' + Uni.DateTime.formatDateLong(new Date(tooltip.x)),
            editedIcon = '<span class="icon-pencil4" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>',
            point = tooltip.point,
            qualityIcon = '',
            icon,
            bgColor,
            value;

        if (point && point.suspect) {
            icon = '<span class="icon-flag5" style="margin-left:4px; display:inline-block; vertical-align:top; color:red"></span>';
        } else if (point && point.notValidated) {
            icon = '<span class="icon-flag6" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>';
        }

        if (point.showQualityIcon) {
            qualityIcon = '<span class="icon-price-tags" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>';
        }

        if (point.value) {
            value = point.value ? point.value + ' ' + point.unitOfMeasure : Uni.I18n.translate('general.missing', 'IMT', 'Missing');
        } else {
            value = point.y ? point.y + ' ' + point.unitOfMeasure : Uni.I18n.translate('general.missing', 'IMT', 'Missing');
        }

        html += '<br/>' + Uni.I18n.translate('devicechannels.interval', 'IMT', 'Interval') + ' ' + Uni.DateTime.formatTimeShort(new Date(point.x));
        html += ' - ' + Uni.DateTime.formatTimeShort(new Date(point.intervalEnd)) + qualityIcon + '<br>';
        html += '<table style="margin-top: 10px"><tbody>';
        bgColor = point.tooltipColor;
        html += '<tr><td colspan="2"><b>' + Uni.I18n.translate('general.value', 'IMT', 'Value') + ':</b>&nbsp;' + value + (icon ? icon : '') + (point.edited ? editedIcon : '') + '</td></tr>';

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