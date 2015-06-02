Ext.define('Fim.controller.History', {
    extend: 'Ext.app.Controller',
    requires: [
        'Fim.privileges.DataImport'
    ],
    views: [
        'Fim.view.history.History'
    ],
    stores: [
        'Fim.store.ImportServicesFilter',
        'Fim.store.Status'

    ],
    models: [
        'Fim.model.HistoryFilter'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'fim-import-service-history'
        },
		{
			ref: 'historyViewMenu',
			selector: '#history-view-menu'
		},
        {
            ref: 'sideFilterForm',
            selector: '#history-filter-form #filter-form'
        },
        {
            ref: 'filterTopPanel',
            selector: '#flt-import-service-history-top-panel'
        },
        {
            ref: 'statusFilter',
            selector: '#cbo-status'
        },
        {
            ref: 'historyGrid',
            selector: 'fim-import-service-history fim-history-grid'
        },
        {
            ref: 'historySortToolbar',
            selector: '#fim-history-sort-toolbar'
        }
    ],
    applicationName: null,

    init: function () {
        this.control({
            'fim-import-service-history fim-history-grid': {
                select: this.showPreview
            },
            'fim-import-service-history filter-top-panel': {
                removeFilter: this.removeFilter,
                clearAllFilters: this.clearFilter
            },
            'fim-import-service-history #history-filter-form uni-filter-combo': {
                updateTopFilterPanelTagButtons: this.onFilterChange
            },
            'fim-import-service-history button[action=applyfilter]': {
                click: this.applyFilter
            },
            'fim-import-service-history button[action=clearfilter]': {
                click: this.clearFilter
            },
            'fim-history-sort-menu': {
                click: this.chooseSort
            },
            '#fim-history-sort-toolbar button[action=clear]': {
                click: this.clearSort
            },
            '#fim-history-sort-toolbar button': {
                closeclick: this.sortCloseclick
            },
            '#fim-history-sort-toolbar #itemsContainer button': {
                click: this.switchSort
            },
			'fim-history-action-menu': {
                click: this.chooseAction
            },

        });

        applicationName = typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : '';
    },

    showImportServicesHistory: function (importServiceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Fim.store.ImportServicesHistory'),
            importServiceModel = me.getModel('Fim.model.ImportService'),
			showImportService = (importServiceId === undefined),
			historyViewMenu = me.getHistoryViewMenu(),
            view;

		me.setDefaultSort(router.filter);
		store.setFilterModel(router.filter);
        store.getProxy().setUrl(router.arguments);
		
        view = Ext.widget('fim-import-service-history', {
            router: router,
            importServiceId: importServiceId,
            showImportService: showImportService
			
        });
		
		me.getApplication().fireEvent('changecontentevent', view);
		me.getHistoryViewMenu().setTitle(showImportService ? Uni.I18n.translate('general.importHistory', 'FIM', 'Import history') : Uni.I18n.translate('general.importService', 'FIM', 'Import services'));
		me.initFilter();
       
        
		
		if (!showImportService){
			importServiceModel.load(importServiceId, {
				success: function (record) {
					view.down('#history-view-menu #import-service-view-link').setText(record.get('name'));
					me.getApplication().fireEvent('importserviceload', record);
				}
			});
		}

    },

    initFilter: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filter = router.filter,
            date;

        me.getSideFilterForm().loadRecord(filter);

        for (var f in filter.getData()) {
            var name = '', val = filter.get(f), filterDisplay = [];

            if ((f === 'status') && !Ext.isEmpty(val)) {
                var combo = me.getStatusFilter();
                var store = combo.getStore();

                Ext.Array.each(val, function (v) {
                    var rec = store.findRecord('value', v);
                    if (rec) {
                        filterDisplay.push(rec.get('display'));
                    }
                });


                me.getFilterTopPanel().setFilter(combo.getName(), combo.getFieldLabel(), filterDisplay);
            }
        }

        me.setFilterTimeInterval('startedOnFrom', filter.get('startedOnFrom'), 'startedFrom', 'Started from');
        me.setFilterTimeInterval('startedOnTo', filter.get('startedOnTo'), 'startedFrom', 'Started to');
        me.setFilterTimeInterval('finishedOnFrom', filter.get('finishedOnFrom'), 'finishedFrom', 'Finished from');
        me.setFilterTimeInterval('finishedOnTo', filter.get('finishedOnTo'), 'finishedTo', 'Finished to');
        me.setSortingToolbar(filter);
        me.getFilterTopPanel().setVisible(true);
    },

    setSortingToolbar: function (filter) {
        var me = this,
            page = me.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer();

        sortContainer.removeAll();
		//me.setDefaultSort(filter);
		sorting = filter.get('sorting');
				
        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {

                if (sortItem.value) {
                    menuItem = me.getPage().down('#menu-history-sort [name=' + sortItem.type + ']');
                    cls = sortItem.value === Uni.component.sort.model.Sort.ASC
                        ? 'x-btn-sort-item-asc'
                        : 'x-btn-sort-item-desc';

                    //menuItem.hide();
                    sortContainer.add({
                        xtype: 'sort-item-btn',
                        itemId: 'history-sort-by-' + sortItem.type + '-button',
                        text: menuItem.text,
                        sortType: sortItem.type,
                        sortDirection: sortItem.value,
                        iconCls: cls/*,
                        listeners: {
                            sortCloseclick: function () {
                                this.fireEvent('removeSort', this.sortType, this.sortDirection);
                            },
                            switchSort: function () {
                                this.fireEvent('changeSortDirection', this.sortType, this.sortDirection);
                            }
                        }*/
                    });
                }
            });

        }

       
    },
	
	setDefaultSort: function(filter){
		var sorting = filter.get('sorting');
		
		if (sorting === ''){
			sorting = [];
			sorting.push({
					type: 'status',
					value: Uni.component.sort.model.Sort.DESC
				});
			sorting.push({
					type: 'startedOn',
					value: Uni.component.sort.model.Sort.DESC
				});
			filter.set('sorting', sorting);			
		}
	},
	
    setFilterTimeInterval: function (field, value, resource, defaultValue) {
        var me = this, name;

        if (value) {
            name = Uni.I18n.translate('importService.history.' + resource, 'FIM', defaultValue);
            me.getFilterTopPanel().setFilter(field, name, Uni.DateTime.formatDateLong(new Date(value)));
        }
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('fim-history-preview'),
            previewForm = page.down('fim-history-preview-form')


        Ext.suspendLayouts();

        preview.setTitle(record.get('importService'));
        previewForm.loadRecord(record);
        preview.down('fim-history-action-menu').record = record;
        Ext.resumeLayouts();
    },

    onFilterChange: function (combo) {
        if (!_.isEmpty(combo.getRawValue())) {
            this.getFilterTopPanel().setFilter(combo.getName(), combo.getFieldLabel(), combo.getRawValue());
        }
    },

    applyFilter: function () {
        this.getSideFilterForm().updateRecord();
        this.getSideFilterForm().getRecord().save();
    },

    clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        record.set(key, null);
        record.save();
    },

    chooseSort: function (menu, item) {
        var page = this.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer(),
            name = item.name,
            router = this.getController('Uni.controller.history.Router'),
            filter = router.filter,
            sorting = filter.get('sorting');

       
        if (Ext.isArray(sorting)) {
			sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.type === name
            });
			
			if (sortingItem) {
				return;
			}
			else {			
				sorting.push({
					type: name,
					value: Uni.component.sort.model.Sort.DESC
				});
			}
        } 
		else {
            sorting = [
                {
                    type: name,
                    value: Uni.component.sort.model.Sort.DESC
                }
            ];
        }
        filter.set('sorting', sorting);
        filter.save();
    },

    clearSort: function (btn) {
	
        var router = this.getController('Uni.controller.history.Router'),
            filter = router.filter,
            sorting = filter.get('sorting');

		sorting = [];
        filter.set('sorting', sorting);
        filter.save();
    },

    sortCloseclick: function (btn) {
	
        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting');

        if (Ext.isArray(sorting)) {
            Ext.Array.remove(sorting, Ext.Array.findBy(sorting, function (item) {
                return item.type === btn.sortType
            }));
        }
        filter.set('sorting', sorting);
        filter.save();
    },

    switchSort: function (btn) {
	
        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting'),
            sortingItem;

        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.type === btn.sortType
            });
            if (sortingItem) {
                if (sortingItem.value === Uni.component.sort.model.Sort.ASC) {
                    sortingItem.value = Uni.component.sort.model.Sort.DESC;
                } else {
                    sortingItem.value = Uni.component.sort.model.Sort.ASC;
                }
            }
        }
        filter.set('sorting', sorting);
        filter.save();
      
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;
			
		
		router.arguments.importServiceId = menu.record.get('importServiceId');
		router.arguments.occurrenceId = menu.record.get('historyId');
		if (item.action === 'viewLog'){
			route = 'administration/importservices/importservice/history/occurrence';			
		}
		
        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    }

});