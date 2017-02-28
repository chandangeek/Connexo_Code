/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.PurposesField', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.purposes-field',
    requires: [
        'Uni.grid.column.Check',
        'Uni.form.field.ReadingTypeDisplay'
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
                    flex: 2,
                    renderer: function (value, metaData, record) {
                        var description = record.get('description');

                        return value + (description
                                ? '<span class="icon-info" style="color: #A9A9A9; margin-left: 10px; font-size: 16px; vertical-align: middle;" data-qtip="'
                            + description + '"></span>'
                                : '');
                    }
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
                    var outputsDetailsPanel = me.down('#purpose-outputs-details-panel'),
                        outputsContainer = me.down('#purpose-outputs-container'),
                        readingTypeDeliverables = record.readingTypeDeliverables();

                    Ext.suspendLayouts();
                    outputsDetailsPanel.setTitle(record.get('name'));
                    outputsContainer.removeAll();
                    if (readingTypeDeliverables.getCount()) {
                        readingTypeDeliverables.each(function (readingTypeDeliverable) {
                            outputsContainer.add({
                                xtype: 'fieldcontainer',
                                fieldLabel: undefined,
                                layout: 'hbox',
                                width: '100%',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        value: readingTypeDeliverable.get('name'),
                                        style: 'margin-right: 10px'
                                    },
                                    {
                                        xtype: 'reading-type-displayfield',
                                        fieldLabel: undefined,
                                        value: readingTypeDeliverable.get('readingType')
                                    }
                                ]
                            });
                        });
                    } else {
                        outputsContainer.add({
                            xtype: 'displayfield',
                            value: '-'
                        });
                    }
                    Ext.resumeLayouts(true);
                }
            },
            bbar: {
                xtype: 'component',
                itemId: 'purposes-field-errors',
                cls: 'x-form-invalid-under',
                hidden: true
            }
        };

        me.previewComponent = {
            xtype: 'form',
            itemId: 'purpose-outputs-details-panel',
            frame: true,
            items: {
                xtype: 'fieldcontainer',
                itemId: 'purpose-outputs-container',
                fieldLabel: Uni.I18n.translate('general.outputs', 'IMT', 'Outputs')
            }
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
                    value.push(_.pick(record.getData(), 'id'));
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

    markInvalid: function (errors) {
        var me = this,
            errorsField = me.down('#purposes-field-errors');

        Ext.suspendLayouts();
        errorsField.show();
        errorsField.update(errors.join('<br>'));
        Ext.resumeLayouts(true);
    },

    clearInvalid: function (errors) {
        var me = this,
            errorsField = me.down('#purposes-field-errors');

        Ext.suspendLayouts();
        errorsField.hide();
        errorsField.update();
        Ext.resumeLayouts(true);
    }
});