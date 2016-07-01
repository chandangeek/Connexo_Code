/**
 * A display-only text field which is not validated and not submitted. This is useful for when you want to display a
 * value from a form's {@link Ext.form.Basic#load loaded data} but do not want to allow the user to edit or submit that
 * value. The value can be optionally {@link #htmlEncode HTML encoded} if it contains HTML markup that you do not want
 * to be rendered.
 *
 * If you have more complex content, or need to include components within the displayed content, also consider using a
 * {@link Ext.form.FieldContainer} instead.
 *
 * Example:
 *
 *     @example
 *     Ext.create('Ext.form.Panel', {
 *         renderTo: Ext.getBody(),
 *         width: 175,
 *         height: 120,
 *         bodyPadding: 10,
 *         title: 'Final Score',
 *         items: [{
 *             xtype: 'displayfield',
 *             fieldLabel: 'Home',
 *             name: 'home_score',
 *             value: '10'
 *         }, {
 *             xtype: 'displayfield',
 *             fieldLabel: 'Visitor',
 *             name: 'visitor_score',
 *             value: '11'
 *         }],
 *         buttons: [{
 *             text: 'Update'
 *         }]
 *     });
 */
Ext.define('Mdc.view.field.ReadingQualities', {
    extend:'Ext.form.field.Display',
    alias: 'widget.reading-qualities-field',
    fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
    htmlEncode: false,

    renderer : function(value, field) {
        if (value.isConfirmed) {
            return this.getConfirmed(value.confirmedInApps);
        } else if (!Ext.isEmpty(value.validationRules)) {
            return this.getValidationRules(value.validationRules);
        } else if (value.estimatedByRule) {
            return this.getEstimatedByRule(value.estimatedByRule);
        } else {
            field.hide();
        }
    },

    getConfirmed: function(apps) {
        return apps && apps.length
            ? Uni.I18n.translate('general.confirmedIn', 'MDC', 'Confirmed in {0}', [application.name]) //TODO: fold applications
            : Uni.I18n.translate('general.confirmed', 'MDC', 'Confirmed')
    },

    getEstimatedByRule: function(estimatedRule) {
        var me = this,
            application = estimatedRule.application,
            url = me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl({
                ruleSetId: estimatedRule.ruleSetId,
                ruleId: estimatedRule.id
            });

        var estimatedRuleName = estimatedRule.deleted
            ? estimatedRule.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)')
            : me.makeLink(application, url, estimatedRule.name);

        return application
            ? Uni.I18n.translate('deviceChannelData.estimatedAccordingToApp', 'MDC', 'Estimated in {0} according to {1}',[
                application.name,
                estimatedRuleName
            ], false)
            : Uni.I18n.translate('deviceChannelData.estimatedAccordingTo', 'MDC', 'Estimated according to {0}',[
                estimatedRuleName
            ], false);
    },

    makeLink: function(application, url, value) {
        var appUrl = application ? Uni.store.Apps.getAppUrl(application.name) : null;

        if (Ext.isObject(appUrl)) {
            appUrl = null;
        }

        return appUrl
          ? ('<a href="' + appUrl + url.slice(1) +  '">' + value + '</a>')
          : value;
    },

    getValidationRules: function (rules) {
        var str = '',
            prop,
            me = this,
            failEqualDataValue,
            intervalFlagsValue = '';

        Ext.Array.each(rules, function (rule) {
            var application = rule.application
                ? '<span class="application">'+ Uni.I18n.translate('general.application', 'MDC', '({0})', [rule.application.name]) + '</span>'
                : '';
            if (!Ext.isEmpty(rule.properties)) {
                switch (rule.implementation) {
                    case 'com.elster.jupiter.validators.impl.ThresholdValidator':
                        prop = ' - ' + rule.properties[0].key.charAt(0).toUpperCase() + rule.properties[0].key.substring(1) + ': ' + rule.properties[0].propertyValueInfo.value + ', ' +
                            rule.properties[1].key.charAt(0).toUpperCase() + rule.properties[1].key.substring(1) + ': ' + rule.properties[1].propertyValueInfo.value;
                        break;
                    case 'com.elster.jupiter.validators.impl.RegisterIncreaseValidator':
                        if (rule.properties[0].propertyValueInfo.value) {
                            failEqualDataValue = Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                        } else {
                            failEqualDataValue = Uni.I18n.translate('general.no', 'MDC', 'No');
                        }
                        prop = ' - ' + Uni.I18n.translate('device.registerData.failEqualData', 'MDC', 'Fail equal data') + ': ' + failEqualDataValue;
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
                        prop = ' - ' + Uni.I18n.translate('deviceloadprofiles.intervalFlags', 'MDC', 'Interval flags') + ': ' + intervalFlagsValue;
                        break;
                    default:
                        prop = '';
                        break;
                }
            } else {
                prop = '';
            }
            if (rule.deleted) {
                str += '<span style="word-wrap: break-word; display: inline-block; width: 800px">' + rule.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') + prop + '</span>' + '&nbsp;' + application  + '<br>';
            } else {
                str = '<span style="word-wrap: break-word; display: inline-block; width: 800px">';

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
    }
});