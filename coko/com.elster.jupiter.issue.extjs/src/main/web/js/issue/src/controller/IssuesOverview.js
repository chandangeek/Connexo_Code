Ext.define('Isu.controller.IssuesOverview', {
    extend: 'Ext.app.Controller',

    mixins: [
        'Isu.util.IsuComboTooltip'
    ],

    showOverview: function (issueType, widgetXtype, callback) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (queryString.myopenissues) {
            me.getStore('Isu.store.IssueAssignees').load({
                params: {me: true},
                callback: function (records) {
                    queryString.myopenissues = undefined;
                    queryString.assignee = records[0].getId();
                    queryString.status = 'status.open';
                    queryString.groupingType = 'none';
                    queryString.sort = ['dueDate', 'modTime'];
                    window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                }
            });
        } else if (!queryString.groupingType) {
            queryString.status = ['status.open', 'status.in.progress'];
            queryString.groupingType = 'none';
            queryString.sort = ['dueDate', 'modTime'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else {
            me.getStore('Isu.store.Clipboard').set(issueType + '-latest-issues-filter', queryString);
            me.getStore('Isu.store.IssueStatuses').getProxy().setExtraParam('issueType', issueType);
            me.getStore('Isu.store.IssueReasons').getProxy().setExtraParam('issueType', issueType);

            me.getApplication().fireEvent('changecontentevent', Ext.widget(widgetXtype, {
                router: me.getController('Uni.controller.history.Router'),
                groupingType: queryString.groupingType
            }));

            if (me.getGroupGrid()) {
                me.setGrouping(true);
            }
            callback ? callback() : null;
        }
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview();

        preview.setLoading(true);

        this.getModel(record.$className).load(record.getId(), {
            success: function (record) {
                if (!preview.isDestroyed) {
                    Ext.suspendLayouts();
                    preview.loadRecord(record);
                    preview.down('issues-action-menu').record = record;
                    preview.setTitle(record.get('title'));
                    Ext.resumeLayouts(true);
                }
            },
            callback: function () {
                if (!preview.isDestroyed) {
                    preview.setLoading(false);
                }
            }
        });
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
            case 'assignee':
                if (button.filterValue) {
                    filterToolbar.down('[dataIndex=' + button.filterBy + ']').setFilterValue([button.filterValue.id, button.filterValue.type].join(':'));
                } else {
                    filterToolbar.down('[dataIndex=' + button.filterBy + ']').setFilterValue('-1:UnexistingType');
                }
                break;
            case 'device':
                filterToolbar.down('[dataIndex=meter]').setFilterValue(button.filterValue.serialNumber);
                break;
            default:
                filterToolbar.down('[dataIndex=' + button.filterBy + ']').setFilterValue([button.filterValue.id]);
        }
        filterToolbar.applyFilters();
    },

    setGroupingType: function (combo, newValue) {
        var me = this,
            groupGrid = me.getGroupGrid(),
            groupEmptyPanel = me.getGroupEmptyPanel(),
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        queryString.groupingType = newValue;

        if (newValue !== 'none') {
            groupGrid.show();
            groupGrid.getStore().load({
                params: me.getGroupProxyParams(newValue),
                callback: function (records) {
                    if (!Ext.isEmpty(records)) {
                        groupEmptyPanel.hide();
                        queryString.groupingValue = records.length ? records[0].getId() : undefined;
                        me.applyGrouping(queryString);
                    } else {
                        groupGrid.hide();
                        groupEmptyPanel.show();
                    }
                }
            });
        } else {
            groupEmptyPanel.hide();
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
                    },
                    {
                        property: 'issueType',
                        value: me.getStore('Isu.store.IssueStatuses').getProxy().extraParams.issueType
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
            groupEmptyPanel = me.getGroupEmptyPanel(),
            groupingTitle = me.getGroupingTitle(),
            groupStore = groupGrid.getStore(),
            previewContainer = me.getPreviewContainer(),
            afterLoad = function () {
                if (!Ext.isEmpty(groupGrid.getStore().getRange())) {
                    groupEmptyPanel.hide();
                    var groupingRecord = groupStore.getById(queryString.groupingValue);

                    Ext.suspendLayouts();
                    if (queryString.groupingValue && groupingRecord) {
                        groupGrid.getSelectionModel().select(groupingRecord);
                        groupingTitle.setTitle(Uni.I18n.translate('general.issuesFor', 'ISU', 'Issues for {0}: {1}', [queryString.groupingType, groupingRecord.get('reason')]));
                        groupingTitle.show();
                    } else {
                        groupingTitle.hide();
                    }
                    Ext.resumeLayouts(true);
                } else {
                    groupGrid.hide();
                    groupingTitle.hide();
                    groupEmptyPanel.show();
                }
            };

        Ext.suspendLayouts();
        if (queryString.groupingType !== 'none') {
            groupGrid.updateGroupingType(queryString.groupingType);
            groupGrid.show();
            if (doLoad) {
                groupStore.load({
                    params: me.getGroupProxyParams(),
                    callback: afterLoad
                });
            } else {
                afterLoad();
            }

            previewContainer.setVisible(!!queryString.groupingValue);
            me.getNoGroupSelectedPanel().setVisible(!queryString.groupingValue);
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