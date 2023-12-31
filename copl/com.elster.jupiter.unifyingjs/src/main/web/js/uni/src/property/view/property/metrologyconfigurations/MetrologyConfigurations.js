/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.metrologyconfigurations.MetrologyConfigurations', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.property.view.property.metrologyconfigurations.AddMetrologyConfigurationsView',
        'Uni.property.model.PropertyDeviceConfiguration',
        'Uni.property.store.PropertyDeviceConfigurations',
        'Uni.model.BreadcrumbItem',
        'Uni.grid.column.RemoveAction'
    ],

    currentPageView: null,
    addDeviceConfigurationsView: null,

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'fieldcontainer',
            itemId: 'add-metrology-configurations-fieldcontainer',
            name: me.getName(),
            width: 800,
            readOnly: me.isReadOnly,
            layout: 'hbox',
            items: [
                {
                    xtype: 'displayfield',
                    itemId: 'add-metrology-configurations-empty-text',
                    value: Uni.I18n.translate('metrologyconfigurations.noAdded', 'UNI', 'There are no metrology configurations added yet to this')
                },
                {
                    xtype: 'grid',
                    itemId: 'add-metrology-configurations-grid',
                    store: Ext.create('Ext.data.Store', {
                        model: 'Uni.property.model.PropertyDeviceConfiguration'
                    }),
                    width: 500,
                    padding: 0,
                    maxHeight: 323,
                    columns: [
                        {
                            header: Uni.I18n.translate('metrologyconfigurations.metrologyConfiguration', 'UNI', 'Metrology configuration'),
                            dataIndex: 'name',
                            flex: 1,
                            renderer: function (value) {
                                return value ? Ext.htmlEncode(value) : '';
                            }
                        },
                        {
                            xtype: 'uni-actioncolumn-remove',
                            handler: function (grid, rowIndex) {
                                grid.getStore().removeAt(rowIndex);
                                me.setValue();
                            }
                        }
                    ]
                },
                {
                    xtype: 'button',
                    itemId: 'add-metrology-configurations-add-button',
                    text: Uni.I18n.translate('metrologyconfigurations.addMetrologyConfigurations', 'UNI', 'Add metrology configurations'),
                    action: 'addDeviceConfigurations',
                    margin: '0 0 0 10',
                    handler: Ext.bind(me.showAddView, me)
                }
            ],
            // add validation support
            isFormField: true,
            markInvalid: function (errors) {
                this.down('displayfield').markInvalid(errors);
            },
            clearInvalid: function () {
                this.down('displayfield').clearInvalid();
            },
            isValid: function () {
                return true;
            },
            getModelData: function () {
                return null;
            }
        }
    },

    getValue: function () {
        var me = this,
            result = me.recordsToIds(me.down('grid').getStore().getRange());

        return result.length ? result : null;
    },

    setValue: function (value) {
        var me = this,
            grid = me.down('#add-metrology-configurations-grid'),
            emptyMessage = me.down('#add-metrology-configurations-empty-text'),
            store = grid.getStore();

        Ext.suspendLayouts();
        if (Ext.isArray(value)) {
            store.loadData(me.idsToRecords(value), true);
        }

        if (store.getCount()) {
            grid.show();
            emptyMessage.hide();
        } else {
            grid.hide();
            emptyMessage.show();
        }
        Ext.resumeLayouts(true);
    },

    showAddView: function () {
        var me = this,
            addedConfigurations = me.getValue() || [],
            selectionGrid,
            store;

        me.currentPageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0].down();
        me.addDeviceConfigurationsView = Ext.widget('uni-add-metrology-configurations-view', {
            itemId: me.key + 'uni-add-metrology-configurations-view'
        });
        selectionGrid = me.addDeviceConfigurationsView.down('uni-add-metrology-configurations-grid');
        store = selectionGrid.getStore();

        selectionGrid.on('allitemsadd', me.hideAddView, me, {single: true});
        selectionGrid.on('selecteditemsadd', me.hideAddView, me, {single: true});
        selectionGrid.getCancelButton().on('click', me.hideAddView, me, {single: true});

        Ext.suspendLayouts();
        me.currentPageView.hide();
        Ext.ComponentQuery.query('viewport > #contentPanel')[0].add(me.addDeviceConfigurationsView);
        store.loadData(me.getProperty().getPossibleValues() || []);
        store.filterBy(function (record, id) {
            return !Ext.Array.contains(addedConfigurations, id);
        });
        me.setPseudoNavigation(true);
        Ext.resumeLayouts(true);
        store.fireEvent('load');
    },

    hideAddView: function () {
        var me = this;

        if (arguments[0] && Ext.isArray(arguments[0])) {
            me.setValue(me.recordsToIds(arguments[0]));
        } else if (arguments[0] && arguments[0].single) {
            me.setValue(me.recordsToIds(me.addDeviceConfigurationsView.down('uni-add-metrology-configurations-grid').getStore().getRange()));
        }
        Ext.suspendLayouts();
        me.setPseudoNavigation();
        me.addDeviceConfigurationsView.destroy();
        me.currentPageView.show();
        Ext.resumeLayouts(true);
    },

    setPseudoNavigation: function (toAddDeviceConfigurations) {
        var me = this,
            breadcrumbTrail = Ext.ComponentQuery.query('breadcrumbTrail')[0],
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];

        if (toAddDeviceConfigurations) {
            lastBreadcrumbLink.renderData.href = window.location.href;
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
            breadcrumbTrail.addBreadcrumbItem(Ext.create('Uni.model.BreadcrumbItem', {
                text: Uni.I18n.translate('metrologyconfigurations.addMetrologyConfigurations', 'UNI', 'Add metrology configurations'),
                relative: false
            }));
            lastBreadcrumbLink.getEl().on('click', me.hideAddView, me, {single: true});
        } else {
            lastBreadcrumbLink.destroy();
            breadcrumbTrail.query('breadcrumbSeparator:last')[0].destroy();
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];
            lastBreadcrumbLink.renderData.href = '';
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
        }
    },

    recordsToIds: function (data) {
        var modifiedData = [];

        Ext.Array.each(data, function (record) {
            modifiedData.push(record.getId());
        });

        return modifiedData;
    },

    idsToRecords: function (data) {
        var me = this,
            modifiedData = [],
            store = Ext.getStore('Uni.property.store.PropertyDeviceConfigurations');

        if (!store.getCount()) {
            store.loadData(me.getProperty().getPossibleValues());
        }

        Ext.Array.each(data, function (id) {
            modifiedData.push(store.getById(id));
        });

        return modifiedData;
    }
});