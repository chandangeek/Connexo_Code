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
                fieldLabel: Uni.I18n.translate('device.registerData.validationStatus', 'MDC', 'Validation status'),
                name: 'validationInfo_validationActive'
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.allDataValidated', 'MDC', 'All data validated'),
                name: 'validationInfo_dataValidated'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation.suspects', 'MDC', 'Suspects (last month)'),
                name: 'validationInfo',
                renderer: function (value, field) {
                    var result = '',
                        url;
                    if (!Ext.isEmpty(value.suspectReason)) {
                        field.show();
                        Ext.Array.each(value.suspectReason, function (rule) {
                            if (rule.key.deleted) {
                                result += rule.key.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') + ' - ' + rule.value + ' ' + Uni.I18n.translate('general.suspects', 'MDC', 'suspects') + '<br>';
                            } else {
                                if (Cfg.privileges.Validation.canViewOrAdminstrate()) {
                                    url = me.router.getRoute('administration/rulesets/overview/rules').buildUrl({ruleSetId: rule.key.ruleSet.id, ruleId: rule.key.id});
                                    result += '<a href="' + url + '"> ' + rule.key.name + '</a>';
                                } else {
                                    result = rule.key.name;
                                }
                            }   result += ' - ' + rule.value + ' ' + Uni.I18n.translate('general.suspects', 'MDC', 'suspects') + '<br>';
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
                        ? value + '<span style="margin: 0 0 0 10px; width: 16px; height: 16px" class="icon-info-small" data-qtip="' + tooltip + '"></span>'
                        : '';
                }
            }
        ];

        me.callParent(arguments);
    }
});

