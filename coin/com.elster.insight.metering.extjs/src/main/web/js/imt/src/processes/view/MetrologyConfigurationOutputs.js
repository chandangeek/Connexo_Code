/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.MetrologyConfigurationOutputs', {
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
                var me = this;

                me.deliverablesStore.getProxy().url = '/api/udr/usagepoints/' + me.up('property-form').context.id + '/deliverables';
                me.deliverablesStore.load(function () {
                    me.getGrid().getSelectionModel().selectAll();
                });
            }
        }
    },

    getEditCmp: function () {
        var me = this;

        me.deliverablesStore = Ext.create('Ext.data.Store', {
            fields: ['name', 'readingType'],
            proxy: {
                type: 'rest',
                reader: {
                    type: 'json',
                    root: 'deliverables'
                }
            }
        });
        me.name = me.getName();

        return [
            {
                xtype: 'grid',
                itemId: 'metrology-configuration-outputs-grid',
                store: me.deliverablesStore,
                width: 600,
                margin: 0,
                padding: 0,
                selModel: Ext.create('Ext.selection.CheckboxModel', {
                    mode: 'MULTI',
                    checkOnly: true,
                    showHeaderCheckbox: false,
                    pruneRemoved: false,
                    updateHeaderState: Ext.emptyFn
                }),
                columns: [
                    {
                        header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                        dataIndex: 'name',
                        flex: 1
                    },
                    {
                        xtype: 'reading-type-column',
                        header: Uni.I18n.translate('general.readingType', 'IMT', 'Reading type'),
                        dataIndex: 'readingType',
                        flex: 1
                    }
                ]
            }
        ];
    },

    getGrid: function () {
        return this.down('grid');
    },

    setValue: function (value) {
    },

    getValue: function () {
        var me = this;

        return _.map(me.getGrid().getSelectionModel().getSelection(), function (record) {
            return record.get('readingType').mRID;
        }).join(';');
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