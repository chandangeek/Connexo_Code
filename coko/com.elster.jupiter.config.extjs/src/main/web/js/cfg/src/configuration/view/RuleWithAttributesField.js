/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.RuleWithAttributes
 */
Ext.define('Cfg.configuration.view.RuleWithAttributesField', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.rule-with-attributes-field',
    labelAlign: 'top',
    requires: [
        'Uni.property.form.Property'
    ],
    labelWidth: 250,
    defaults: {
        labelWidth: 250,
        xtype: 'displayfield'
    },
    record: undefined,
    type: null,
    kindOfReadingType: '',

    initComponent: function () {
        var me = this,
            record = me.record,
            activeIcon = '<span class="icon-checkmark-circle" style="color: #33CC33; margin-left: 10px;" data-qtip="' + Uni.I18n.translate('general.active', 'CFG', 'Active') + '"></span>',
            inactiveIcon = '<span class="icon-blocked" style="color: #eb5642; margin-left: 10px" data-qtip="' + Uni.I18n.translate('general.inactive', 'CFG', 'Inactive') + '"></span>',
            icon = !!record.get('isActive') ? activeIcon : inactiveIcon;

        me.fieldLabel = record.get('name') + icon;
        if (me.type === 'validation' && !record.get('isEffective')) {
            me.fieldLabel += '<span class="icon-history" style="margin-left: 10px;" data-qtip="' + Uni.I18n.translate('general.futureOrPastVersion', 'CFG', 'Future or past version') + '"></span>';
        }
        if (me.type === 'validation') {
            me.items = [
                {
                    value: me.record.get('validator'),
                    fieldLabel: Uni.I18n.translate('validation.validator', 'CFG', 'Validator'),
                    itemId: 'validator' + me.record.getId() + me.kindOfReadingType
                },
                {
                    value: me.record.get('dataQualityLevel'),
                    fieldLabel: Uni.I18n.translate('validation.dataQualityLevel', 'CFG', 'Data quality level'),
                    itemId: 'level' + me.record.getId() + me.kindOfReadingType
                }
            ];
        } else {
            me.items = [
                {
                    value: me.record.get('estimator'),
                    fieldLabel: Uni.I18n.translate('general.estimator', 'CFG', 'Estimator'),
                    itemId: 'validator' + me.record.getId() + me.kindOfReadingType
                }
            ];
        }
        me.items.push({
            xtype: 'property-form',
            itemId: 'property-form' + me.record.getId() + me.kindOfReadingType,
            isEdit: false
        });

        me.callParent();
        me.down('property-form').loadRecord(me.record);
    }
});
