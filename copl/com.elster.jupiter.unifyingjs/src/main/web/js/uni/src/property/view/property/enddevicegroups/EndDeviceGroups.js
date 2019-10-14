/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
 
Ext.define('Uni.property.view.property.enddevicegroups.EndDeviceGroups', {
    extend: 'Uni.property.view.property.Base',
    requires: [
		'Uni.property.view.property.enddevicegroups.AddEndDeviceGroupsView',
        'Uni.property.model.PropertyEndDeviceGroup',
		'Uni.property.store.PropertyEndDeviceGroups',
		'Uni.model.BreadcrumbItem',
        'Uni.grid.column.RemoveAction',
    ],

    currentPageView: null,
    addDeviceGroupsView: null,

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'fieldcontainer',
            itemId: 'add-device-groups-fieldcontainer',
            name: me.getName(),
            width: 800,
            readOnly: me.isReadOnly,
            layout: 'hbox',
            items: [
                {
                    xtype: 'displayfield',
                    itemId: 'add-device-groups-empty-text',
                    value: Uni.I18n.translate('deviceGroups.noAdded', 'UNI', 'There are no device groups excluded yet')
                },
                {
                    xtype: 'grid',
                    itemId: 'add-device-groups-grid',
                    store: Ext.create('Ext.data.Store', {
                        model: 'Uni.property.model.PropertyEndDeviceGroup'
                    }),
                    width: 500,
                    padding: 0,
                    maxHeight: 323,
                    columns: [
                        {
                            header: Uni.I18n.translate('deviceGroups.deviceGroupName', 'UNI', 'Device group name'),
                            dataIndex: 'name',
                            flex: 1,
                            renderer: function (value) {
                                return value ? Ext.htmlEncode(value) : '';
                            }
                        },
                        {
                            header: Uni.I18n.translate('deviceGroups.dynamic', 'UNI', 'Dymanic'),
                            dataIndex: 'dynamic',
                            flex: 1
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
                    itemId: 'add-device-groups-add-button',
                    text: Uni.I18n.translate('deviceGroups.addDeviceGroups', 'UNI', 'Add device groups'),
                    action: 'addDeviceGroups',
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
            grid = me.down('#add-device-groups-grid'),
            emptyMessage = me.down('#add-device-groups-empty-text'),
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
            addedGroups = me.getValue() || [],
            selectionGrid,
            store,
			filteredOutGroupIds = '';

        me.currentPageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0].down();
        me.addDeviceGroupsView = Ext.widget('uni-add-device-groups-view', {
            itemId: me.key + 'uni-add-device-groups-view-id'
        });
        selectionGrid = me.addDeviceGroupsView.down('uni-device-groups-selection-grid');
        store = selectionGrid.getStore();

        me.addDeviceGroupsView.down('#saveOperation').on('click', me.hideAddViewOnSave, me, {single: true});
        me.addDeviceGroupsView.down('#cancelOperation').on('click', me.hideAddView, me, {single: true});

        Ext.suspendLayouts();
        me.currentPageView.hide();
        Ext.ComponentQuery.query('viewport > #contentPanel')[0].add(me.addDeviceGroupsView);
		
		if(addedGroups && addedGroups.length > 0) {
			Ext.Array.each(addedGroups, function(groupId) {
				if (filteredOutGroupIds) {
					filteredOutGroupIds = filteredOutGroupIds.concat(',', groupId);
				} else {
					filteredOutGroupIds = filteredOutGroupIds.concat(groupId);
				}
			});
		}
		store.getProxy().setExcludedGroups(filteredOutGroupIds);
		selectionGrid.down('pagingtoolbartop').resetPaging();
		selectionGrid.down('pagingtoolbarbottom').resetPaging();
		store.load(function (records, operation, success) {
			if (success == true) {
				me.setPseudoNavigation(true);
				Ext.resumeLayouts(true);
			}
		});
    },

    hideAddView: function () {
        var me = this;
        Ext.suspendLayouts();
        me.setPseudoNavigation();
        me.addDeviceGroupsView.destroy();
        me.currentPageView.show();
        Ext.resumeLayouts(true);
    },
	
	hideAddViewOnSave: function () {
        var me = this;
        me.setValue(me.recordsToIds(me.addDeviceGroupsView.down('uni-device-groups-selection-grid').getSelectionModel().getSelection()));
		me.hideAddView();
    },

    setPseudoNavigation: function (toAddDeviceGroups) {
        var me = this,
            breadcrumbTrail = Ext.ComponentQuery.query('breadcrumbTrail')[0],
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];

        if (toAddDeviceGroups) {
            lastBreadcrumbLink.renderData.href = window.location.href;
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
            breadcrumbTrail.addBreadcrumbItem(Ext.create('Uni.model.BreadcrumbItem', {
                text: Uni.I18n.translate('deviceGroupExclusions.excludeEndDeviceGroups', 'UNI', 'Exclude end device groups'),
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
            store = Ext.getStore('Uni.property.store.PropertyEndDeviceGroups');

        if (!store.getCount()) {
            store.loadData(me.getProperty().getPossibleValues());
        }

        Ext.Array.each(data, function (id) {
            modifiedData.push(store.getById(id));
        });

        return modifiedData;
    }
});