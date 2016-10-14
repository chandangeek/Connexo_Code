Ext.define('Mdc.view.setup.devicechannels.ValidationOverview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceloadprofilechannelsoverview-validation',
    itemId: 'deviceloadprofilechannelsoverviewvalidation',
    router: null,
    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation', 'MDC', 'Validation'),

    labelAlign: 'top',
    layout: 'vbox',

    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    requires:[
        'Cfg.privileges.Validation'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                fieldLabel: Uni.I18n.translate('device.registerData.deviceValidationStatus', 'MDC', 'Device validation status'),
                name: 'validationInfo_validationActive'
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.channelValidationStatus', 'MDC', 'Channel validation status'),
                name: 'validationInfo_channelValidationStatus'
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.allDataValidated', 'MDC', 'All data validated'),
                name: 'validationInfo_dataValidated',
                htmlEncode: false
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation.suspects', 'MDC', 'Suspects (last month)'),
                name: 'validationInfo',
                minWidth: 450,
                renderer: function (value, field) {
                    var result = '',
                        url;
                    if (!Ext.isEmpty(value.suspectReason)) {
                        field.show();
                        Ext.Array.each(value.suspectReason, function (rule) {
                            if (rule.key.deleted) {
                                result += Ext.String.htmlEncode(rule.key.name) + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') + ' - ' + Uni.I18n.translate('general.xsuspects', 'MDC', '{0} suspects',[rule.value]) + '<br>';
                            } else {
                                if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                                    url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({ruleSetId: rule.key.ruleSetVersion.ruleSet.id, versionId: rule.key.ruleSetVersion.id, ruleId: rule.key.id});
                                    result += '<a href="' + url + '"> ' + Ext.String.htmlEncode(rule.key.name) + '</a>';
                                } else {
                                    result = Ext.String.htmlEncode(rule.key.name);
                                }
                            }   result += ' - ' +  Uni.I18n.translate('general.xsuspects', 'MDC', '{0} suspects',[rule.value]) + '<br>';
                        });
                        return result;
                    } else {
                        field.hide();
                    }
                }
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation.lastChecked', 'MDC', 'Last checked'),
                name: 'lastChecked_formatted',
                itemId: 'lastCheckedCont',
                renderer: function (value) {
                    var tooltip = Uni.I18n.translate('deviceloadprofiles.tooltip.lastChecked', 'MDC', 'The moment when the last interval was checked in the validation process.');
                    return value
                        ? '<span style="display:inline-block; float:left; margin-right:7px;" >' + Ext.String.htmlEncode(value) + '</span>' +
                    '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="' + Ext.String.htmlEncode(tooltip) + '"></span>'
                        : '-';
                }
            }
        ];

        me.callParent(arguments);
    }
});

