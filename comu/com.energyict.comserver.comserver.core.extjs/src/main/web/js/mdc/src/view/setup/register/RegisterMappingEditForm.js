/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.register.RegisterMappingEditForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.edit-register-mapping-form',
    width: '100%',
    defaults: {
        labelWidth: 150,
        width: 700
    },

    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ReadingTypeCombo',
        'Uni.form.field.CustomAttributeSetSelector',
        'Mdc.store.CustomAttributeSetsOnRegister'
    ],

    items: [
        {
            xtype: 'combobox',
            fieldLabel: Uni.I18n.translate('general.readingType', 'MDC', 'Reading type'),
            itemId: 'edit-register-mapping-reading-type-field',
            name: 'readingType',
            required: true,
            disabled: true
        },
        {
            xtype: 'obis-field',
            itemId: 'edit-register-mapping-obis-field',
            disabled: true,
            afterSubTpl: null,
            fieldLabel: Uni.I18n.translate('loadProfileTypes.obisCode', 'MDC', 'OBIS code'),
            name: 'obisCode',
            msgTarget: 'under'
        },
        {
            xtype: 'custom-attribute-set-selector',
            itemId: 'edit-register-mapping-attribute-set-combo',
            fieldLabel: Uni.I18n.translate('deviceloadprofiles.customattributeset', 'MDC', 'Custom attribute set'),
            comboWidth: 535,
            comboStore: 'Mdc.store.CustomAttributeSetsOnRegister'
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: '&nbsp',
            items: [
                {
                    xtype: 'button',
                    itemId: 'save-register-mapping-type-button',
                    text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                    ui: 'action'
                },
                {
                    xtype: 'button',
                    itemId: 'cancel-edit-register-mapping-type-button',
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    ui: 'link'
                }
            ]
        }
    ],

    customLoadRecord: function (record, deviceTypeId) {
        var me = this,
            readingTypeField = me.down('#edit-register-mapping-reading-type-field'),
            obisField = me.down('#edit-register-mapping-obis-field'),
            customAttributesCombo = me.down('#edit-register-mapping-attribute-set-combo');

        me.record = record;
        Ext.suspendLayouts();
        readingTypeField.setValue(record.get('name'));
        obisField.setValue(record.get('obisCode'));
        customAttributesCombo.getStore().getProxy().setUrl(deviceTypeId);
        customAttributesCombo.getStore().load(function () {
            if (record.get('customPropertySet') && record.get('customPropertySet').id) {
                customAttributesCombo.setValue(record.get('customPropertySet').id);
            } else {
                customAttributesCombo.setValue(null);
            }
        });
        Ext.resumeLayouts(true);
        me.down('#save-register-mapping-type-button').on('click', me.showConfirmationMessage, me);
    },

    showConfirmationMessage: function () {
        var me = this,
            customAttributesCombo = me.down('#edit-register-mapping-attribute-set-combo');

        if (!!me.record.get('customPropertySet') && me.record.get('customPropertySet').id !== customAttributesCombo.getValue().id) {
            Ext.create('Uni.view.window.Confirmation', {confirmText: Uni.I18n.translate('general.change', 'MDC', 'Change')}).show({
                msg: Uni.I18n.translate('customattributeset.confirmWindow.changeDescription', 'MDC', 'Changing the custom attribute set removes any values that were defined for the previous set.'),
                title: Uni.I18n.translate('customattributeset.confirmWindow.change', 'MDC', "Change custom attribute set?"),
                scope: me,
                value: customAttributesCombo.getValue(),
                fn: me.fireSaveEvent
            });
        } else {
            me.fireSaveEvent('confirm', null, {scope: me, value: customAttributesCombo.getValue()})
        }
    },

    fireSaveEvent: function (state, text, conf) {
        var me = conf.scope,
            value = conf.value;

        if (state === 'confirm') {
            me.fireEvent('saverecord', me.record, value);
        }
    }
});

