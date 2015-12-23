Ext.define('Imt.registerdata.view.RegisterValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.registerValidationPreview',
    itemId: 'registerValidationPreview',
    router: null,
    fieldLabel: Uni.I18n.translate('registerdata.validation', 'IMT', 'Validation'),
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
                fieldLabel: Uni.I18n.translate('registerdata.validationStatus', 'IMT', 'Validation status'),
                name: 'validationInfo_validationActive'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('registerdata.allDataValidated', 'IMT', 'All data validated'),
                name: 'validationInfo_dataValidated',
                htmlEncode: false
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('registerdata.validation.suspects', 'IMT', 'Suspects (last year)'),
                name: 'validationInfo',
                renderer: function (value, field) {
                	//return 'yes suspect';
                    var result = '',
                        url;
                    if (!Ext.isEmpty(value.suspectReason)) {
                        field.show();
                        Ext.Array.each(value.suspectReason, function (rule) {
                            if (rule.key.deleted) {
                                result += rule.key.name + ' ' + Uni.I18n.translate('device.registerdata.removedRule', 'IMT', '(removed rule)') + ' - ' + rule.value + ' ' + Uni.I18n.translate('general.suspects', 'IMT', 'suspects') + '<br>';
                            } else {
                                if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                                    url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({ruleSetId: rule.key.ruleSetVersion.ruleSet.id, versionId: rule.key.ruleSetVersion.id, ruleId: rule.key.id});
                                    result = '<a href="' + url + '"> ' + rule.key.name + '</a>';
                                } else {
                                    result = rule.key.name;
                                }
                                result += ' - ' + Uni.I18n.translate('general.xsuspects', 'IMT', '{0} suspects',[rule.value]) + '<br>';
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
                fieldLabel: Uni.I18n.translate('registerdata.validation.lastChecked', 'IMT', 'Last checked'),
                itemId: 'lastCheckedCont',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'displayfield',
                        name: 'validationInfo_lastChecked', //_formatted',
                        renderer: function (value, field) {
//                            if (!_.isEmpty(value)) {
//                                field.up('#lastChecked').show();
//                                this.nextSibling('button').setVisible(value ? true : false);
                                return Ext.String.htmlEncode(value);
//                            } else {
//                                field.up('#lastChecked').hide();
//                            }
                        }
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('registerdata.tooltip.lastChecked', 'IMT', 'The moment when the last reading was checked in the validation process.'),
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
