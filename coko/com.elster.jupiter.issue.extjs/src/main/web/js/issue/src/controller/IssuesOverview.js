Ext.define('Isu.controller.IssuesOverview', {
    extend: 'Ext.app.Controller',

    mixins: [
        'Isu.util.IsuComboTooltip'
    ],

    models: [
        'Isu.model.IssuesFilter',
        'Isu.model.IssueAssignee',
        'Isu.model.IssueWorkgroupAssignee',
        'Isu.model.IssueReason',
        'Isu.model.Device',
        'Uni.component.sort.model.Sort'
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssueActions',
        'Isu.store.IssueStatuses',
        'Isu.store.IssueAssignees',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.store.IssueReasons',
        'Isu.store.Devices',
        'Isu.store.IssueGrouping',
        'Isu.store.Groups',
        'Isu.store.Clipboard'
    ],

    views: [
        'Isu.view.issues.Overview'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'issues-overview #issues-preview'
        },
        {
            ref: 'filterToolbar',
            selector: 'issues-overview isu-view-issues-issuefilter'
        },
        {
            ref: 'groupingToolbar',
            selector: 'issues-overview #issues-grouping-toolbar'
        },
        {
            ref: 'groupGrid',
            selector: 'issues-overview #issues-group-grid'
        },
        {
            ref: 'previewContainer',
            selector: 'issues-overview #issues-preview-container'
        },
        {
            ref: 'groupingTitle',
            selector: 'issues-overview issues-grouping-title'
        },
        {
            ref: 'issuesGrid',
            selector: 'issues-overview #issues-grid'
        },
        {
            ref: 'previewActionMenu',
            selector: '#issues-preview issues-action-menu'
        }
    ],

    init: function () {
        var me = this;
        this.control({
            'issues-overview #issues-overview-action-menu': {
                click: this.chooseAction
            },
            'issues-overview #issues-grid uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'issues-overview #issues-grid': {
                select: this.showPreview
            },
            'issues-overview issues-grouping-toolbar #issues-grouping-toolbar-combo': {
                change: this.setGroupingType
            },
            'issues-overview issues-group-grid': {
                select: this.setGroupingValue
            },
            'issues-overview isu-view-issues-issuefilter': {
                change: this.setGrouping
            },
            'issues-overview #issues-preview #filter-display-button': {
                click: this.setFilterItem
            }
        });
    },

    showOverview: function () {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (queryString.myopenissues) {
            me.getStore('Isu.store.IssueAssignees').load({
                params: {me: true},
                callback: function (records) {
                    queryString.myopenissues = undefined;
                    queryString.userAssignee = records[0].getId();
                    queryString.groupingType = 'none';
                    queryString.sort = ['-priorityTotal'];
                    window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                }
            });
        } else if (queryString.myworkgroupissues) {
            Ext.Ajax.request({
                url: '/api/isu/workgroups?myworkgroups=true',
                method: 'GET',
                success: function (response) {
                    var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                    if (decoded && decoded.workgroups) {
                        queryString.myworkgroupissues = undefined;
                        queryString.userAssignee = [-1];
                        queryString.workGroupAssignee = decoded.workgroups.length == 0 ? [-1] : decoded.workgroups.map(function (wg) {
                            return wg.id;
                        });
                        queryString.groupingType = 'none';
                        queryString.sort = ['-priorityTotal'];
                        window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                    }
                }
            });
        } else if (!queryString.groupingType) {
            queryString.status = ['status.open', 'status.in.progress'];
            queryString.groupingType = 'none';
            queryString.sort = ['-priorityTotal'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else {
            me.getStore('Isu.store.Clipboard').set('latest-issues-filter', queryString);
            me.getApplication().fireEvent('changecontentevent', Ext.widget('issues-overview', {
                router: me.getController('Uni.controller.history.Router'),
                groupingType: queryString.groupingType
            }));

            if (me.getGroupGrid()) {
                me.setGrouping(true);
            }
        }
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            previewActionMenu = me.getPreviewActionMenu();
        Ext.getStore('Isu.store.Clipboard').set('issue', record);
        Ext.getStore('Isu.store.Clipboard').set('latest-issues-filter', Uni.util.QueryString.getQueryStringValues(false));
        Ext.suspendLayouts();
        preview.loadRecord(record);

        if (previewActionMenu) {
            previewActionMenu.record = record;
        }
        preview.setTitle(Ext.String.htmlEncode(record.get('title')));
        Ext.resumeLayouts(true);
    },

    chooseAction: function (menu, menuItem) {
        if (!Ext.isEmpty(menuItem.actionRecord)) {
            this.applyActionImmediately(menu.record, menuItem.actionRecord);
        }
    },

    applyActionImmediately: function (issue, action) {
        var me = this,
            actionModel = Ext.create(issue.actions().model);

        actionModel.setId(action.getId());
        actionModel.set('parameters', {});
        actionModel.getProxy().url = issue.getProxy().url + '/' + issue.getId() + '/actions';
        actionModel.save({
            callback: function (model, operation, success) {
                var response = Ext.decode(operation.response.responseText, true);

                if (response) {
                    if (response.data.actions[0].success) {
                        me.getApplication().fireEvent('acknowledge', response.data.actions[0].message);
                        me.getIssuesGrid().getStore().load();
                    } else {
                        me.getApplication().getController('Uni.controller.Error').showError(model.get('name'), responseText.data.actions[0].message);
                    }
                }
            }
        });
    },

    setFilterItem: function (button) {
        var me = this,
            filterToolbar = me.getFilterToolbar();

        switch (button.filterBy) {
            case 'userAssignee':
                if (button.filterValue && button.filterValue.id) {
                    filterToolbar.down('[dataIndex=' + button.filterBy + ']').setFilterValue([button.filterValue.id]);
                } else {
                    filterToolbar.down('[dataIndex=' + button.filterBy + ']').setFilterValue([-1]);
                }
                break;
            case 'workGroupAssignee':
                if (button.filterValue && button.filterValue.id) {
                    filterToolbar.down('[dataIndex=' + button.filterBy + ']').setFilterValue([button.filterValue.id]);
                } else {
                    filterToolbar.down('[dataIndex=' + button.filterBy + ']').setFilterValue([-1]);
                }
                break;
            case 'device':
                filterToolbar.down('[dataIndex=meter]').setFilterValue(button.filterValue.name);
                break;
            case 'issueType':
                filterToolbar.down('[dataIndex=issueType]').setFilterValue(button.filterValue.uid);
                break;
            default:
                filterToolbar.down('[dataIndex=' + button.filterBy + ']').setFilterValue([button.filterValue.id]);
        }
        filterToolbar.applyFilters();
    },

    setGroupingType: function (combo, newValue) {
        var me = this,
            groupGrid = me.getGroupGrid(),
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        queryString.groupingType = newValue;

        if (newValue !== 'none') {
            groupGrid.show();
            groupGrid.getStore().load({
                params: me.getGroupProxyParams(newValue),
                callback: function (records) {
                    if (!Ext.isEmpty(records)) {
                        queryString.groupingValue = records.length ? records[0].getId() : undefined;
                        me.applyGrouping(queryString);
                    } else {
                        groupGrid.hide();
                        queryString.groupingValue = undefined;
                        me.applyGrouping(queryString);
                    }
                }
            });
        } else {
            queryString.groupingValue = undefined;
            me.applyGrouping(queryString);
        }
    },

    setGroupingValue: function (selectionModel, record) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        queryString.groupingValue = record ? record.getId() : undefined;

        me.applyGrouping(queryString, true);
    },

    getGroupProxyParams: function (groupingType) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            filterToolbar = me.getFilterToolbar(),
            filter = filterToolbar.getFilterParams(false, !filterToolbar.filterObjectEnabled),
            params = {
                filter: [
                    {
                        property: 'field',
                        value: groupingType || queryString.groupingType
                    }
                ]
            };

        Ext.iterate(filter, function (key, value) {
            if (!Ext.isEmpty(value)) {
                params.filter.push({
                    property: key,
                    value: value
                });
            }
        });

        params.filter = Ext.encode(params.filter);

        return params;
    },

    setGrouping: function (doLoad) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            groupGrid = me.getGroupGrid(),
            groupingTitle = me.getGroupingTitle(),
            groupStore = groupGrid.getStore(),
            afterLoad = function () {
                if (!Ext.isEmpty(groupGrid.getStore().getRange())) {
                    if ( Ext.isEmpty(queryString.groupingValue) && !Ext.isEmpty(queryString[queryString.groupingType]) ) {
                        queryString.groupingValue = queryString[queryString.groupingType];
                    }
                    var groupingRecord = groupStore.getById(queryString.groupingValue);
                    if ( Ext.isEmpty(groupingRecord) && !Ext.isEmpty(queryString[queryString.groupingType]) ) {
                        queryString.groupingValue = queryString[queryString.groupingType];
                        groupingRecord = groupStore.getById(queryString.groupingValue);
                    }
                    if (Ext.isEmpty(groupingRecord)) {
                        groupGrid.getView().getSelectionModel().select(0);
                    }

                    Ext.suspendLayouts();
                    if (queryString.groupingValue && groupingRecord) {
                        groupGrid.getSelectionModel().select(groupingRecord);
                        groupingTitle.setTitle(Uni.I18n.translate('general.issuesFor', 'ISU', 'Issues for {0}: {1}', [queryString.groupingType, groupingRecord.get('description')]));
                        groupingTitle.show();
                    } else {
                        groupingTitle.hide();
                    }
                    Ext.resumeLayouts(true);
                } else {
                    groupGrid.hide();
                    groupingTitle.hide();
                }
            };
        me.getIssuesGrid().down('#pagingtoolbartop').isFullTotalCount = false;
        Ext.suspendLayouts();
        if (queryString.groupingType !== 'none') {
            groupGrid.updateGroupingType(queryString.groupingType);
            groupGrid.show();
            groupGrid.down('pagingtoolbarbottom').params = me.getGroupProxyParams();
            if (doLoad) {
                groupStore.load({
                    params: me.getGroupProxyParams(),
                    callback: afterLoad
                });
            } else {
                afterLoad();
            }
        } else {
            groupGrid.hide();
            groupingTitle.hide();
        }
        Ext.resumeLayouts(true);
    },

    applyGrouping: function (queryString, doLoad) {
        var me = this,
            issuesGrid = me.getIssuesGrid(),
            href;

        if (!Ext.isEmpty(queryString.start)) {
            queryString.start = 0;
        }
        href = Uni.util.QueryString.buildHrefWithQueryString(queryString, false);
        if (window.location.href !== href) {
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            window.location.href = href;
            Ext.util.History.currentToken = window.location.hash.substr(1);
            me.setGrouping(!!doLoad);
            me.resetPagingToolbars(issuesGrid);
            issuesGrid.getStore().loadPage(1);
        }
    },

    resetPagingToolbars: function (issuesGrid) {
        var pagingToolbarTop = issuesGrid.down('pagingtoolbartop'),
            pagingToolbarBottom = issuesGrid.down('pagingtoolbarbottom');

        if (!Ext.isEmpty(pagingToolbarTop)) {
            pagingToolbarTop.resetPaging();
        }

        if (!Ext.isEmpty(pagingToolbarBottom)) {
            pagingToolbarBottom.totalCount = 0;
            pagingToolbarBottom.totalPages = 0;
            pagingToolbarBottom.isFullTotalCount = false;
            pagingToolbarBottom.store.currentPage = 1;
            pagingToolbarBottom.initPageNavItems(pagingToolbarBottom.child('#pageNavItem'), 1, pagingToolbarBottom.totalPages);
        }
    }
});