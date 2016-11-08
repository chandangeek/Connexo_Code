Ext.define('Imt.metrologyconfiguration.view.PurposesField', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.purposes-field',
    requires: [
        'Uni.grid.column.Check',
        'Uni.grid.column.ReadingType'
    ],

    mixins: {
        field: 'Ext.form.field.Field',
        bindable: 'Ext.util.Bindable'
    },

    hasNotEmptyComponent: true,

    initComponent: function () {
        var me = this;

        me.grid = {
            xtype: 'grid',
            itemId: 'purposes-grid',
            title: Uni.I18n.translate('general.purposes', 'IMT', 'Purposes'),
            ui: 'medium',
            cls: 'uni-selection-grid',
            style: 'padding-left: 0;padding-right: 0;',
            store: null,
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
                    flex: 2
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
            ],
            listeners: {
                select: function (selectionModel, record) {
                    var outputsGrid = me.down('#outputs-grid'),
                        readingTypeDeliverables = record.readingTypeDeliverables();


                    Ext.suspendLayouts();
                    if (readingTypeDeliverables.getCount()) {
                        outputsGrid.show();
                        outputsGrid.setTitle(record.get('name'));
                        outputsGrid.reconfigure(readingTypeDeliverables);
                    } else {
                        outputsGrid.hide();
                    }
                    Ext.resumeLayouts(true);
                }
            }
        };

        me.previewComponent = {
            xtype: 'grid',
            itemId: 'outputs-grid',
            ui: 'medium',
            style: 'padding-left: 0;padding-right: 0;',
            store: null,
            columns: [
                {
                    header: Uni.I18n.translate('general.outputName', 'IMT', 'Output name'),
                    dataIndex: 'name',
                    flex: 1
                },
                {
                    xtype: 'reading-type-column',
                    dataIndex: 'readingType'
                }
            ]
        };

        me.callParent(arguments);
    },

    setStore: function (store) {
        var me = this;

        Ext.suspendLayouts();
        me.down('#purposes-grid').reconfigure(store);
        me.bindStore(store);
        store.fireEvent('load');
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
                    value.push(record.getId());
                }
            });
        }

        return !Ext.isEmpty(value) ? value : null;
    },

    getRawValue: function () {
        var me = this;

        return me.getValue().toString();
    },

    setValue: Ext.emptyFn,

    markInvalid: Ext.emptyFn,

    clearInvalid: Ext.emptyFn
});