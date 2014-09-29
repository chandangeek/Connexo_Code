Ext.define('Mdc.view.setup.deviceregisterconfiguration.ValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceregisterdetailspreview-validation',
    itemId: 'deviceregisterdetailspreviewvalidation',
    router: null,
    fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validation', 'MDC', 'Validation'),
    labelAlign: 'top',
    layout: 'vbox',
    inputLabelWidth: 200,

    initComponent: function () {
        var me = this;
        me.defaults = {
            xtype: 'displayfield',
            labelWidth: this.inputLabelWidth
        };
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
                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validation.suspects', 'MDC', 'Suspects (last year)'),
                name: 'detailedValidationInfo',
                renderer: function (value, field) {
                    var result = '',
                        url;
                    if (value.suspectReason) {
                        field.show();
                        Ext.Array.each(value.suspectReason, function (rule) {
                            url = me.router.getRoute('administration/rulesets/overview/rules').buildUrl({ruleSetId: rule.key.ruleSet.id, ruleId: rule.key.id});
                            result += '<a href="' + url + '"> ' + rule.key.name + '</a>' + ' - ' + rule.value + ' ' + Uni.I18n.translate('general.suspects', 'MDC', 'suspects') + '<br>';
                        });
                        return result;
                    }
                    if(Ext.isEmty(value.suspectReason)) {
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
                            if (!_.isEmpty(value)) {
                                field.up('#lastCheckedCont').show();
                                this.nextSibling('button').setVisible(value ? true : false);
                                return value;
                            } else {
                                field.up('#lastCheckedCont').hide();
                            }
                        }
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('deviceloadprofiles.tooltip.lastChecked', 'MDC', 'The timestamp of the last data that was validated'),
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
