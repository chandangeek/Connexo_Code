/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.ValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceregisterreportpreview-validation',
    itemId: 'deviceregisterreportpreviewvalidation',
    requires:[
        'Cfg.privileges.Validation',
        'Cfg.view.field.ReadingQualities'
    ],

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
                fieldLabel: Uni.I18n.translate('device.registerData.dataValidated', 'MDC', 'Data validated'),
                name: 'dataValidated',
                htmlEncode: false,
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                        : Uni.I18n.translate('general.no', 'MDC', 'No') + '<span class="icon-flag6" style="margin-left:10px; display:inline-block;"></span>';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
                name: 'validationResult',
                renderer: function (value, field) {
                    if (!Ext.isEmpty(value)) {
                        field.show();
                        switch (value) {
                            case 'validationStatus.notValidated':
                                field.hide();
                                break;
                            case 'validationStatus.ok':
                                if (field.up('form').getRecord().get('isConfirmed')) {
                                    return Uni.I18n.translate('general.notSuspect', 'MDC', 'Not suspect') + '<span class="icon-checkmark" style="margin-left:5px; display:inline-block;"></span>';
                                } else {
                                    return Uni.I18n.translate('general.notSuspect', 'MDC', 'Not suspect');
                                }
                                break;
                            case 'validationStatus.suspect':
                                return Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect') + '<span class="icon-flag5" style="margin-left:10px; display:inline-block; color:red;"></span>';
                                break;
                            default:
                                field.hide();
                                break;
                        }
                    } else {
                        field.hide();
                    }
                }
            },
            {
                xtype: 'reading-qualities-field',
                labelWidth: 200,
                name: 'suspectReason',
                router: me.router,
                renderer: function (value, field) {
                    var rec = field.up('form').getRecord();
                    if (rec && rec.get('isConfirmed')) {
                        field.show();
                        return this.getConfirmed(rec.get('confirmedInApps'));
                    } else if (!Ext.isEmpty(value)) {
                        field.show();
                        return this.getValidationRules(value);
                    } else {
                        field.hide();
                    }
                }
            }
        ];

        this.callParent(arguments);
    }
});
