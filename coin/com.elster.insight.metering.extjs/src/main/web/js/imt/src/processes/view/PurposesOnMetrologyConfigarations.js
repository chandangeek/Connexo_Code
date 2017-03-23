/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.PurposesOnMetrologyConfigarations', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.grid.column.ReadingType'
    ],

    mixins: {
        field: 'Ext.form.field.Field'
    },

    msgTarget: 'under',
    width: 600,

    listeners: {
        afterrender: {
            fn: function () {
                var me = this,
                store = Ext.getStore('Imt.processes.store.PurposesWithValidationRuleSets');

                store.on('load', function () {
                    me.setStore(store);
                });

                store.on('clearPurposeStore', function () {
                    store.removeAll();
                });
            }
        }
    },

    getEditCmp: function () {
        var me = this;

        me.name = me.getName();
        return [
            {
                xtype: 'grid',
                itemId: 'purposes-grid',
                width: 335,
                margin: 0,
                padding: 0,
                ui: 'medium',
                cls: 'uni-selection-grid',
                style: 'padding-left: 0;padding-right: 0;',
                columns: [
                    {
                        xtype: 'uni-checkcolumn',
                        dataIndex: 'active',
                        isDisabled: function (record) {
                            return record.get('mandatory');
                        }
                    },
                    {
                        header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                        dataIndex: 'name',
                        flex: 1
                    },
                    {
                        header: Uni.I18n.translate('general.required', 'IMT', 'Required'),
                        dataIndex: 'mandatory',
                        flex: 1,
                        renderer: function (value) {
                            return value
                                ? Uni.I18n.translate('general.yes', 'IMT', 'Yes')
                                : Uni.I18n.translate('general.no', 'IMT', 'No')
                        }
                    }
                ]
            }
        ];
    },


    setStore: function (store) {
        var me = this;

        Ext.suspendLayouts();
        me.down('grid').reconfigure(store);
        me.down('grid').bindStore(store);
        Ext.resumeLayouts(true);
    },

    getValue: function () {
        var me = this,
            store = me.down('#purposes-grid').getStore(),
            value;

        if (store) {
            value = [];

            store.each(function (record) {
                if (record.get('active')) {
                    value.push(record.get('id'));
                }
            });
        }

        return !Ext.isEmpty(value) ? value.join(';') : null;
    },

    getGrid: function () {
        return this.down('grid');
    },

    setValue: function (value) {
    },

    getRawValue: function () {
        var me = this;

        return me.getValue().toString();
    },

    markInvalid: function (error) {
        var me = this;

        me.toggleInvalid(error);
    },

    clearInvalid: function () {
        var me = this;

        me.toggleInvalid();
    },

    toggleInvalid: function (error) {
        var me = this,
            oldError = me.getActiveError(),
            grid = me.getGrid();

        Ext.suspendLayouts();
        if (error) {
            me.setActiveErrors(error);
        } else {
            me.unsetActiveError();
        }
        if (oldError !== me.getActiveError()) {
            me.doComponentLayout();
        }
        Ext.resumeLayouts(true);
    },

    getValueAsDisplayString: function (value) {
        var me = this;

        return '-';
    }
});