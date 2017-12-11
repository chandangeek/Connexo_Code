/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.util.readingtypesgroup.FilterTopPanel', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.reading-types-group-filter-top-panel',
    requires: [
        'Mtr.store.readingtypes.attributes.TimeOfUse',
        'Mtr.store.readingtypes.attributes.UnitOfMeasures',
        'Mtr.store.readingtypes.attributes.Status',
        'Mtr.store.readingtypes.attributes.Interval',
        'Mtr.store.readingtypes.attributes.Kind',
        'Mtr.store.readingtypes.attributes.InterharmonicNumerator',
        'Mtr.store.readingtypes.attributes.InterharmonicDenominator',
        'Mtr.store.readingtypes.attributes.ArgumentNumerator',
        'Uni.grid.filtertop.ClosableCombobox'
    ],
    layout: 'form',
    majorFilters: [
        {
            type: 'text',
            dataIndex: 'fullAliasName',
            emptyText: Uni.I18n.translate('readingTypes.attribute.name', 'MTR', 'Name'),
            displayField: 'name',
            valueField: 'code',
            itemId: 'reading-type-name'
        },
        {
            type: 'combobox',
            dataIndex: 'active',
            emptyText: Uni.I18n.translate('readingTypes.attribute.status', 'MTR', 'Status'),
            displayField: 'displayName',
            valueField: 'code',
            itemId: 'reading-type-status',
            store: 'Mtr.store.readingtypes.attributes.Status'
        },
        {
            type: 'combobox',
            dataIndex: 'unit',
            emptyText: Uni.I18n.translate('readingTypes.attribute.unitOfMeasure', 'MTR', 'Unit of measure'),
            displayField: 'displayName',
            valueField: 'code',
            multiSelect: true,
            itemId: 'reading-type-unit-of-measure',
            store: 'Mtr.store.readingtypes.attributes.UnitOfMeasures'
        },
        {
            type: 'combobox',
            dataIndex: 'timeOfUse',
            emptyText: Uni.I18n.translate('readingTypes.attribute.ToU', 'MTR', 'Time of use'),
            displayField: 'displayName',
            valueField: 'code',
            multiSelect: true,
            itemId: 'reading-type-time-of-use',
            store: 'Mtr.store.readingtypes.attributes.TimeOfUse'
        },
        {
            type: 'combobox',
            dataIndex: 'measurementPeriod',
            emptyText: Uni.I18n.translate('readingTypes.attribute.interval', 'MTR', 'Interval'),
            displayField: 'displayName',
            valueField: 'code',
            multiSelect: true,
            itemId: 'reading-type-time',
            store: 'Mtr.store.readingtypes.attributes.MeasuringPeriod'
        }
    ],

    minorFilters: [
        {
            text: Uni.I18n.translate('readingTypes.attribute.kind', 'MTR', 'Kind'),
            name: 'kind',
            filterConfig: {
                type: 'closablecombobox',
                dataIndex: 'measurementKind',
                emptyText: Uni.I18n.translate('readingTypes.attribute.kind', 'MTR', 'Kind'),
                displayField: 'displayName',
                valueField: 'code',
                multiSelect: true,
                forceSelection: true,
                queryMode: 'local',
                itemId: 'reading-type-kind',
                store: 'Mtr.store.readingtypes.attributes.Kind'
            }
        },
        {
            text: Uni.I18n.translate('readingTypes.attribute.interharmonicDenominator', 'MTR', 'Interharmonic denominator'),
            name: 'interharmonicDenominator',
            filterConfig: {
                type: 'closablecombobox',
                dataIndex: 'interHarmonicDenominator',
                emptyText: Uni.I18n.translate('readingTypes.attribute.interharmonicDenominator', 'MTR', 'Interharmonic denominator'),
                displayField: 'displayName',
                valueField: 'code',
                multiSelect: true,
                forceSelection: true,
                queryMode: 'local',
                itemId: 'reading-type-interharmonic-interharmonic-denominator',
                store: 'Mtr.store.readingtypes.attributes.InterharmonicDenominator'
            }
        },
        {
            text: Uni.I18n.translate('readingTypes.attribute.interharmonicNumerator', 'MTR', 'Interharmonic numerator'),
            name: 'interharmonicNumerator',
            filterConfig: {
                type: 'closablecombobox',
                dataIndex: 'interHarmonicNumerator',
                emptyText: Uni.I18n.translate('readingTypes.attribute.interharmonicNumerator', 'MTR', 'Interharmonic numerator'),
                displayField: 'displayName',
                valueField: 'code',
                multiSelect: true,
                forceSelection: true,
                queryMode: 'local',
                itemId: 'reading-type-interharmonic-numerator',
                store: 'Mtr.store.readingtypes.attributes.InterharmonicNumerator'
            }
        },
        {
            text: Uni.I18n.translate('readingTypes.attribute.argumentNumerator', 'MTR', 'Argument numerator'),
            name: 'argumentNumerator',
            filterConfig: {
                type: 'closablecombobox',
                dataIndex: 'argumentNumerator',
                emptyText: Uni.I18n.translate('readingTypes.attribute.argumentNumerator', 'MTR', 'Argument numerator'),
                displayField: 'displayName',
                valueField: 'code',
                multiSelect: true,
                forceSelection: true,
                queryMode: 'local',
                itemId: 'reading-type-interharmonic-argument-numerator',
                store: 'Mtr.store.readingtypes.attributes.ArgumentNumerator'
            }
        }
    ],


    setValueForDataIndex: function (dataIndex, value) {
        var me = this,
            addMinor = true;

        Ext.suspendLayouts();

        me.filters.each(function (filter) {
            if (filter.dataIndex === dataIndex) {
                filter.setFilterValue(value);
                addMinor = false;
                return false;
            }
        }, me);

        if (!Ext.isEmpty(me.filters) && addMinor) {
            Ext.each(me.minorFilters, function (minorFilter) {
                if (minorFilter.filterConfig.dataIndex === dataIndex) {
                    var config = {};
                    Ext.merge(config, minorFilter.filterConfig);
                    me.addMinorFilter(config).setFilterValue(value);
                    return false;
                }
            });
        }

        Ext.resumeLayouts(true);
    },

    addFilter: function (filter, isMinor) {
        var me = this,
            component = me.createFilter(filter);

        if (Ext.isDefined(component)) {
            me.filters.add(component);
            if (filter.type !== 'noui') {
                if (isMinor) {
                    var minorCont = me.down('#minor-filters-container');
                    if (!minorCont) {
                        minorCont = me.insert(1, {
                            xtype: 'container',
                            padding: 0,
                            itemId: 'minor-filters-container',
                            layout: {
                                type: 'column',
                                tdAttrs: {
                                    style: 'padding: 10px;'
                                }
                            }
                        });
                    }
                    minorCont.add(component)
                } else {
                    var majorCont = me.down('#major-filters-container');
                    if (!majorCont) {
                        majorCont = me.insert(0, {
                            xtype: 'container',
                            padding: 0,
                            itemId: 'major-filters-container',
                            layout: {
                                type: 'column',
                                tdAttrs: {
                                    style: 'padding: 10px;'
                                }
                            }
                        });
                    }
                    majorCont.add(component)
                }
            }
            component.on('filterupdate', me.applyFilters, me);
        }
        return component
    },

    onAddMenuClick: function (menu, item) {
        var me = this,
            index = me.filters.findIndex('dataIndex', item.filterConfig);
        me.addMinorFilter(item.filterConfig);
        item.hide()
    },

    addMinorFilter: function (config) {
        var me = this,
            handlerConfig = {
                removeHandler: function (btn) {
                    me.onCloseFilter(btn.up('[type=closablecombobox]'))
                }
            };
        Ext.apply(config, handlerConfig);
        var result = me.addFilter(config, true);
        me.configureMenu();
        return result
    },

    onCloseFilter: function (cmp) {
        var me = this;
        cmp.resetValue();
        cmp.deleted = true;
        me.down('#minor-filters-container').remove(cmp);
        me.configureMenu()
    },


    configureMenu: function () {
        var me = this,
            addFilterBtn = me.down('#add-filter-button'),
            menu = addFilterBtn.menu,
            allItemsHidden = true;
        menu.items.each(function (menuItem) {
            var filterItem = me.down('[dataIndex=' + menuItem.filterConfig.dataIndex + ']');
            if (filterItem) {
                menuItem.rendered ? menuItem.hide() : menuItem.hidden = true
            } else {
                menuItem.rendered ? menuItem.show() : menuItem.hidden = false;
                allItemsHidden = false
            }
        });
        allItemsHidden ? addFilterBtn.disable() : addFilterBtn.enable()
    },

    initComponent: function () {
        var me = this;

        me.filters = me.majorFilters;
        me.additionalCmps = [
            {
                xtype: 'button',
                itemId: 'add-filter-button',
                text: Uni.I18n.translate('readingTypes.addFilter', 'MTR', 'Add filter'),
                menu: {
                    xtype: 'menu',
                    itemId: 'add-minor-filter-menu',
                    listeners: {
                        click: function (menu, item) {
                            me.onAddMenuClick(menu, item)
                        }
                    },
                    items: me.minorFilters
                }
            }
        ];

        me.items = [
            {
                xtype: 'container',
                itemId: 'major-filters-container',
                layout: 'hbox',
                height: 129
            },
            {
                xtype: 'container',
                itemId: 'minor-filters-container',
                layout: 'hbox'
            }
        ];
        me.callParent(arguments)
    }
});
