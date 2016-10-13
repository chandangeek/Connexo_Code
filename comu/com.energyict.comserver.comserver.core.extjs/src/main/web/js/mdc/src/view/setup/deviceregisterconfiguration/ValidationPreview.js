Ext.define('Mdc.view.setup.deviceregisterconfiguration.ValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceregisterdetailspreview-validation',
    itemId: 'deviceregisterdetailspreviewvalidation',
    router: null,
    fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validation', 'MDC', 'Validation'),
    labelAlign: 'top',
    margin: '0 0 100 0',
    layout: 'vbox',
    inputLabelWidth: 200,

    initComponent: function () {
        var me = this;
        me.fieldDefaults = {
            labelWidth: me.inputLabelWidth
        };
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('device.registerData.deviceValidationStatus', 'MDC', 'Device validation status'),
                name: 'validationInfo_validationActive'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('device.registerData.registerValidationStatus', 'MDC', 'Register validation status'),
                name: 'validationInfo_channelValidationStatus'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('device.registerData.allDataValidated', 'MDC', 'All data validated'),
                name: 'validationInfo_dataValidated',
                htmlEncode: false
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validation.suspects', 'MDC', 'Suspects (last year)'),
                name: 'detailedValidationInfo',
                renderer: function (value, field) {
                    var result = '',
                        url;
                    if (!Ext.isEmpty(value.suspectReason)) {
                        field.show();
                        Ext.Array.each(value.suspectReason, function (rule) {
                            if (rule.key.deleted) {
                                result += rule.key.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') + ' - ' + rule.value + ' ' + Uni.I18n.translate('general.suspects', 'MDC', 'suspects') + '<br>';
                            } else {
                                if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                                    url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({ruleSetId: rule.key.ruleSetVersion.ruleSet.id, versionId: rule.key.ruleSetVersion.id, ruleId: rule.key.id});
                                    result = '<a href="' + url + '"> ' + rule.key.name + '</a>';
                                } else {
                                    result = rule.key.name;
                                }
                                result += ' - ' + Uni.I18n.translate('general.xsuspects', 'MDC', '{0} suspects',[rule.value]) + '<br>';
                            }
                        });
                        return Ext.isEmpty(result) ? '-' : result;
                    } else {
                        field.hide();
                    }
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation.lastChecked', 'MDC', 'Last checked'),
                itemId: 'lastCheckedCont',
                name: 'lastChecked_formatted',
                renderer: function (value, field) {
                    if (Ext.isEmpty(value)) {
                        field.hide();
                    } else {
                        field.show();
                        var tooltip = Uni.I18n.translate('deviceloadprofiles.tooltip.lastChecked', 'MDC', 'The moment when the last interval was checked in the validation process.');
                        return '<span style="display:inline-block; float:left; margin-right:7px;" >' + Ext.String.htmlEncode(value) + '</span>' +
                            '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="' + Ext.String.htmlEncode(tooltip) + '"></span>'
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});
