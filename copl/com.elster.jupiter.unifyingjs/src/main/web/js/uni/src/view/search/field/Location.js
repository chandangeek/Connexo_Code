/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.Location', {
    extend: 'Uni.view.search.field.Selection',
    requires: [
        'Uni.property.form.Property',
        'Uni.property.form.GroupedPropertyForm',
        'Uni.model.LocationInfo'
    ],
    alias: 'widget.uni-search-criteria-location',
    minWidth: 350,
    value: null,
    selType: 'rowmodel',
    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'toolbar',
                padding: '0 5 5 5',
                dock: 'bottom',
                style: {
                    'background-color': '#fff !important'
                },
                items: [
                    {
                        xtype: 'button',
                        itemId: 'clearall',
                        text: Uni.I18n.translate('general.clearAll', 'UNI', 'Clear all'),
                        align: 'right',
                        action: 'reset',
                        disabled: true,
                        handler: me.reset,
                        scope: me,
                        hidden: true  // Always (CXO-3259)
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.down('#grid-selection').selModel.updateHeaderState = Ext.emptyFn;
        me.on('afterrender', me.createCriteriaLine, me);
    },

    createCriteriaLine: function () {
        var me = this;

        me.down('#empty-values').setVisible(false);
        me.down('#docked-items').insert(2, {
            xtype: 'container',
            items: [
                {
                    xtype: 'checkboxfield',
                    checked: false,
                    itemId: 'advanced-search',
                    boxLabel: Uni.I18n.translate('search.field.selection.checkbox.advancedSerach', 'UNI', 'Advanced search'),
                    name: 'topping',
                    listeners: {
                        change: {
                            fn: me.onAdvancedSearchChange,
                            scope: me
                        }
                    }
                },
                {
                    xtype: 'form',
                    height: 355,
                    itemId: 'properties-form',
                    autoScroll: true
                }
            ]
        });

        me.loadLocationInfo();
        me.onAdvancedSearchChange(null, false, null);
    },

    onValueChange: function () {
        var value = this.getValue(),
            clearBtn = this.down('#clearall');

        this.callParent(arguments);
        if (clearBtn) {
            clearBtn.setDisabled(!!Ext.isEmpty(value));
        }
    },

    onAdvancedSearchChange: function (field, newValue, oldValue) {
        var me = this;

        me.down('#filter-operator').setDisabled(newValue);
        me.down('#filter-input').setDisabled(newValue);
        me.down('#properties-form').setVisible(newValue);
        //me.down('#clearall').setVisible(!newValue);   // No "Clear all" button (CXO-3259)
        me.down('#grid-selection').body.setVisible(!newValue);

        newValue && me.hasChanged();
        !newValue && me.fireEvent('change', me, me.getValue());
    },

    hasChanged: function () {
        var me = this,
            propertiesFormValues = me.down('#properties-form').getValues(), values = [];
        for (var propertyName in propertiesFormValues) {
            if (propertiesFormValues.hasOwnProperty(propertyName) && !Ext.isEmpty(propertiesFormValues[propertyName])) {
                values.push(
                    {
                        propertyName: propertyName,
                        propertyValue: propertiesFormValues[propertyName]
                    });
            }
        }
        me.fireEvent('change', this, values.length == 0 ? "" : [
            Ext.create('Uni.model.search.Value', {
                operator: '==',
                criteria: {values: values}
            })
        ]);
    },

    loadLocationInfo: function () {
        var me = this,
            propertyForm = me.down('#property-form-location'),
            propertiesForm = me.down('#properties-form'),
            applicationName = Ext.Ajax.defaultHeaders['X-CONNEXO-APPLICATION-NAME'],
            url = (applicationName === 'MDC') ? '/api/ddr/devices/locations/0' :
                (applicationName === 'INS') ? '/api/udr/usagepoints/locations/0' : '';

        Ext.Ajax.request({
            url: url,
            method: 'GET',
            success: function (response) {
                var model = new Uni.model.LocationInfo();
                var reader = model.getProxy().getReader();
                var resultSet = reader.readRecords(Ext.decode(response.responseText));
                var recordProperties = resultSet.records[0];

                if (recordProperties && recordProperties.properties() && recordProperties.properties().count()) {
                    recordProperties.properties().each(function (record) {
                        propertiesForm.add(Ext.create('Ext.form.field.Text', {
                            fieldLabel: record.get('name'),
                            name: record.get('key'),
                            labelWidth: 140,
                            width: 300,
                            listeners: {
                                change: {
                                    fn: me.hasChanged,
                                    scope: me
                                }
                            }
                        }));
                    });
                }

                if (me.value && me.value[0] && me.value[0].get('criteria') && (me.value[0].get('criteria').values instanceof Array)) {
                    me.down('#advanced-search').setValue(true);
                    Ext.Array.each(me.value[0].get('criteria').values, function (val) {
                        var component = Ext.ComponentQuery.query('[name=' + val.propertyName + ']');
                        if (component && component instanceof Array && component.length > 0) {
                            component[0].setValue(val.propertyValue);
                        }
                    });
                }
            }
        })
    },

    setValue: function (value) {
        var me = this,
            store = me.getStore();

        me.value = value;
        if (value && value[0] && value[0].get('criteria') && (value[0].get('criteria').values instanceof Array == false)) {
            if (me.value[0].get('filter') == me.down('#filter-input').getValue()) {
                Uni.view.search.field.Location.superclass.setValue.call(me, me.value);
            }
            else {
                me.down('#filter-input').setValue(me.value[0].get('filter'));
                store.addListener('load', me.onLocationsStoreLoaded, me);
            }
        }
    },

    onLocationsStoreLoaded: function () {
        var me = this;

        Uni.view.search.field.Location.superclass.setValue.call(me, me.value);
    },

    getValue: function () {
        var me = this, value;

        me.selection.sort();
        value = me.selection.getRange().map(function (item) {
            return item.get(me.valueField)
        });

        return value.length ? [Ext.create('Uni.model.search.Value', {
            operator: this.down('#filter-operator').getValue(),
            criteria: value,
            filter: me.down('#filter-input').getValue()
        })] : null
    }
});