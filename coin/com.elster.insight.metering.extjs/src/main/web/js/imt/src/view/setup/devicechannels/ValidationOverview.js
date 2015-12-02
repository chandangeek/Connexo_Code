Ext.define('Imt.view.setup.devicechannels.ValidationOverview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceloadprofilechannelsoverview-validation',
    itemId: 'deviceloadprofilechannelsoverviewvalidation',
    router: null,
    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation', 'IMT', 'Validation'),

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
                fieldLabel: Uni.I18n.translate('device.registerData.validationStatus', 'IMT', 'Validation status'),
                name: 'validationInfo_validationActive'
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.allDataValidated', 'IMT', 'All data validated'),
                name: 'validationInfo_dataValidated',
                htmlEncode: false
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation.suspects', 'IMT', 'Suspects (last month)'),
                name: 'validationInfo',
                minWidth: 450,
                renderer: function (value, field) {
                    var result = '',
                        url;
                    if (!Ext.isEmpty(value.suspectReason)) {
                        field.show();
                        Ext.Array.each(value.suspectReason, function (rule) {
                            if (rule.key.deleted) {
                                result += Ext.String.htmlEncode(rule.key.name) + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'IMT', '(removed rule)') + ' - ' + Uni.I18n.translate('general.xsuspects', 'IMT', '{0} suspects',[rule.value]) + '<br>';
                            } else {
                                if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                                    url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({ruleSetId: rule.key.ruleSetVersion.ruleSet.id, versionId: rule.key.ruleSetVersion.id, ruleId: rule.key.id});
                                    result += '<a href="' + url + '"> ' + Ext.String.htmlEncode(rule.key.name) + '</a>';
                                } else {
                                    result = Ext.String.htmlEncode(rule.key.name);
                                }
                            }   result += ' - ' +  Uni.I18n.translate('general.xsuspects', 'IMT', '{0} suspects',[rule.value]) + '<br>';
                        });
                        return result;
                    } else {
                        field.hide();
                    }
                }
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation.lastChecked', 'IMT', 'Last checked'),
                name: 'lastChecked_formatted',
                itemId: 'lastCheckedCont',
                renderer: function (value) {
                    var tooltip = Uni.I18n.translate('deviceloadprofiles.tooltip.lastChecked', 'IMT', 'The moment when the last interval was checked in the validation process.');
                    return value
                        ? Ext.String.htmlEncode(value) + '<span style="margin: 0 0 0 10px; width: 16px; height: 16px" class="uni-icon-info-small" data-qtip="' + tooltip + '"></span>'
                        : '';
                }
            }
        ];

        me.callParent(arguments);
    }
});

