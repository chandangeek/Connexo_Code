/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
 
Ext.define('Uni.property.view.property.comtasks.ComTasks', {
    extend: 'Uni.property.view.property.Base',
    requires: [
		'Uni.property.view.property.comtasks.AddComTasksView',
		'Uni.property.model.PropertyCommunicationTask',
		'Uni.property.store.PropertyCommunicationTasks',
		'Uni.property.store.PropertyCommunicationTasksCurrentValue',
		'Uni.model.BreadcrumbItem',
        'Uni.grid.column.RemoveAction',
    ],

    currentPageView: null,
    addComTasksView: null,
	previousComponentValue: null,
	componentIsDisabled: false,

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'fieldcontainer',
            itemId: 'add-comtasks-fieldcontainer',
            name: me.getName(),
            width: 800,
            readOnly: me.isReadOnly,
            layout: 'hbox',
            items: [
                {
                    xtype: 'displayfield',
                    itemId: 'add-comtasks-empty-text',
                    value: Uni.I18n.translate('comTasks.noAdded', 'UNI', 'There are no communication tasks filtered out yet')
                },
				{
                    xtype: 'displayfield',
                    itemId: 'add-comtasks-disabled-text',
                    value: Uni.I18n.translate('comTasks.componentDisabled', 'UNI', 'Communication task filtering not possible for the selected event type')
                },
                {
                    xtype: 'grid',
                    itemId: 'add-comtasks-grid',
                    store: Ext.create('Ext.data.Store', {
                        model: 'Uni.property.model.PropertyCommunicationTask'
                    }),
                    width: 500,
                    padding: 0,
                    maxHeight: 323,
                    columns: [
                        {
							header: Uni.I18n.translate('general.name', 'UNI', 'Name'),
							dataIndex: 'name',
							flex: 1,
							renderer: function (value) {
                                return value ? Ext.htmlEncode(value) : '';
                            }
						},
						{
							header: Uni.I18n.translate('general.systemTask', 'UNI', 'Is system ComTask'),
							dataIndex: 'systemTask',
							renderer: function (value) {
								if (value) {
									return Uni.I18n.translate('general.yes', 'UNI', 'Yes')
								} else {
									return Uni.I18n.translate('general.no', 'UNI', 'No')
								}
							},
							flex: 1
						},
                        {
                            xtype: 'uni-actioncolumn-remove',
                            handler: function (grid, rowIndex) {
                                grid.getStore().removeAt(rowIndex);
                                me.updateGrid();
                            }
                        }
                    ]
                },
                {
                    xtype: 'button',
                    itemId: 'id-comtasks-add-button',
                    text: Uni.I18n.translate('comTasks.addComTasks', 'UNI', 'Add com tasks'),
                    action: 'addComTasks',
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
			isDirty: function () {
				return false;
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
            grid = me.down('#add-comtasks-grid'),
            emptyMessage = me.down('#add-comtasks-empty-text'),
			disabledMessage = me.down('#add-comtasks-disabled-text'),
            store = grid.getStore(),
			eventTypePropertyField = me.up().getComponent('BasicDataCollectionRuleTemplate.eventType');

		if(value && Ext.isArray(value) && value.length > 0) {
			Ext.suspendLayouts();
			me.idsToRecords(value, function(records) {
				if (Ext.isArray(records)) {
					store.loadData(records, true);
				}
				disabledMessage.hide();
				if (store.getCount()) {
					grid.show();
					emptyMessage.hide();
				} else {
					grid.hide();
					emptyMessage.show();
				}
				Ext.resumeLayouts(true);
			});
		} else {
			if(eventTypePropertyField) {
				var eventTypeValue = eventTypePropertyField.getValue();
				if(eventTypeValue && eventTypePropertyField.comTaskEventTypes.includes(eventTypeValue)) {
					me.updateComponent(false);
				} else {
					me.updateComponent(true);
				}
			} else {
				me.updateComponent(true);
			}
		}
    },
	
	memorizeCurrentComponentValue: function () {
		var me = this;
		me.previousComponentValue = me.down('grid').getStore().getRange();
	},
	
	restoreCurrentComponentValue: function (value) {
		var me = this,
			store = me.down('grid').getStore();
		if(value && Ext.isArray(value)) {
			store.loadData(value, true);
		}
	},

	updateComponent: function (doDisable) {
		var me = this,
			grid = me.down('#add-comtasks-grid'),
            emptyMessage = me.down('#add-comtasks-empty-text'),
			disabledMessage = me.down('#add-comtasks-disabled-text'),
			addComTaskButton = me.down('#id-comtasks-add-button');
		Ext.suspendLayouts();
		if(doDisable) {
			addComTaskButton.setDisabled(true);
			grid.hide();
			emptyMessage.hide();
			disabledMessage.show();
			if(me.componentIsDisabled == false) {
				me.memorizeCurrentComponentValue();
				grid.getStore().removeAll();
				me.componentIsDisabled = true;
			}
		} else {
			if(me.componentIsDisabled) {
				me.restoreCurrentComponentValue(me.previousComponentValue);
				me.componentIsDisabled = false;
			}
			disabledMessage.hide();
			me.updateGrid();
			addComTaskButton.setDisabled(false);
		}
		Ext.resumeLayouts(true);
	},
	
	updateGrid: function () {
		var me = this,
			grid = me.down('#add-comtasks-grid'),
            emptyMessage = me.down('#add-comtasks-empty-text'),
            store = grid.getStore();
			
		Ext.suspendLayouts();
		
		if(store.getCount()) {
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
            addedComTasks = me.getValue() || [],
            selectionGrid,
            store,
			filteredOutComTaskIds = '';

        me.currentPageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0].down();
        me.addComTasksView = Ext.widget('uni-add-comtasks-view', {
            itemId: me.key + 'uni-add-comtasks-view-id'
        });
        selectionGrid = me.addComTasksView.down('uni-comtasks-selection-grid');
        store = selectionGrid.getStore();

        me.addComTasksView.down('#saveOperation').on('click', me.hideAddViewOnSave, me, {single: true});
        me.addComTasksView.down('#cancelOperation').on('click', me.hideAddView, me, {single: true});

        Ext.suspendLayouts();
        me.currentPageView.hide();
        Ext.ComponentQuery.query('viewport > #contentPanel')[0].add(me.addComTasksView);
		
		if(addedComTasks && addedComTasks.length > 0) {
			Ext.Array.each(addedComTasks, function(comTaskId) {
				if (filteredOutComTaskIds) {
					filteredOutComTaskIds = filteredOutComTaskIds.concat(',', comTaskId);
				} else {
					filteredOutComTaskIds = filteredOutComTaskIds.concat(comTaskId);
				}
			});
		}
		store.getProxy().setExcludedComTaskIds(filteredOutComTaskIds);
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
        me.addComTasksView.destroy();
        me.currentPageView.show();
        Ext.resumeLayouts(true);
    },
	
	hideAddViewOnSave: function () {
        var me = this;
        me.setValue(me.recordsToIds(me.addComTasksView.down('uni-comtasks-selection-grid').getSelectionModel().getSelection()));
		me.hideAddView();
    },

    setPseudoNavigation: function (toAddComTasks) {
        var me = this,
            breadcrumbTrail = Ext.ComponentQuery.query('breadcrumbTrail')[0],
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];

        if (toAddComTasks) {
            lastBreadcrumbLink.renderData.href = window.location.href;
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
            breadcrumbTrail.addBreadcrumbItem(Ext.create('Uni.model.BreadcrumbItem', {
                text: Uni.I18n.translate('comTaskFiltering.filterOutComTasks', 'UNI', 'Filter out communication tasks'),
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

	idsToRecords: function (data, callback) {
        var me = this,
			ids = '',
            modifiedData = [],
			store = Ext.getStore('Uni.property.store.PropertyCommunicationTasksCurrentValue');
			
		if(Ext.isArray(data)) {
			Ext.Array.each(data, function (id) {
				if(!id) {
					return;
				}
				if(ids) {
					ids = ids.concat(',',id);
				} else {
					ids = ids.concat(id);
				}
			});
			if (ids) {
				store.getProxy().setComTaskIds(ids);
				store.load(function (records, operation, success) {
					if (success == true) {
						callback(records);
					}
				});
			}
		}
    },
    
    disableComponent: function() {
		var me = this;
		me.updateComponent(true);
	},
	
	enableComponent: function() {
		var me = this;
		me.updateComponent(false);
	}
});