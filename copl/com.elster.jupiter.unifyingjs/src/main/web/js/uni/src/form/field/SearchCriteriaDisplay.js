Ext.define('Uni.form.field.SearchCriteriaDisplay', {
    extend: 'Ext.form.FieldContainer',
    requires: [
        'Uni.store.search.Properties'
    ],
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.search-criteria-display',
    readOnly: true,
    submitValue: false,
    defaultType: 'displayfield',

    initComponent: function () {
        var me = this;

        if (me.value) {
            me.items = me.prepareContent(me.value);
        }

        me.callParent(arguments);
    },

    setValue: function (value) {
        var me = this;

        me.value = value;
        Ext.suspendLayouts();
        me.removeAll();
        me.add(me.prepareItems(value));
        Ext.resumeLayouts(true);
    },

    prepareContent: function (value) {
        var me = this,
            items = [],
            primaryCriteria = [],
            currentGroup;

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
                    fieldLabel: criteria.group.displayValue,
                    labelStyle: 'color: #1E7D9E',
                    itemId: 'group-label-' + criteria.group.id
                });
                currentGroup = criteria.group.id;
            }
            items.push({
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

            if (condition.operator === 'between') {
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
                    displayValue +=  prefix + Ext.htmlEncode(me.formatValue(criteria, value));
                });
            }
        });

        return displayValue;
    },

    formatValue: function (criteria, value) {
        if (criteria.selectionMode === 'multiple') {
            return Ext.Array.findBy(criteria.values, function (possibleValue) {
                return value == possibleValue.id;
            }).displayValue
        }

        switch (criteria.type + ':' + criteria.factoryName) {
            case 'Date:com.energyict.mdc.dynamic.DateFactory':
                return Uni.DateTime.formatDateShort(new Date (value));
            case 'Date:com.energyict.mdc.dynamic.DateAndTimeFactory':
                return Uni.DateTime.formatDateTimeShort(new Date (value));
            case 'Boolean:com.elster.jupiter.properties.BooleanFactory':
                return value
                    ? Uni.I18n.translate('general.yes', 'UNI', 'Yes')
                    : Uni.I18n.translate('general.no', 'UNI', 'No');
        }

        return value;
    }
});