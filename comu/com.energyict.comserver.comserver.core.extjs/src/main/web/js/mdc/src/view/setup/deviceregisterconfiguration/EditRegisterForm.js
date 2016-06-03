Ext.define('Mdc.view.setup.deviceregisterconfiguration.EditRegisterForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.form.field.Obis',
        'Uni.property.view.DefaultButton'
    ],
    alias: 'widget.device-register-edit-form',
    returnLink: null,
    defaults: {
        labelWidth: 250,
        maxWidth: 600
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                margin: '0 0 10 0',
                width: 450,
                hidden: true
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'obis-code-container',
                required: true,
                layout: 'hbox',
                margin: '20 0 0 0',
                fieldLabel: Uni.I18n.translate('general.obisCode', 'MDC', 'OBIS code'),
                items: [
                    {
                        xtype: 'obis-field',
                        name: 'overruledObisCode',
                        itemId: 'mdc-editOverruledObisCodeField',
                        fieldLabel: '',
                        msgTarget: 'under',
                        allowBlank: false,
                        afterSubTpl: null,
                        width: 150
                    },
                    {
                        xtype: 'uni-default-button',
                        itemId: 'mdc-restore-obiscode-btn',
                        hidden: false,
                        disabled: true
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'overflowValue-container',
                required: true,
                layout: 'hbox',
                margin: '10 0 0 0',
                fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value'),
                items: [
                    {
                        xtype: 'numberfield',
                        name: 'overruledOverflow',
                        msgTarget: 'under',
                        itemId: 'mdc-editOverflowValueField',
                        width: 150,
                        maxValue: 2147483647,
                        hideTrigger: true,
                        maxLength: 22,
                        enforceMaxLength: true,
                        required: true,
                        allowBlank: false,
                        minValue: 1
                    },
                    {
                        xtype: 'uni-default-button',
                        itemId: 'mdc-restore-overflow-btn',
                        hidden: false,
                        disabled: true
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'fractionDigits-container',
                required: true,
                layout: 'hbox',
                margin: '10 0 0 0',
                fieldLabel: Uni.I18n.translate('registerConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                items: [
                    {
                        xtype: 'numberfield',
                        name: 'overruledNumberOfFractionDigits',
                        required: true,
                        msgTarget: 'under',
                        itemId: 'mdc-editNumberOfFractionDigitsField',
                        minValue: 0,
                        maxValue: 6,
                        maxLength: 1,
                        enforceMaxLength: true,
                        width: 150
                    },
                    {
                        xtype: 'uni-default-button',
                        itemId: 'mdc-restore-fractionDigits-btn',
                        hidden: false,
                        disabled: true
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'form-buttons',
                fieldLabel: '&nbsp;',
                layout: 'hbox',
                margin: '20 0 0 0',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-save-register',
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                        ui: 'action',
                        action: 'saveRegister'
                    },
                    {
                        xtype: 'button',
                        itemId: 'btn-cancel-editRegister',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        ui: 'link',
                        action: 'cancelEditRegister',
                        href: me.returnLink
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    setRegister: function(registerRecord) {
        var me = this,
            newTitle = registerRecord.get('readingType').fullAliasName;

        me.setTitle(newTitle);
        me.loadRecord(registerRecord);
    }

});