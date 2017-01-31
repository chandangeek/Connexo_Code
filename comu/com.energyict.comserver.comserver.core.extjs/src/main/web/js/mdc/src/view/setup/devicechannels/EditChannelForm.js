/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.EditChannelForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.form.field.Obis',
        'Uni.property.view.DefaultButton'
    ],
    alias: 'widget.device-channel-edit-form',
    returnLink: null,
    layout: {
        type: 'vbox',
        align: 'stretch' // in order to completely see the error messages
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                margin: '0 0 10 0',
                maxWidth: 450,
                hidden: true
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'obis-code-container',
                required: true,
                width: 450,
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
                width: 450,
                layout: 'hbox',
                margin: '10 0 0 0',
                fieldLabel: Uni.I18n.translate('channelConfig.overflowValue', 'MDC', 'Overflow value'),
                items: [
                    {
                        xtype: 'numberfield',
                        name: 'overruledOverflowValue',
                        msgTarget: 'under',
                        itemId: 'mdc-editOverflowValueField',
                        width: 150,
                        hideTrigger: true,
                        maxLength: 15, // don't increase this value. Javascript can't handle precise values larger than 9007199254740992
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
                width: 450,
                layout: 'hbox',
                margin: '10 0 0 0',
                fieldLabel: Uni.I18n.translate('channelConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                items: [
                    {
                        xtype: 'numberfield',
                        name: 'overruledNbrOfFractionDigits',
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
                        itemId: 'btn-save-channel',
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                        ui: 'action',
                        action: 'saveChannel'
                    },
                    {
                        xtype: 'button',
                        itemId: 'btn-cancel-editChannel',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        ui: 'link',
                        action: 'cancelEditChannel',
                        href: me.returnLink
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    setChannel: function(channelRecord) {
        var me = this,
            newTitle = channelRecord.get('readingType').fullAliasName;

        me.setTitle(newTitle);
        me.loadRecord(channelRecord);
    }

});