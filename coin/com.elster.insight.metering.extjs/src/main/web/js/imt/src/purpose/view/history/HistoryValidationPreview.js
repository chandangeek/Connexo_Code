/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * This class was copied from MDC - Mdc.view.setup.deviceregisterdata.HistoryValidationPreview
 */

Ext.define('Imt.purpose.view.history.HistoryValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.history-validation-preview',
    requires: [
        'Cfg.privileges.Validation',
        'Cfg.view.field.ReadingQualities'
    ],

    fieldLabel: Uni.I18n.translate('general.validation', 'IMT', 'Validation'),
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
                xtype: 'uni-form-info-message',
                itemId: 'mdc-noHistoryReadings-msg',
                text: Uni.I18n.translate('general.reading.noDataQualities', 'IMT', 'There are no reading qualities for this reading.'),
                margin: '7 10 32 0',
                padding: '10'
            },
            {
                fieldLabel: Uni.I18n.translate('reading.validationResult', 'IMT', 'Validation result'),
                name: 'validationResult',
                renderer: function (value, field) {
                    me.showNoReading(field.up('form').getRecord());
                    if (!Ext.isEmpty(value)) {
                        field.show();
                        switch (value) {
                            case 'validationStatus.notValidated':
                                field.hide();
                                break;
                            case 'validationStatus.ok':
                                if (field.up('form').getRecord().get('isConfirmed')) {
                                    return Uni.I18n.translate('reading.validationResult.notsuspect', 'IMT', 'Not suspect') + '<span class="icon-checkmark" style="margin-left:5px; display:inline-block;"></span>';
                                } else {
                                    return Uni.I18n.translate('reading.validationResult.notsuspect', 'IMT', 'Not suspect');
                                }
                                break;
                            case 'validationStatus.suspect':
                                return Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') + '<span class="icon-flag5" style="margin-left:10px; display:inline-block; color:red;"></span>';
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
    },

    showNoReading: function (record) {
        this.down('#mdc-noHistoryReadings-msg').setVisible(Ext.isEmpty(record.get('validationResult')) && Ext.isEmpty(record.get('suspectReason')));
    }
});
