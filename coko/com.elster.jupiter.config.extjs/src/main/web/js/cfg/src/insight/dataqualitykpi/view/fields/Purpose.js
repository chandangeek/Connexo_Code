/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.view.fields.Purpose', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.purpose-field',
    mixins: [
        'Ext.form.field.Field'
    ],
    style: 'margin-top: 38px',
    msgTarget: 'under',
    store: Ext.create('Ext.data.Store', {
        fields: ['id', 'name'],
        data: [
            {id: 1, name: 'Purpose A'},
            {id: 2, name: 'Purpose B'},
            {id: 3, name: 'Purpose C'}
        ]
    }),

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                margin: '-30px 0 4 0',
                defaultType: 'button',
                items: [
                    {
                        itemId: 'btn-check-all',
                        text: Uni.I18n.translate('general.checkAll', 'CFG', 'Check all'),
                        listeners: {
                            click: {
                                scope: me,
                                fn: me.checkAll
                            }
                        }
                    },
                    {
                        itemId: 'btn-uncheck-all',
                        text: Uni.I18n.translate('general.unCheckAll', 'CFG', 'Uncheck all'),
                        disabled: true,
                        listeners: {
                            click: {
                                scope: me,
                                fn: me.uncheckAll
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'grid',
                itemId: 'selection-grid',
                cls: 'uni-selection-grid',
                hideHeaders: true,
                padding: 0,
                scroll: 'vertical',
                store: me.store,
                maxHeight: 292,
                selType: 'checkboxmodel',
                selModel: {
                    mode: 'MULTI',
                    checkOnly: true
                },
                columns: [
                    {
                        dataIndex: 'name',
                        flex: 1
                    }
                ],
                listeners: {
                    selectionChange: {
                        scope: me,
                        fn: me.selectionChange
                    }
                }
            }
        ];

        me.callParent(arguments);
    },

    setValue: function (value) {
        var me = this,
            grid = me.down('#selection-grid'),
            selectionModel = grid.getSelectionModel(),
            store = grid.getStore();

        Ext.suspendLayouts();
        selectionModel.deselectAll();
        selectionModel.select(_.map(value, getRecordById));
        Ext.resumeLayouts(true);
        me.value = value;

        function getRecordById(id) {
            return store.getById(id);
        }
    },

    getValue: function () {
        var me = this,
            grid = me.down('#selection-grid');

        return _.map(grid.getSelectionModel().getSelection(), getRecordId);

        function getRecordId(record) {
            return record.getId();
        }
    },

    getRawValue: function () {
        var me = this;

        return me.getValue().toString();
    },

    markInvalid: function (error) {
        var me = this;

        me.setActiveErrors(error);
        me.doComponentLayout();
    },

    clearInvalid: function () {
        var me = this;

        me.unsetActiveError();
        me.doComponentLayout();
    },

    checkAll: function () {
        var me = this,
            grid = me.down('#selection-grid');

        grid.getSelectionModel().selectAll();
    },

    uncheckAll: function () {
        var me = this,
            grid = me.down('#selection-grid');

        grid.getSelectionModel().deselectAll();
    },

    selectionChange: function (selectionModel, selectedRecords) {
        var me = this,
            grid = me.down('#selection-grid');

        Ext.suspendLayouts();
        me.down('#btn-check-all').setDisabled(grid.getStore().getRange().length === selectedRecords.length);
        me.down('#btn-uncheck-all').setDisabled(!selectionModel.hasSelection());
        Ext.resumeLayouts(true);
    }
});