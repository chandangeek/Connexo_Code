/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.CommandFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.commands-overview-filter',
    store: undefined,

    requires:[
        'Mdc.store.DeviceGroups',
        'Mdc.store.CommandCategories',
        'Mdc.store.Commands'
    ],

    initComponent: function () {
        var me = this;
        me.filters = [
            {
                type: 'combobox',
                itemId: 'mdc-commands-filter-device-group-combo',
                dataIndex: 'deviceGroups',
                emptyText: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Mdc.store.DeviceGroups'
            },
            {
                type: 'combobox',
                itemId: 'mdc-commands-filter-category-combo',
                dataIndex: 'messageCategories',
                emptyText: Uni.I18n.translate('general.commandCategory', 'MDC', 'Command category'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Mdc.store.CommandCategories',
                listeners: {
                    //collapse: {
                    //    scope: me,
                    //    fn: me.onCategoryCollapse
                    //},
                    change: {
                        scope: me,
                        fn: me.onCategoryChange
                    }
                }
            },
            {
                type: 'combobox',
                itemId: 'mdc-commands-filter-command-combo',
                dataIndex: 'deviceMessageIds',
                emptyText: Uni.I18n.translate('general.commandName', 'MDC', 'Command name'),
                multiSelect: true,
                displayField: 'command',
                valueField: 'commandName',
                store: 'Mdc.store.Commands',
                loadStore: false,
                disabled: true
            }
        ];
        me.callParent(arguments);
    },

    onCategoryChange: function(categoryCombo, newValue, oldValue) {
        var me = this,
            commandCombo = me.down('#mdc-commands-filter-command-combo'),
            commandStore = commandCombo.getStore();

        if ( Ext.isDefined(oldValue) && !me.mainArrayCcontainsSubArray(newValue, oldValue) ) {
            me.clearCombo(commandCombo);
        }
        if (categoryCombo.getValue().length > 0) {
            commandCombo.setDisabled(false);
            delete commandStore.getProxy().extraParams.filter;
            commandStore.getProxy().setExtraParam('filter',
                Ext.encode([
                {
                    property: 'categories',
                    value: categoryCombo.getValue()
                }
            ]));
            commandStore.load(function () {
                commandStore.sort('name', 'ASC');
            });
        }
    },

    clearCombo: function (combo) {
        combo.setValue('');
        combo.setDisabled(true);
    },


    mainArrayCcontainsSubArray: function(mainArray, subArray) {
        mainArray.sort();
        subArray.sort();
        var i, j;
        for (i=0,j=0; i<mainArray.length && j<subArray.length;) {
            if (mainArray[i] < subArray[j]) {
                ++i;
            } else if (mainArray[i] == subArray[j]) {
                ++i;
                ++j;
            } else {
                return false; // subArray[j] not in mainArray
            }
        }
        return j === subArray.length; // make sure there are no elements left in sub
    }

    //enableClearAll: function (filters) {
        //var me = this,
        //    enableClearAllBasedOnOtherThanFromTo = Ext.Array.filter(filters, function (filter) {
        //            return filter.property !== 'from' && filter.property !== 'to' && !Ext.Array.contains(me.noUiFilters, filter.property);
        //        }).length > 0,
        //    fromFilter = _.find(filters, function (item) {
        //        return item.property === 'from';
        //    }),
        //    toFilter = _.find(filters, function (item) {
        //        return item.property === 'to';
        //    }),
        //    fromToFilterIsDefault = fromFilter && me.filterDefault.from && fromFilter.value === me.filterDefault.from.getTime() &&
        //        toFilter && me.filterDefault.to && toFilter.value === me.filterDefault.to.getTime();
        //
        //Ext.suspendLayouts();
        //me.down('button[action=clearAll]').setDisabled(enableClearAllBasedOnOtherThanFromTo ? false : fromToFilterIsDefault);
        //Ext.resumeLayouts(true);
    //}

});
