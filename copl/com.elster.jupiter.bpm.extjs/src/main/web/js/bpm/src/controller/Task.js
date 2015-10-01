Ext.define('Bpm.controller.Task', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.privileges.BpmManagement',
        'Bpm.controller.FilterSortTasks'
    ],
    views: [
        'Bpm.view.task.Tasks'
    ],
    stores: [
		'Bpm.store.task.Tasks',
        'Bpm.store.task.TasksFilterDueDates',
        'Bpm.store.task.TasksFilterProcesses',
        'Bpm.store.task.TasksFilterStatuses',
        'Bpm.store.task.TasksFilterUsers'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'bpm-tasks'
        },
        {
            ref: 'mainGrid',
            selector: 'bpm-tasks bpm-tasks-grid'
        }
    ],

    init: function () {
        this.control({
            'bpm-tasks bpm-tasks-grid': {
                select: this.showPreview
            }/*,
            'bpm-tasks-sort-menu': {
                click: this.chooseSort
            },
            '#bpm-task-sort-toolbar button[action=clear]': {
                click: this.clearAllSorting
            },
            '#bpm-task-sort-toolbar button': {
                closeclick:
                 this.onSortCloseClicked
            },
            '#bpm-task-sort-toolbar #itemsContainer button': {
                click: this.switchSortingOrder
            }*/
            /*,
            'bpm-task-action-menu': {
                click: this.chooseAction
            }*/
        });
        this.application.getController('Bpm.controller.FilterSortTasks');
    },

    showTasks: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            filterSortContoller = this.application.getController('Bpm.controller.FilterSortTasks'),
            view;

        if (queryString.myopentask) {

        }
        filterSortContoller.setDefaultSort();
        view = Ext.widget('bpm-tasks', {
            router: router
        });

        me.getApplication().fireEvent('changecontentevent', view);
        filterSortContoller.updateSortingToolbar();
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('bpm-task-preview'),
            previewForm = page.down('bpm-task-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
     //   preview.down('bpm-task-action-menu').record = record;
        Ext.resumeLayouts();
    }/*,

    updateSortingToolbar: function () {
        var me = this,
            page = me.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer(),
            store = me.getStore('Bpm.store.task.Tasks');

        sortContainer.removeAll();
        sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {

                if (sortItem.direction) {
                    menuItem = me.getPage().down('#menu-tasks-sort [name=' + sortItem.property + ']');
                    cls = sortItem.direction === Uni.component.sort.model.Sort.ASC
                        ? 'x-btn-sort-item-asc'
                        : 'x-btn-sort-item-desc';

                    sortContainer.add({
                        xtype: 'sort-item-btn',
                        itemId: 'sort-by-' + sortItem.property + '-button',
                        text: menuItem.text,
                        sortType: sortItem.property,
                        sortDirection: sortItem.direction,
                        iconCls: cls
                    });
                }
            });
        }
    },

    setDefaultSort: function () {
        var me = this,
            store = me.getStore('Bpm.store.task.Tasks'),
            sorting = store.getProxy().extraParams['sort'];

        if (sorting === undefined) { // set default filters
            sorting = [];
            sorting.push({
                property: 'dueDate',
                direction: Uni.component.sort.model.Sort.DESC
            });
            sorting.push({
                property: 'creationDate',
                direction: Uni.component.sort.model.Sort.DESC
            });
            store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        }
    },

    chooseSort: function (menu, item) {
        var me = this,
            name = item.name,
            store = me.getStore('Bpm.store.task.Tasks'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.property === name
            });

            if (sortingItem) {
                return;
            } else {
                sorting.push({
                    property: name,
                    direction: Uni.component.sort.model.Sort.DESC
                });
            }
        } else {
            sorting = [
                {
                    property: name,
                    direction: Uni.component.sort.model.Sort.DESC
                }
            ];
        }
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    },

    clearAllSorting: function (btn) {
        var me = this,
            store = me.getStore('Bpm.store.task.Tasks');

        sorting = [];
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    },

    onSortCloseClicked: function (btn) {
        var me = this,
            store = me.getStore('Bpm.store.task.Tasks'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        if (Ext.isArray(sorting)) {
            Ext.Array.remove(sorting, Ext.Array.findBy(sorting, function (item) {
                return item.property === btn.sortType
            }));
        }
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    },

    switchSortingOrder: function (btn) {
        var me = this,
            store = me.getStore('Bpm.store.task.Tasks'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']),
            sortingItem;

        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.property === btn.sortType
            });
            if (sortingItem) {
                if (sortingItem.direction === Uni.component.sort.model.Sort.ASC) {
                    sortingItem.direction = Uni.component.sort.model.Sort.DESC;
                } else {
                    sortingItem.direction = Uni.component.sort.model.Sort.ASC;
                }
            }
        }
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    },

    updateSortingToolbarAndResults: function() {
        var me = this,
            gridView = me.getMainGrid(),
            store = me.getStore('Bpm.store.task.Tasks');

        me.updateSortingToolbar();
        gridView.setLoading();
        store.load(function(records, operation, success) {
            gridView.setLoading(false);
        });
    }
    */
    /*,

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        router.arguments.importServiceId = menu.record.get('importServiceId');
        router.arguments.occurrenceId = menu.record.get('occurrenceId');
        if (item.action === 'viewLog') {
            route = 'administration/importservices/importservice/history/occurrence';
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    }*/
});