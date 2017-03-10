/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.SearchCriteriaDisplay', {
    extend: 'Ext.form.FieldContainer',
    requires: [
        'Uni.property.store.TimeUnits',
        'Uni.util.Common',
        'Uni.DateTime'
    ],
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.search-criteria-display',
    readOnly: true,
    submitValue: false,
    defaultType: 'displayfield',
    stores: [
        'Uni.property.store.TimeUnits'
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        if (me.value) {
            Uni.util.Common.loadNecessaryStores(me.stores, function () {
                me.setValue(me.value);
            });
        }
    },

    setValue: function (value) {
        var me = this;

        me.value = value;
        Ext.suspendLayouts();
        me.removeAll();
        me.add(me.prepareContent(value));
        Ext.resumeLayouts(true);
    },

    prepareContent: function (value) {
        var me = this,
            items = [],
            primaryCriteria = [],
            currentGroup;

        value = Ext.clone(value);

        // add sort option for each value item
        Ext.Array.each(value, function (criteria, index) {
            if (criteria.visibility === 'sticky') {
                criteria.sortOption = '0';
            } else {
                if (!criteria.group) {
                    criteria.sortOption = ('1' + criteria.displayValue).replace(/\s/, '')
                } else {
                    criteria.sortOption = ('2' + criteria.group.displayValue + criteria.displayValue).replace(/\s/, '');
                }
            }
        });

        Ext.Array.each(_.sortBy(value, 'sortOption'), function (criteria) {
            if (criteria.group && criteria.group.id !== currentGroup) {
                items.push({
                    itemId: 'group-label-' + criteria.group.id,
                    fieldLabel: criteria.group.displayValue,
                    labelStyle: 'color: #1E7D9E'
                });
                currentGroup = criteria.group.id;
            }
            items.push({
                itemId: 'search-criteria-' + criteria.name,
                fieldLabel: criteria.displayValue,
                value: me.getDisplayValueOfCriteria(criteria),
                htmlEncode: false
            });
        });

        return items;
    },

    getDisplayValueOfCriteria: function (criteria) {
        var me = this,
            displayValue = '';

        Ext.Array.each(criteria.value, function (condition, conditionIndex) {
            var prefix = '';

            if (conditionIndex) {
                displayValue += '<br><br>';
            }

            if (condition.operator.toLowerCase() === 'between') {
                displayValue = Uni.I18n.translate('searchCriteriaDisplay.between', 'UNI', 'between {0} and {1}',
                    [me.formatValue(criteria, condition.criteria[0]), me.formatValue(criteria, condition.criteria[1])],
                    false);
            } else {
                if (condition.operator !== '==') {
                    prefix = condition.operator + ' ';
                }
                Ext.Array.each(condition.criteria, function (value, valueIndex) {
                    if (valueIndex) {
                        displayValue += '<br>';
                    }
                    displayValue += prefix + Ext.htmlEncode(me.formatValue(criteria, value));
                });
            }
        });

        return displayValue;
    },

    formatValue: function (criteria, value) {
        var formatTimeDuration = function (timeDuration) {
                var arr = value.split(':');
                return arr[1]
                    + ' '
                    + Ext.getStore('Uni.property.store.TimeUnits').find('code', arr[1]).get('localizedValue');
            },
            selectFromPossibleValues = function (desiredValue) {
                return Ext.Array.findBy(criteria.values, function (possibleValue) {
                    return desiredValue == possibleValue.id;
                }).displayValue
            };

        if (criteria.selectionMode === 'multiple' && criteria.type != 'Quantity') {
            return selectFromPossibleValues(value);
        }

        switch (criteria.type + ':' + criteria.factoryName) {
            case 'Date:com.energyict.mdc.dynamic.DateFactory':
                return Uni.DateTime.formatDateShort(new Date(parseInt(value)));
            case 'Date:com.energyict.mdc.dynamic.DateAndTimeFactory':
                return Uni.DateTime.formatDateTimeShort(new Date(parseInt(value)));
            case 'Instant:com.elster.jupiter.properties.InstantFactory':
                return Uni.DateTime.formatDateTimeShort(new Date(parseInt(value)));
            case 'TimeOfDay:com.energyict.mdc.dynamic.TimeOfDayFactory':
                return Uni.DateTime.formatTimeShort(new Date(parseInt(value) * 60000));
            case 'Boolean:com.elster.jupiter.properties.BooleanFactory':
                return value
                    ? Uni.I18n.translate('general.yes', 'UNI', 'Yes')
                    : Uni.I18n.translate('general.no', 'UNI', 'No');
            case 'TimeDuration:com.energyict.mdc.dynamic.TimeDurationValueFactory':
                return formatTimeDuration(value);
            case 'TimeDuration:com.elster.jupiter.properties.StringReferenceFactory':
                return formatTimeDuration(value);
            case 'Quantity:com.elster.jupiter.properties.QuantityValueFactory':
                return value.replace(/(\d*)\:\d*\:.*/, '$1') + ' ' + selectFromPossibleValues(value.replace(/\d*(\:\d*\:.*)/, '0$1'));
        }

        return value;
    }
});