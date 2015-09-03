Ext.define('Mdc.view.setup.deviceloadprofiles.ValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceloadprofiles-validation',
    itemId: 'deviceloadprofilesvalidation',
    router: null,
    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation', 'MDC', 'Validation'),
    labelAlign: 'top',
    layout: 'vbox',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                fieldLabel: Uni.I18n.translate('device.registerData.validationStatus', 'MDC', 'Validation status'),
                name: 'validationInfo_validationActive'
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.allDataValidated', 'MDC', 'All data validated'),
                name: 'validationInfo_dataValidated',
                htmlEncode: false
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
                                if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                                    url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({ruleSetId: rule.key.ruleSetVersion.ruleSet.id, versionId: rule.key.ruleSetVersion.id, ruleId: rule.key.id});
                                    result += '<a href="' + url + '"> ' + rule.key.name + '</a>';
                                } else {
                                    result += rule.key.name;
                                }
                                result += ' - ' + Uni.I18n.translate('general.xsuspects', 'MDC', '{0} suspects',[rule.value]) + '<br>';
                            }
                        });
                        return result;
                    } else {
                        field.hide();
                    }
                }
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation.lastChecked', 'MDC', 'Last checked'),
                itemId: 'lastCheckedCont',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'displayfield',
                        name: 'lastChecked_formatted',
                        renderer: function (value, field) {
                            if (!Ext.isEmpty(value)) {
                                field.up('#lastCheckedCont').show();
                                this.nextSibling('button').setVisible(value ? true : false);
                                return Ext.String.htmlEncode(value);
                            } else {
                                field.up('#lastCheckedCont').hide();
                            }
                        }
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('deviceloadprofiles.tooltip.lastChecked', 'MDC', 'The moment when the last interval was checked in the validation process.'),
                        iconCls: 'icon-info-small',
                        ui: 'blank',
                        itemId: 'lastCheckedHelp',
                        shadow: false,
                        margin: '6 0 0 10',
                        width: 16
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
