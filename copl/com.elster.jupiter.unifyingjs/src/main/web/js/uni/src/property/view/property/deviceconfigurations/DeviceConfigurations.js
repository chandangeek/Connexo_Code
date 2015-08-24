Ext.define('Uni.property.view.property.deviceconfigurations.DeviceConfigurations', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.property.view.property.deviceconfigurations.AddDeviceConfigurationsView',
        'Uni.property.model.PropertyDeviceConfiguration',
        'Uni.property.store.PropertyDeviceConfigurations'
    ],

    currentPageView: null,
    addDeviceConfigurationsView: null,

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'fieldcontainer',
            itemId: 'add-device-configurations-fieldcontainer',
            name: me.getName(),
            width: 800,
            readOnly: me.isReadOnly,
            layout: 'hbox',
            items: [
                {
                    xtype: 'displayfield',
                    itemId: 'add-device-configurations-empty-text',
                    value: Uni.I18n.translate('deviceconfigurations.noAdded', 'UNI', 'There are no device configurations added yet to this')
                },
                {
                    xtype: 'grid',
                    itemId: 'add-device-configurations-grid',
                    store: Ext.create('Ext.data.Store', {
                        model: 'Uni.property.model.PropertyDeviceConfiguration'
                    }),
                    width: 500,
                    padding: 0,
                    maxHeight: 323,
                    columns: [
                        {
                            header: Uni.I18n.translate('deviceconfigurations.deviceConfiguration', 'UNI', 'Device configuration'),
                            dataIndex: 'name',
                            flex: 1,
                            renderer: function (value) {
                                return value ? Ext.htmlEncode(value) : '';
                            }
                        },
                        {
                            header: Uni.I18n.translate('deviceconfigurations.deviceType', 'UNI', 'Device type'),
                            dataIndex: 'deviceTypeName',
                            flex: 1
                        },
                        {
                            xtype: 'actioncolumn',
                            header: Uni.I18n.translate('general.action', 'UNI', 'Action'),
                            align: 'center',
                            items: [{
                                iconCls: 'uni-icon-delete',
                                tooltip: Uni.I18n.translate('general.remove', 'UNI', 'Remove'),
                                handler: function (grid, rowIndex) {
                                    grid.getStore().removeAt(rowIndex);
                                    me.setValue();
                                }
                            }]
                        }
                    ]
                },
                {
                    xtype: 'button',
                    itemId: 'add-device-configurations-add-button',
                    text: Uni.I18n.translate('deviceconfigurations.addDeviceConfigurations', 'UNI', 'Add device configurations'),
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
            },
        }
    },

    getValue: function () {
        var me = this,
            result = me.recordsToIds(me.down('grid').getStore().getRange());

        return result.length ? result : null;
    },

    setValue: function (value) {
        var me = this,
            grid = me.down('#add-device-configurations-grid'),
            emptyMessage = me.down('#add-device-configurations-empty-text'),
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
        me.addDeviceConfigurationsView = Ext.widget('uni-add-device-configurations-view', {
            itemId: me.key + 'uni-add-device-configurations-view'
        });
        selectionGrid = me.addDeviceConfigurationsView.down('uni-add-device-configurations-grid');
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
            me.setValue(me.recordsToIds(me.addDeviceConfigurationsView.down('uni-add-device-configurations-grid').getStore().getRange()));
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
            breadcrumbTrail.addBreadcrumbItem({data:{text: Uni.I18n.translate('deviceconfigurations.addDeviceConfigurations', 'UNI', 'Add device configurations')}});
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