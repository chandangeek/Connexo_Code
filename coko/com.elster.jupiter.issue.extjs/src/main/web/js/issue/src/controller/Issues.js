Ext.define('Isu.controller.Issues', {
    extend: 'Ext.app.Controller',

    requires: [
        'Isu.model.ExtraParams',
        'Uni.controller.Navigation',
        'Uni.util.QueryString'
    ],

    models: [
        'Isu.model.Issues',
        'Isu.model.IssueFilter'
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssuesGroups',
        'Isu.store.Assignee',
        'Isu.store.IssueStatus',
        'Isu.store.IssueReason',
        'Isu.store.IssueMeter',
        'Isu.store.UserGroupList',
        'Isu.store.IssueGrouping'
    ],

    views: [
        'workspace.issues.Setup'
    ],

    mixins: [
        'Isu.util.IsuGrid',
        'Isu.util.IsuComboTooltip'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'issues-setup #issues-preview'
        },
        {
            ref: 'actionMenu',
            selector: '#issue-action-menu'
        },
        {
            ref: 'filterForm',
            selector: 'issues-setup #issues-side-filter #filter-form'
        },
        {
            ref: 'filteringToolbar',
            selector: 'issues-setup #filtering-toolbar'
        },
        {
            ref: 'sortingToolbar',
            selector: 'issues-setup #sorting-toolbar'
        },
        {
            ref: 'groupingToolbar',
            selector: 'issues-setup #grouping-toolbar'
        },
        {
            ref: 'groupPanel',
            selector: 'issues-setup #issue-group'
        }
    ],

    init: function () {
        this.control({
            'issues-setup #issues-grid': {
                select: this.showPreview
            },
            'issues-setup #issues-preview #filter-display-button': {
                click: this.applyFilterBy
            },
            '#issue-action-menu': {
                click: this.chooseAction
            },
            'issues-setup #issues-grid gridview': {
                refresh: this.setAssigneeTypeIconTooltip
            },
            'issues-setup #sorting-toolbar container sort-item-btn': {
                click: this.changeSortDirection,
                closeclick: this.removeSortItem
            },
            'issues-setup #sorting-toolbar issue-sort-menu': {
                click: this.addSortParam
            },
            'issues-setup #sorting-toolbar button[action=clear]': {
                click: this.clearSortParams
            },
            'issues-setup #grouping-toolbar [name=groupingcombo]': {
                change: this.changeGrouping
            },
            'issues-setup #filtering-toolbar tag-button': {
                closeclick: this.removeFilterItem
            },
            'issues-setup #filtering-toolbar button[action="clear"]': {
                click: this.resetFilter
            },
            'issues-side-filter button[action="reset"]': {
                click: this.resetFilter
            },
            'issues-side-filter button[action="filter"]': {
                click: this.applyFilter
            },
            'issues-side-filter filter-form combobox[name=reason]': {
                render: this.setComboTooltip
            },
            'issues-side-filter filter-form combobox[name=meter]': {
                render: this.setComboTooltip,
                expand: this.limitNotification
            }
        });
    },

    showDataCollection: function () {
        this.showOverview('datacollection');
    },

    showDataValidation: function () {
        this.showOverview('datavalidation');
    },

    showOverview: function (issueType) {
        var me = this,
            issuesStore = me.getStore('Isu.store.Issues'),
            issuesStoreProxy = issuesStore.getProxy(),
            groupStore = me.getStore('Isu.store.IssuesGroups'),
            filter,
            sort;

        me.extraParamsModel = new Isu.model.ExtraParams();

        me.extraParamsModel.setValuesFromQueryString(function (extraParamsModel, data) {
            issuesStoreProxy.extraParams = data || {};
            issuesStoreProxy.setExtraParam('issueType', issueType);
            me.setParamsForIssueGroups(extraParamsModel.get('filter'), extraParamsModel.get('group').get('value'));

            me.getApplication().fireEvent('changecontentevent', Ext.widget('issues-setup', {
                router: me.getController('Uni.controller.history.Router'),
                issueType: issueType
            }));
            me.getFilteringToolbar().addFilterButtons(extraParamsModel.get('filter'));
            me.getSortingToolbar().addSortButtons(extraParamsModel.get('sort'));
            me.setFilterForm();

            groupStore.on('load', function () {
                me.setGrouping();
            });
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            id = record.getId(),
            preview = this.getPreview(),
            form = preview.down('#issues-preview-form'),
            router = this.getController('Uni.controller.history.Router'),
            detailsLink = router.getRoute(router.currentRoute + '/view').buildUrl({id: id});

        preview.setLoading(true);

        this.getModel('Isu.model.Issues').load(id, {
            success: function (record) {
                if (!form.isDestroyed) {
                    form.loadRecord(record);
                    preview.down('#issue-action-menu').record = record;
                    preview.down('#view-details-link').setHref(detailsLink);
                    preview.setTitle(record.get('title'));
                }
            },
            callback: function () {
                preview.setLoading(false);
            }
        });
    },

    chooseAction: function (menu, item) {
        var router = this.getController('Uni.controller.history.Router');

        router.getRoute(router.currentRoute + '/' + item.action).forward({id: menu.record.getId()});
    },

    setFilterForm: function () {
        var me = this,
            filterModel = this.extraParamsModel.get('filter'),
            form = me.getFilterForm(),
            filterCheckboxGroup = form.down('filter-checkboxgroup');

        if (filterModel.get('assignee')) {
            form.down('combobox[name=assignee]').getStore().add(filterModel.getAssignee());
        }

        if (filterCheckboxGroup.getStore().getCount()) {
            form.loadRecord(filterModel);
        } else {
            filterCheckboxGroup.on('itemsadded', function () {
                form.loadRecord(filterModel);
            }, me, {single: true});
        }
    },

    removeFilterItem: function (button) {
        var filterForm = this.getFilterForm();

        if (!button.targetId) {
            filterForm.down('[name=' + button.target + ']').reset();
        } else {
            filterForm.down('[name=' + button.target + '] checkboxfield[inputValue=' + button.targetId + ']').setValue(false);
        }

        this.applyFilter();
    },

    addSortParam: function (menu, item) {
        var sorting = this.extraParamsModel.get('sort');

        if (!sorting.data[item.action]) {
            sorting.addSortParam(item.action);
            this.refresh();
        }
    },

    changeSortDirection: function (button) {
        var sorting = this.extraParamsModel.get('sort');

        sorting.toggleSortParam(button.sortName);
        this.refresh();
    },

    removeSortItem: function (button) {
        var sorting = this.extraParamsModel.get('sort');

        sorting.removeSortParam(button.sortName);
        this.refresh();
    },

    clearSortParams: function () {
        var sorting = this.extraParamsModel.get('sort');

        sorting.data = {};
        this.refresh();
    },

    refresh: function () {
        window.location.assign(this.extraParamsModel.getQueryStringFromValues());
    },

    setGrouping: function () {
        var grouping = this.extraParamsModel.get('group').get('value'),
            groupStore = this.getStore('Isu.store.IssuesGroups'),
            groupingCombo = this.getGroupingToolbar().down('[name=groupingcombo]'),
            groupPanel = this.getGroupPanel(),
            groupingGrid = groupPanel.down('issue-group-grid'),
            groupingInfo = groupPanel.down('[name=issue-group-info]'),
            selectionModel = groupingGrid.getSelectionModel(),
            groupingField;

        groupingCombo.setValue(grouping);

        if (grouping == 'none' || !groupStore.getCount()) {
            groupPanel.hide();
        } else {
            if (this.extraParamsModel.get('filter').get(grouping)) {
                groupingField = groupStore.getById(this.extraParamsModel.get('filter').get(grouping).getId());
            } else {
                groupingField = groupStore.getById(this.extraParamsModel.get('groupValue'));
                if (!groupingField && this.extraParamsModel.get('group') != 'none') {
                    groupingField = groupStore.getAt(0);
                }
            }
            groupingGrid.on('itemclick', this.changeGroup, this);
            if (groupingField) {
                selectionModel.select(groupingField);
                groupingInfo.update('<h3>Issues for reason: ' + groupingField.get('reason') + '</h3>');
                groupingInfo.show();
                groupingGrid.fireEvent('itemclick', groupingGrid, groupingField);
            } else {
                this.getIssuesList().hide();
                this.getItemPanel().hide();
            }
            groupPanel.show();
        }
    },

    changeGroup: function (grid, record) {
        this.extraParamsModel.set('groupValue', record.getId());
        this.refresh();
    },

    changeGrouping: function (combo, newValue) {
        var store = combo.getStore(),
            previousGroup = this.extraParamsModel.get('group');

        if (newValue !== 'reason') {
            this.extraParamsModel.set('groupValue', null);
        }
        if (previousGroup != store.getById(newValue)) {
            this.extraParamsModel.set('group', store.getById(newValue));
            this.refresh();
        }
    },

    setParamsForIssueGroups: function (filterModel, field) {
        var groupStore = this.getStore('Isu.store.IssuesGroups'),
            groupStoreProxy = groupStore.getProxy(),
            status = filterModel.statusStore,
            statusValues = [],
            reason = filterModel.getReason(),
            assignee = filterModel.getAssignee(),
            meter = filterModel.getMeter();

        if (field != 'none') {
            groupStoreProxy.setExtraParam('field', field);
        } else {
            groupStoreProxy.setExtraParam('field', 'reason');
        }

        if (status) {
            status.each(function (item) {
                statusValues.push(item.get('id'));
            });
            groupStoreProxy.setExtraParam('status', statusValues);
        } else {
            groupStoreProxy.setExtraParam('status', []);
        }
        if (assignee) {
            groupStoreProxy.setExtraParam('assigneeId', assignee.get('id'));
            groupStoreProxy.setExtraParam('assigneeType', assignee.get('type'));
        } else {
            groupStoreProxy.setExtraParam('assigneeId', []);
            groupStoreProxy.setExtraParam('assigneeType', []);
        }
        if (reason) {
            groupStoreProxy.setExtraParam('id', reason.get('id'));
        } else {
            groupStoreProxy.setExtraParam('id', []);
        }
        if (meter) {
            groupStoreProxy.setExtraParam('meter', meter.get('id'));
        } else {
            groupStoreProxy.setExtraParam('meter', []);
        }
    },

    setGroupFields: function (view) {
        var model = Ext.ModelManager.getModel('Isu.model.Issues'),
            reason = model.getFields()[1];

        view.store = Ext.create('Ext.data.Store', {
            fields: ['value', 'display'],
            data: [
                { value: 0, display: 'None'},
                { value: 'reason', display: reason.displayValue }
            ]
        });
    },

    applyFilter: function () {
        var form = this.getFilterForm(),
            filter = form.getRecord();

        form.updateRecord(filter);
        this.extraParamsModel.set('filter', filter);
        this.refresh();
    },

    resetFilter: function () {
        this.extraParamsModel.set('filter', Ext.create('Isu.model.IssueFilter'));
        this.refresh();
    },

    applyFilterBy: function (button) {
        switch (button.filterBy) {
            case 'status':
                this.setCheckboxFilter(button.filterBy, button.filterValue.id);
                break;
            case 'assignee':
                if (button.filterValue) {
                    this.setComboFilter(button.filterBy, button.filterValue.id + ':' + button.filterValue.type, button.filterValue.name);
                } else {
                    this.setComboFilter(button.filterBy, '-1:UnexistingType');
                }
                break;
            case 'reason':
                this.setComboFilter(button.filterBy, parseInt(button.filterValue.id), button.filterValue.name);
                break;
            case 'device':
                this.setComboFilter('meter', parseInt(button.filterValue.id), button.filterValue.serialNumber);
                break;
        }
    },

    setCheckboxFilter: function (filterType, filterValue) {
        var checkboxGroup = this.getFilterForm().down('[name=' + filterType + ']');

        checkboxGroup.reset();
        checkboxGroup.down('checkboxfield[inputValue=' + filterValue + ']').setValue(true);
        this.applyFilter();
    },

    setComboFilter: function (filterType, filterValue, visualValue) {
        var me = this,
            filterForm = this.getFilterForm(),
            combo = filterForm.down('[name=' + filterType + ']'),
            comboStore = combo.getStore(),
            storeProxy = comboStore.getProxy();

        storeProxy.setExtraParam(combo.queryParam, visualValue);
        comboStore.load(function () {
            combo.setValue(filterValue);
            me.applyFilter();
        });
    },

    assignedToMe: function () {
        var me = this,
            assignCombo = me.getFilterForm().down('combobox[name=assignee]'),
            assignStore = assignCombo.getStore(),
            currentUser;

        assignStore.load({ params: {me: true}, callback: function () {
            currentUser = assignStore.getAt(0);
            assignCombo.setValue(currentUser.get('idx'));
            me.applyFilter();
        }});
    }
});