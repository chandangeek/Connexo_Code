/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.EditCustomAttributeSetForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.edit-custom-attribute-set-form',
    width: '100%',
    defaults: {
        labelWidth: 150,
        width: 700
    },

    requires: [
        'Uni.form.field.Obis',
        'Uni.grid.column.ReadingType',
        'Uni.form.field.CustomAttributeSetSelector',
        'Mdc.store.Intervals',
        'Mdc.store.SelectedRegisterTypesForLoadProfileType',
        'Mdc.store.CustomAttributeSetsOnLoadProfile',
        'Uni.grid.column.RemoveAction'
    ],

    items: [
        {
            xtype: 'textfield',
            itemId: 'edit-custom-attribute-name',
            name: 'name',
            allowBlank: false,
            disabled: true,
            required: true,
            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            msgTarget: 'under'
        },
        {
            xtype: 'obis-field',
            itemId: 'edit-custom-attribute-obis-field',
            disabled: true,
            afterSubTpl: null,
            fieldLabel: Uni.I18n.translate('loadProfileTypes.obisCode', 'MDC', 'OBIS code'),
            name: 'obisCode',
            msgTarget: 'under'
        },
        {
            xtype: 'combobox',
            disabled: true,
            itemId: 'edit-custom-attribute-time-duration',
            fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
            name: 'timeDuration',
            displayField: 'name',
            valueField: 'id',
            forceSelection: true,
            required: true
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
            itemId: 'register-types-fieldcontainer',
            required: true,
            disabled: true,
            msgTarget: 'under',
            items: [
                {
                    xtype: 'panel',
                    width: 530,
                    items: [
                        {
                            xtype: 'gridpanel',
                            itemId: 'edit-custom-attribute-register-types-grid',
                            hideHeaders: true,
                            disabled: true,
                            padding: 0,
                            overflowY: 'hidden',
                            autoHeight: true,
                            columns: [
                                {
                                    xtype: 'reading-type-column',
                                    dataIndex: 'readingType',
                                    flex: 1
                                },
                                {
                                    xtype: 'uni-actioncolumn-remove',
                                    align: 'right'
                                }
                            ]
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'custom-attribute-set-selector',
            itemId: 'edit-custom-attribute-set-combo',
            fieldLabel: Uni.I18n.translate('deviceloadprofiles.customattributeset', 'MDC', 'Custom attribute set'),
            comboWidth: 535,
            comboStore: 'Mdc.store.CustomAttributeSetsOnLoadProfile'
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: '&nbsp',
            items: [
                {
                    xtype: 'button',
                    itemId: 'save-load-profile-type-button',
                    text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                    ui: 'action'
                },
                {
                    xtype: 'button',
                    itemId: 'cancel-edit-load-profile-type-button',
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    ui: 'link'
                }
            ]
        }
    ],

    customLoadRecord: function (record, deviceTypeId) {
        var me = this,
            nameField = me.down('#edit-custom-attribute-name'),
            obisField = me.down('#edit-custom-attribute-obis-field'),
            intervalCombo = me.down('#edit-custom-attribute-time-duration'),
            registersGrid = me.down('#edit-custom-attribute-register-types-grid'),
            customAttributesCombo = me.down('#edit-custom-attribute-set-combo'),
            intervalsStore = Ext.create('Mdc.store.Intervals'),
            registersStore = Ext.create('Mdc.store.SelectedRegisterTypesForLoadProfileType');

        me.record = record;
        Ext.suspendLayouts();
        nameField.setValue(record.get('name'));
        obisField.setValue(record.get('obisCode'));
        intervalsStore.load(function () {
            intervalCombo.bindStore(intervalsStore);
            intervalCombo.setValue(record.get('timeDuration').id);
        });
        registersStore.add(record.get('registerTypes'));
        registersGrid.bindStore(registersStore);

        customAttributesCombo.getStore().getProxy().setUrl(deviceTypeId);
        customAttributesCombo.getStore().load(function () {
            if (record.get('customPropertySet') && record.get('customPropertySet').id) {
                customAttributesCombo.setValue(record.get('customPropertySet').id);
            } else {
                customAttributesCombo.setValue(null);
            }
        });

        Ext.resumeLayouts(true);
        me.down('#save-load-profile-type-button').on('click', me.showConfirmationMessage, me);
    },

    showConfirmationMessage: function () {
        var me = this,
            customAttributesCombo = me.down('#edit-custom-attribute-set-combo');

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

