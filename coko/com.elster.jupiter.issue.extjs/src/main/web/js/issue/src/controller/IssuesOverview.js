Ext.define('Isu.controller.IssuesOverview', {
    extend: 'Ext.app.Controller',

    mixins: [
        'Isu.util.IsuComboTooltip'
    ],

    showOverview: function (issueType, widgetXtype) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            grouping = router.filter.get('grouping');

        if (router.queryParams.myopenissues) {
            delete router.queryParams.myopenissues;
            me.getStore('Isu.store.IssueAssignees').load({params: {me: true}, callback: function (records) {
                router.filter.set('assignee', records[0].getId());
                router.filter.set('status', 'status.open');
                router.filter.set('sorting', [
                    {type: 'dueDate', value: Uni.component.sort.model.Sort.ASC}
                ]);
                router.filter.save();
            }});
        } else if (!router.queryParams.filter) {
            router.filter.set('status', 'status.open');
            router.filter.set('sorting', [
                {type: 'dueDate', value: Uni.component.sort.model.Sort.ASC}
            ]);
            router.filter.save();
        } else {
            me.getStore('Isu.store.IssueStatuses').getProxy().setExtraParam('issueType', issueType);
            me.getStore('Isu.store.IssueReasons').getProxy().setExtraParam('issueType', issueType);

            me.getApplication().fireEvent('changecontentevent', Ext.widget(widgetXtype, {
                router: router,
                groupingType: grouping && grouping.type ? grouping.type : 'none'
            }));

            me.setFilter();
        }
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview();

        preview.setLoading(true);

        this.getModel(record.$className).load(record.getId(), {
            success: function (record) {
                if (!preview.isDestroyed) {
                    preview.loadRecord(record);
                    preview.down('issues-action-menu').record = record;
                    preview.down('#issue-view-details-link').setHref(preview.router.getRoute(preview.router.currentRoute + '/view').buildUrl({issueId: record.getId()}));
                    preview.setTitle(record.get('title'));
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

    setFilter: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filterForm = me.getFilterForm();

        filterForm.setLoading(true);
        me.loadFilterFormDependencies(router.filter, function () {
            filterForm.loadRecord(router.filter);
            filterForm.setLoading(false);
            me.setFilterToolbar(filterForm);
        });
        me.setGrouping(router.filter);
        me.setSortingToolbar(router.filter);
    },

    setFilterToolbar: function (filterForm) {
        var filterToolbar = this.getFilterToolbar(),
            statusesGroupField = filterForm.down('[name=status]'),
            statusFieldLabel = statusesGroupField.getFieldLabel();

        Ext.Array.each(statusesGroupField.query('checkbox'), function (checkbox) {
            if (checkbox.getValue()) {
                filterToolbar.setFilter(checkbox.getName() + '|' + checkbox.inputValue, statusFieldLabel, checkbox.boxLabel);
            }
        });

        Ext.Array.each(filterForm.query('combobox'), function (combo) {
            var value = combo.getRawValue();

            if (!_.isEmpty(value)) {
                filterToolbar.setFilter(combo.getName(), combo.getFieldLabel(), value);
            }
        });
    },

    loadFilterFormDependencies: function (filterModel, callback) {
        var me = this,
            assigneesStore = this.getStore('Isu.store.IssueAssignees'),
            reasonsStore = this.getStore('Isu.store.IssueReasons'),
            metersStore = this.getStore('Isu.store.Devices'),
            assignee = filterModel.get('assignee'),
            reason = filterModel.get('reason'),
            meter = filterModel.get('meter'),
            dependenciesCount = 1,
            checkDependenciesLoading = function () {
                dependenciesCount--;
                if (dependenciesCount === 0) {
                    callback();
                }
            };

        if (!Ext.isEmpty(assignee)) {
            dependenciesCount++;
        }
        if (!Ext.isEmpty(reason)) {
            dependenciesCount++;
        }
        if (!Ext.isEmpty(meter)) {
            dependenciesCount++;
        }

        this.getStore('Isu.store.IssueStatuses').load(checkDependenciesLoading);
        if (!Ext.isEmpty(assignee)) {
            if (assigneesStore.getById(assignee)) {
                checkDependenciesLoading();
            } else {
                this.getModel('Isu.model.IssueAssignee').load(assignee, {
                    callback: checkDependenciesLoading,
                    success: function (record) {
                        assigneesStore.add(record);
                    }
                });
            }
        }
        if (!Ext.isEmpty(reason)) {
            if (reasonsStore.getById(reason)) {
                checkDependenciesLoading();
            } else {
                this.getModel('Isu.model.IssueReason').load(reason, {
                    callback: checkDependenciesLoading,
                    success: function (record) {
                        reasonsStore.add(record);
                    }
                });
            }
        }
        if (!Ext.isEmpty(meter)) {
            if (metersStore.getById(meter)) {
                checkDependenciesLoading();
            } else {
                this.getModel('Isu.model.Device').load(meter, {
                    callback: checkDependenciesLoading,
                    success: function (record) {
                        metersStore.add(record);
                    }
                });
            }
        }
    },

    applyFilter: function () {
        var me = this,
            filterForm = me.getFilterForm();

        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

    resetFilter: function () {
        this.getController('Uni.controller.history.Router').filter.getProxy().destroy();
    },

    setFilterItem: function (button) {
        var me = this,
            filterModel = me.getController('Uni.controller.history.Router').filter;

        switch (button.filterBy) {
            case 'status':
                filterModel.set(button.filterBy, [button.filterValue.id]);
                break;
            case 'assignee':
                if (button.filterValue) {
                    filterModel.set(button.filterBy, [button.filterValue.id, button.filterValue.type].join(':'));
                } else {
                    filterModel.set(button.filterBy, '-1:UnexistingType');
                }
                break;
            case 'device':
                filterModel.set('meter', button.filterValue.serialNumber);
                break;
            default:
                filterModel.set(button.filterBy, button.filterValue.id);
        }

        filterModel.save();
    },

    removeFilterItem: function (key) {
        var filter = this.getController('Uni.controller.history.Router').filter,
            statusRegExp = /status\|\w+/;

        if (key.indexOf('status|') === 0) {
            if (Ext.isArray(filter.get('status'))) {
                Ext.Array.remove(filter.get('status'), key.split('|')[1]);
            }
        } else {
            filter.set(key, null);
        }

        filter.save();
    },

    setGroupingType: function (combo, newValue) {
        var filter = this.getController('Uni.controller.history.Router').filter;

        filter.set('grouping', {
            type: newValue
        });
        filter.save();
    },

    setGroupingValue: function (selectionModel, record) {
        var filter = this.getController('Uni.controller.history.Router').filter;

        filter.get('grouping').value = record.getId();
        filter.save();
    },

    setGrouping: function (filter) {
        var me = this,
            grouping = filter.get('grouping'),
            groupGrid = me.getGroupGrid(),
            groupStore = groupGrid.getStore(),
            groupProxyParams = {issueType: 'datacollection'},
            previewContainer = me.getPreviewContainer();

        if (grouping && grouping.type !== 'none') {
            groupGrid.show();
            groupProxyParams.field = grouping.type;
            Ext.iterate(filter.getData(), function (key, value) {
                if (value) {
                    switch (key) {
                        case 'assignee':
                            groupProxyParams.assigneeId = value.split(':')[0];
                            groupProxyParams.assigneeType = value.split(':')[1];
                            break;
                        case 'grouping':
                            break;
                        case grouping.type:
                            groupProxyParams.id = value;
                            break;
                        default:
                            groupProxyParams[key] = value;
                    }
                }
            });
            groupStore.load({
                params: groupProxyParams,
                callback: function () {
                    var groupingTitle = me.getGroupingTitle(),
                        groupingRecord = groupStore.getById(grouping.value);

                    if (grouping.value && groupingRecord) {
                        groupGrid.getSelectionModel().select(groupingRecord);
                        groupingTitle.setTitle(Ext.String.format(groupingTitle.title, grouping.type, groupingRecord.get('reason')));
                        groupingTitle.show();
                    } else {
                        groupingTitle.hide();
                    }
                }
            });
            if (!grouping.value) {
                previewContainer.hide();
                me.getNoGroupSelectedPanel().show();
            }
        } else {
            groupGrid.hide();
        }
    },

    setSortingToolbar: function (filter) {
        var me = this;

        me.getSortingToolbar().addSortButtons(filter.get('sorting'));
    },

    addSortingItem: function (menu, menuItem) {
        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting');

        if (Ext.isArray(sorting)) {
            sorting.push({
                type: menuItem.action,
                value: Uni.component.sort.model.Sort.ASC
            });
        } else {
            sorting = [
                {
                    type: menuItem.action,
                    value: Uni.component.sort.model.Sort.ASC
                }
            ];
        }

        filter.save();
    },

    removeSortingItem: function (sortType) {
        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting');

        if (Ext.isArray(sorting)) {
            Ext.Array.remove(sorting, Ext.Array.findBy(sorting, function (item) {
                return item.type === sortType
            }));
        }

        filter.save();
    },

    changeSortDirection: function (sortType) {
        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting'),
            sortingItem;

        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.type === sortType
            });
            if (sortingItem) {
                if (sortingItem.value === Uni.component.sort.model.Sort.ASC) {
                    sortingItem.value = Uni.component.sort.model.Sort.DESC;
                } else {
                    sortingItem.value = Uni.component.sort.model.Sort.ASC;
                }
            }
        }

        filter.save();
    }
});