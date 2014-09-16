/**
 * @class Uni.override.ProxyOverride
 */
Ext.define('Isu.override.ProxyOverride', {
    override: 'Ext.data.proxy.Server',

    buildUrl: function(request) {
        var url = this.callParent([request]);
        url = Ext.Loader.getBasePath() + url;

        return url;
    }

//    headers : {
//        Authorization : 'Basic ' + window.btoa([this.login, this.password].join(':'))
//    }
});

Ext.define('Isu.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace : {
            title: 'Workspace',
            route: 'workspace',
            disabled: true,
            items: {
                datacollection: {
                    title: 'Data collection',
                    route: 'datacollection',
                    controller: 'Isu.controller.DataCollectionOverview',
                    items: {
                        issues: {
                            title : 'Issues',
                            route: 'issues',
                            controller: 'Isu.controller.Issues',
                            action: 'showDataCollection',
                            items: {
                                view: {
                                    title: 'issue details',
                                    route: '{id}',
                                    controller: 'Isu.controller.IssueDetail'
                                },
                                edit: {
                                    title: 'Issue Edit',
                                    route: '{id}/addcomment',
                                    controller: 'Isu.controller.IssueDetail'
                                },
                                assign: {
                                    title: 'Issue Assign',
                                    route: '{id}/assign',
                                    controller: 'Isu.controller.AssignIssues'
                                },
                                close: {
                                    title: 'Issue Close',
                                    route: '{id}/close',
                                    controller: 'Isu.controller.CloseIssues'
                                },
                                notify: {
                                    title: 'Notify user',
                                    route: '{id}/notify',
                                    controller: 'Isu.controller.NotifySend',
                                    action: 'showNotifySend'
                                },
                                send: {
                                    title: 'Send to inspect',
                                    route: '{id}/send',
                                    controller: 'Isu.controller.NotifySend',
                                    action: 'showNotifySend'
                                }
                            }
                        },
                        bulk: {
                            title : 'Bulk Changes',
                            route: 'bulkaction',
                            controller: 'Isu.controller.BulkChangeIssues'
                        }
                    }
                },
                datavalidation: {
                    title: Uni.I18n.translate('router.datavalidation', 'ISU', 'Data validation'),
                    route: 'datavalidation',
                    controller: 'Isu.controller.DataValidation',
                    action: 'showOverview',
                    items: {
                        issues: {
                            title : 'Issues',
                            route: 'issues',
                            controller: 'Isu.controller.Issues',
                            action: 'showDataValidation',
                            items: {
                                view: {
                                    title: 'issue details',
                                    route: '{id}',
                                    controller: 'Isu.controller.IssueDetail'
                                }
                            }
                        },
                        bulk: {
                            title : 'Bulk Changes',
                            route: 'bulkaction',
                            controller: 'Isu.controller.BulkChangeIssues'
                        }
                    }
                }
            }
        }
    },

    init :function() {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});

Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration : {
            title: Uni.I18n.translate('route.administration', 'ISU', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                assignmentrules: {
                    title: Uni.I18n.translate('route.assignmentRules', 'ISU', 'Assignment Rules'),
                    route: 'assignmentrules',
                    controller: 'Isu.controller.IssueAssignmentRules'
                },
                creationrules: {
                    title: Uni.I18n.translate('route.issueCreationRules', 'ISU', 'Issue creation rules'),
                    route: 'creationrules',
                    controller: 'Isu.controller.IssueCreationRules',
                    items: {
                        add: {
                            title: Uni.I18n.translate('route.addIssueCreationRule', 'ISU', 'Add issue creation rule'),
                            route: 'add',
                            controller: 'Isu.controller.IssueCreationRulesEdit',
                            action: 'showCreate',
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('route.addAction', 'ISU', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Isu.controller.IssueCreationRulesActionsEdit',
                                    action: 'showCreate'
                                }
                            }
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Isu.controller.IssueCreationRulesEdit',
                            action: 'showEdit'
                        }
                    }
                }
            }
        }
    },

    init :function() {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});

Ext.define('Isu.model.Assignee', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
        },
        {
            name: 'idx',
            displayValue: 'IDX',
            type: 'string',
            convert: function (value, record) {
                var idx = null,
                    id = record.get('id'),
                    type = record.get('type');

                if (id && type) {
                    idx = id + ':' + type;
                }

                return idx;
            }
        },
        {
            name: 'type',
            displayValue: 'Type',
            type: 'auto'
        },
        {
            name: 'name',
            displayValue: 'Name',
            type: 'auto'
        }
    ],

    idProperty: 'idx',

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees',
        reader: {
            type: 'json',
            root: 'data'
        },
        buildUrl: function(request) {
            var idx = request.params.id,
                params;

            if (idx) {
                params = idx.split(':');
                return this.url + '/' + params[0] + '?assigneeType=' + params[1];
            } else {
                return this.url
            }
        }
    }
});

Ext.define('Isu.model.IssueMeter', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/meters',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.model.IssueReason', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/reasons',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.model.IssueStatus', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/statuses',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.model.IssueFilter', {
    extend: 'Uni.component.filter.model.Filter',

    requires: [
        'Isu.model.Assignee',
        'Isu.model.IssueMeter',
        'Isu.model.IssueReason',
        'Isu.model.IssueStatus'
    ],

    hasMany: {
        model: 'Isu.model.IssueStatus',
        associationKey: 'status',
        name: 'status'
    },

    hasOne: [
        {
            model: 'Isu.model.Assignee',
            associationKey: 'assignee',
            name: 'assignee',
            setterName: 'setAssignee',
            getterName: 'getAssignee'
        },
        {
            model: 'Isu.model.IssueReason',
            associationKey: 'reason',
            name: 'reason',
            setterName: 'setReason',
            getterName: 'getReason'
        },
        {
            model: 'Isu.model.IssueMeter',
            associationKey: 'meter',
            name: 'meter',
            setterName: 'setMeter',
            getterName: 'getMeter'
        }
    ],

    /**
     * @override
     * @returns {String[]}
     */
    getFields: function () {
        var fields = this.callParent();
        fields.push('assigneeId');
        fields.push('assigneeType');
        return fields;
    },

    /**
     * @override
     * @returns {*|Object}
     */
    getPlainData: function () {
        var data = this.callParent(),
            assignee;

        if (data.assignee) {
            assignee = data.assignee.split(':');
            data.assigneeId = assignee[0];
            data.assigneeType = assignee[1];
        }

        return data;
    }
});

Ext.define('Isu.model.IssueSort', {
    extend: 'Uni.component.sort.model.Sort',

    fields: [
        {
            name: 'dueDate',
            displayValue: 'Due date'
        }
    ]
});

Ext.define('Isu.model.IssueGrouping', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'value',
            type: 'string'
        },
        {
            name: 'display',
            type: 'string'
        }
    ],
    idProperty: 'value'
});

Ext.define('Isu.model.ExtraParams', {
    extend: 'Ext.data.Model',
    requires: [
        'Isu.model.IssueFilter',
        'Isu.model.IssueSort',
        'Isu.model.IssueGrouping',
        'Uni.util.Hydrator',
        'Uni.util.QueryString'
    ],
    fields: [
        {
            name: 'groupValue',
            type: 'int',
            defaultValue: null
        }
    ],
    hasOne: [
        {
            model: 'Isu.model.IssueFilter',
            associationKey: 'filter',
            name: 'filter'
        },
        {
            model: 'Isu.model.IssueSort',
            associationKey: 'sort',
            name: 'sort'
        },
        {
            model: 'Isu.model.IssueGrouping',
            associationKey: 'group',
            name: 'group'
        }
    ],

    getSorting: function (sorting) {
        var sortName = sorting.charAt(0) == '-' ? sorting.slice(1) : sorting,
            sortDirection = sorting.charAt(0) == '-' ? Uni.component.sort.model.Sort.DESC : Uni.component.sort.model.Sort.ASC;

        return {
            name: sortName,
            direction: sortDirection
        };
    },

    clearEmptyData: function (obg) {
        for (var prop in obg) {
            !obg[prop] && delete obg[prop];
        }
        return obg;
    },

    getDefaults: function () {
        return {
            sort: 'dueDate',
            status: 1,
            group: 'none'
        };
    },

    setValuesFromQueryString: function (callback) {
        var me = this,
            hydrator = new Uni.util.Hydrator(),
            filterModel = new Isu.model.IssueFilter(),
            sortModel = new Isu.model.IssueSort(),
            groupModel,
            groupStore = Ext.getStore('Isu.store.IssueGrouping'),
            queryString = Uni.util.QueryString.getQueryStringValues(),
            filterValues,
            sortValues,
            group,
            groupValue,
            data = {},
            sorting;

        delete queryString.limit;
        delete queryString.start;
        if (_.isEmpty(queryString)) {
            queryString = me.getDefaults();
        }

        filterValues = _.pick(queryString, filterModel.getFields());
        sortValues = _.pick(queryString, sortModel.getFields()).sort;
        group = queryString.group;
        groupValue = queryString.groupValue;

        if (group) {
            groupModel = groupStore.getById(group);
        } else {
            groupModel = groupStore.getAt(0);
        }

        if (groupValue) {
            data.reason = groupValue;
        }

        me.set('group', groupModel);
        me.set('groupValue', groupValue);

        if (Ext.isArray(sortValues)) {
            Ext.Array.each(sortValues, function (item) {
                sorting = me.getSorting(item);
                sortModel.addSortParam(sorting.name, sorting.direction);
            });
        } else if (sortValues) {
            sorting = me.getSorting(sortValues);
            sortModel.addSortParam(sorting.name, sorting.direction);
        }

        me.set('sort', sortModel);
        Ext.merge(data, sortModel.getPlainData());

        if (_.isEmpty(filterValues)) {
            me.set('filter', filterModel);
            callback && callback(me, me.clearEmptyData(data));
        } else {
            var promise = hydrator.hydrate(filterValues, filterModel);

            promise.then(function() {
                me.set('filter', filterModel);
                Ext.merge(data, filterModel.getPlainData());
                callback && callback(me, me.clearEmptyData(data));
            });
        }
    },

    getQueryStringFromValues: function () {
        var filterModel = this.get('filter'),
            sortModel = this.get('sort'),
            groupModel = this.get('group'),
            groupValue = this.get('groupValue') || [],
            newQueryString = {};

        Ext.Array.each(filterModel.getFields(), function (filterItem) {
            newQueryString[filterItem] = [];
        });
        Ext.Array.each(sortModel.getFields(), function (sortItem) {
            newQueryString[sortItem] = [];
        });

        if (groupModel && groupModel.get('value')) {
            newQueryString.group = groupModel.get('value');
            newQueryString.groupValue = !filterModel.get(groupModel.get('value')) ? groupValue : [];
        } else {
            newQueryString.groupValue = [];
        }

        Ext.merge(newQueryString, filterModel.getPlainData());
        Ext.merge(newQueryString, sortModel.getPlainData());

        return Uni.util.QueryString.buildHrefWithQueryString(this.clearEmptyData(newQueryString));
    }
});

/**
 * This class is used as a mixin.
 *
 * This class is to be used to provide basic methods for controllers that handle events of grid view.
 */
Ext.define('Isu.util.IsuGrid', {
    /**
     * Handle 'refresh' event.
     * Set tooltip for assignee type icon.
     * 'class' property of element must be equal 'isu-assignee-type-icon'.
     */
    setAssigneeTypeIconTooltip: function (grid) {
        var gridEl = grid.getEl(),
            icons = gridEl.query('.isu-assignee-type-icon');

        Ext.Array.each(icons, function (item) {
            var icon = Ext.get(item),
                text;

            if (icon.hasCls('isu-icon-USER')) {
                text = 'User';
            } else if (icon.hasCls('isu-icon-GROUP')) {
                text = 'User group';
            } else if (icon.hasCls('isu-icon-ROLE')) {
                text = 'User role';
            }

            if (text) {
                icon.tooltip = Ext.create('Ext.tip.ToolTip', {
                    target: icon,
                    html: text
                });

                grid.on('destroy', function () {
                    icon.tooltip.destroy();
                });
                grid.on('beforerefresh', function () {
                    icon.tooltip.destroy();
                });
            }
        });
    },

    /**
     * Handle 'refresh' event.
     * Set tooltip for description cell if inner text is shown with ellipsis.
     * 'rtdCls' property of column must be equal 'isu-grid-description'.
     */
    setDescriptionTooltip: function (grid) {
        var gridEl = grid.getEl(),
            descriptionCells = gridEl.query('.isu-grid-description');

        Ext.Array.each(descriptionCells, function (item) {
            var cell = Ext.get(item),
                cellInner = cell.down('.x-grid-cell-inner'),
                text = cellInner.getHTML();

            cell.tooltip = Ext.create('Ext.tip.ToolTip', {
                target: cell,
                html: text
            });

            grid.on('destroy', function () {
                cell.tooltip && cell.tooltip.destroy();
            });
            grid.on('beforerefresh', function () {
                cell.tooltip && cell.tooltip.destroy();
            });
        });
    },

    /**
     * Handle 'select' event.
     * Load item model and fire event for item panel view.
     */
    loadGridItemDetail: function (selectionModel, record) {
        var itemPanel = this.getItemPanel(),
            form = itemPanel.down('form');

        itemPanel.setLoading(true);

        this.gridItemModel.load(record.getId(), {
            success: function (record) {
                if (!form.isDestroyed) {
                    form.loadRecord(record);
                    form.up('panel').down('menu').record = record;
                    itemPanel.setLoading(false);
                    itemPanel.fireEvent('afterChange',itemPanel);
                    itemPanel.setTitle(record.data.title);
                }
            }
        });
    },

    /**
     * Handle 'refresh' event.
     * Select first row in grid.
     */

    selectFirstGridRow: function (grid) {
        var itemPanel = this.getItemPanel(),
            index = 0,
            item = grid.getNode(index),
            record;

        if (item) {
            itemPanel.show();
            record = grid.getRecord(item);
            grid.fireEvent('itemclick', grid, record, item, index);
        } else {
            itemPanel.hide();
        }
    }
});

/**
 * This class is used as a mixin.
 *
 * This class is to be used to provide basic methods for controllers that handle events of combobox.
 */
Ext.define('Isu.util.IsuComboTooltip', {
    /**
     * Sets tooltip for combobox.
     * Combobox must has 'tooltipText' property otherwise it sets default text.
     */
    setComboTooltip: function (combo) {
        combo.tooltip = Ext.DomHelper.append(Ext.getBody(), {
            tag: 'div',
            html: combo.tooltipText || 'Start typing',
            cls: 'isu-combo-tooltip'
        }, true);

        combo.tooltip.hide();

        combo.on('destroy', function () {
            combo.tooltip.destroy();
        });
        combo.on('focus', this.onFocusComboTooltip, this);
        combo.on('blur', this.onBlurComboTooltip, this);
    },

    /**
     * Handle 'focus' event.
     * If value of combobox is null shows tooltip.
     */
    onFocusComboTooltip: function (combo) {
        var tooltip = combo.tooltip,
            comboEl = Ext.get(combo.getEl());

        tooltip.setStyle({
            width: comboEl.getWidth(false) + 'px',
            top: comboEl.getY() + comboEl.getHeight(false) + 'px',
            left: comboEl.getX() + 'px'
        });

        if (!combo.getValue()) {
            tooltip.show();
        }

        combo.on('change', this.clearComboTooltip, this);
    },

    /**
     * Handle 'blur' event.
     * Hides tooltip of combobox on blur.
     */
    onBlurComboTooltip: function (combo) {
        var tooltip = combo.tooltip;

        tooltip && tooltip.hide();

        combo.un('change', this.clearComboTooltip, this);
    },

    /**
     * Handle 'change' event.
     * If value of combobox is null resets combobox and shows tooltip otherwise hides tooltip
     * and shows list of values.
     */
    clearComboTooltip: function (combo, newValue) {
        var listValues = combo.picker,
            tooltip = combo.tooltip;

        if (newValue == null) {
            combo.reset();
            listValues && listValues.hide();
            tooltip && tooltip.show();
        } else {
            tooltip && tooltip.hide();
            if (listValues) {
                listValues.show();
                Ext.get(listValues.getEl()).setStyle({
                    visibility: 'visible'
                });
            }
        }
    },

    limitNotification: function (combo) {
        var picker = combo.getPicker();

        if (picker) {
            picker.un('refresh', this.triggerLimitNotification, this);
            picker.on('refresh', this.triggerLimitNotification, this);
        }
    },

    triggerLimitNotification: function (view) {
        var store = view.getStore(),
            el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');

        if (store.getTotalCount() > store.getCount()) {
            el.appendChild({
                tag: 'li',
                html: 'Keep typing to narrow down',
                cls: Ext.baseCSSPrefix + 'boundlist-item isu-combo-limit-notification'
            });
        }
    }
});

Ext.define('Isu.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        }
    ]
});

Ext.define('Isu.model.Issues', {
    extend: 'Ext.data.Model',
    requires: [
        'Isu.model.IssueReason',
        'Isu.model.IssueStatus',
        'Isu.model.Device',
        'Isu.model.Assignee'
    ],
    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
        },
        {
            name: 'dueDate',
            displayValue: 'Due date',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'customer',
            displayValue: 'Customer',
            type: 'auto'
        },
        {
            name: 'creationDate',
            displayValue: 'Creation date',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            displayValue: 'Version',
            type: 'auto'
        },
        {
            name: 'device',
            type: 'auto'
        },
        {
            name: 'reason',
            type: 'auto'
        },
        {
            name: 'status',
            type: 'auto'
        },
        {
            name: 'assignee',
            type: 'auto'
        },
        {
            name: 'title', mapping: function (data) {
            // todo: internationalisation
            return data.reason.name + (data.device ? ' to ' + data.device.name + ' ' + data.device.serialNumber : '');
        }
        },
        {name: 'reason_name', mapping: 'reason.name'},
        {name: 'status_name', mapping: 'status.name'},
        {name: 'device_name', mapping: 'device.name'},
        {name: 'assignee_name', mapping: 'assignee.name'},
        {name: 'assignee_type', mapping: 'assignee.type'},
        {name: 'usage_point', mapping: 'device.usagePoint.info'},
        {name: 'service_location', mapping: 'device.serviceLocation.info'},
        {name: 'service_category', mapping: 'device.serviceCategory.info'},
        {
            name: 'status_name_f',
            mapping: function (data) {
                var filterIcon;
                if (data.status) {
                    filterIcon = '<span class="isu-icon-filter isu-apply-filter" data-filterType="status" data-filterValue="' + data.status.id + '"></span>';
                    return data.status.name + ' ' + filterIcon;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'reason_name_f',
            mapping: function (data) {
                var filterIcon;
                if (data.reason) {
                    filterIcon = '<span class="isu-icon-filter isu-apply-filter" data-filterType="reason" data-filterValue="' + data.reason.id + '" data-filterSearch="' + data.reason.name + '"></span>';
                    return data.reason.name + ' ' + filterIcon;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'device_f',
            mapping: function (data) {
                var filterIcon;
                if (data.device) {
                    filterIcon = '<span class="isu-icon-filter isu-apply-filter" data-filterType="meter" data-filterValue="' + data.device.id + '" data-filterSearch="' + data.device.serialNumber + '"></span>';
                    return '<a href="#/devices/' + data.device.serialNumber + '">' + data.device.name + ' ' + data.device.serialNumber + '</a>' + filterIcon;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'assignee_name_f',
            mapping: function (data) {
                var filterIcon;
                if (data.assignee) {
                    filterIcon = '<span class="isu-icon-filter isu-apply-filter" data-filterType="assignee" data-filterValue="' + data.assignee.id + ':' + data.assignee.type + '" data-filterSearch="' + data.assignee.name + '"></span>';
                    return data.assignee.name + filterIcon;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'devicelink',
            mapping: function (data) {
                if (data.device) {
                    return '<a href="#/devices/' + data.device.serialNumber + '">' + data.device.name + ' ' + data.device.serialNumber + '</a>';
                } else {
                    return '';
                }
            }
        }
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Isu.model.IssueReason',
            associationKey: 'reason',
            name: 'reason'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.IssueStatus',
            associationKey: 'status',
            name: 'status'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.Device',
            associationKey: 'device',
            name: 'device'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.Assignee',
            associationKey: 'assignee',
            name: 'assignee'
        },
        {
            type: 'hasMany',
            model: 'Isu.model.IssueComments',
            associationKey: 'comments',
            name: 'comments'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/isu/issue',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.view.workspace.issues.component.AssigneeCombo', {
    extend: 'Ext.ux.Rixo.form.field.GridPicker',

    alias: 'widget.issues-assignee-combo',
    store: 'Isu.store.Assignee',
    displayField: 'name',
    valueField: 'idx',

    triggerAction: 'query',
    queryMode: 'remote',
    queryParam: 'like',
    allQuery: '',
    lastQuery: '',
    queryDelay: 100,
    minChars: 0,
    disableKeyFilter: true,
    queryCaching: false,

    formBind: true,
    typeAhead: true,

    anchor: '100%',

    forceSelection: true,

    gridConfig: {
        emptyText: 'No assignee found',
        resizable: false,
        stripeRows: true,

        features: [
            {
                ftype: 'grouping',
                groupHeaderTpl: '{name}',
                collapsible: false
            }
        ],
        columns: [
            {
                header: false,
                xtype: 'templatecolumn',
                tpl: '<tpl if="type"><span class="isu-icon-{type} isu-assignee-type-icon"></span></tpl> {name}',
                flex: 1
            }
        ]
    },
    listeners: {
        focus: {
            fn: function(combo){
                if (!combo.getValue()) {
                    combo.doQuery(combo.getValue());
                }
            }
        },
        change: {
          fn: function(combo, newValue){
              if (!newValue){
                  combo.reset();
              }
          }
        },
        beforequery: {
            fn: function(queryPlan) {
                var store = queryPlan.combo.store;
                if (queryPlan.query) {
                    store.group('type');
                } else {
                    store.clearGrouping();
                }
            }
        }
    }
});


Ext.define('Isu.util.FilterCheckboxgroup', {
    extend: 'Ext.form.CheckboxGroup',
    alias: 'widget.filter-checkboxgroup',
    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    checkbox: {
        boxLabel: 'name',
        inputValue: 'id'
    },

    initComponent: function () {
        var me = this;

        me.bindStore(me.store || 'ext-empty-store', true);

        this.callParent(arguments);
    },

    beforeRender: function () {
        this.callParent(arguments);
        if (!this.store.isLoading()) {
            this.onLoad();
        }
    },

    onLoad: function () {
        var me = this;

        me.removeAll();
        Ext.Array.forEach(me.store.getRange(), function (item) {
            me.add({
                xtype: 'checkboxfield',
                boxLabel: item.data[me.checkbox.boxLabel],
                inputValue: item.data[me.checkbox.inputValue],
                name: me.name
            });
        });
        me.fireEvent('itemsadded', me, me.store);
    },

    setValue: function(data) {
        var values = {};
        values[this.name] = data;
        this.callParent([values]);
    },

    getStoreListeners: function () {
        return {
            load: this.onLoad
        };
    }
});

Ext.define('Isu.store.IssueStatus', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueStatus',
    autoLoad: true
});

Ext.define('Isu.store.IssueReason', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueReason',
    autoLoad: false,

    listeners: {
        beforeload: function () {
            this.getProxy().setExtraParam('issueType', 'datacollection');
        }
    }
});

Ext.define('Isu.view.workspace.issues.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-side-filter',
    cls: 'filter-form',
    width: 200,
    title: Uni.I18n.translate('general.title.filter', 'ISU', 'Filter'),
    ui: 'filter',
    requires: [
        'Isu.view.workspace.issues.component.AssigneeCombo',
        'Isu.util.FilterCheckboxgroup',
        'Uni.component.filter.view.Filter',
        'Isu.store.IssueStatus',
        'Isu.store.IssueReason'
    ],

    items: [
        {
            xtype: 'filter-form',
            itemId: 'filter-form',
            items: [
                {
                    itemId: 'filter-by-status',
                    xtype: 'filter-checkboxgroup',
                    store: 'Isu.store.IssueStatus',
                    name: 'status',
                    fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                    labelAlign: 'top',
                    columns: 1,
                    vertical: true
                },
                {
                    itemId: 'filter-by-assignee',
                    xtype: 'issues-assignee-combo',
                    name: 'assignee',
                    fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                    labelAlign: 'top',
                    forceSelection: true,
                    anyMatch: true,
                    emptyText: 'select an assignee',
                    tooltipText: 'Start typing for assignee'
                },
                {
                    itemId: 'filter-by-reason',
                    xtype: 'combobox',
                    name: 'reason',
                    fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                    labelAlign: 'top',

                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
                    store: 'Isu.store.IssueReason',

                    listConfig: {
                        cls: 'isu-combo-color-list',
                        emptyText: 'No reason found'
                    },

                    queryMode: 'remote',
                    queryParam: 'like',
                    queryDelay: 100,
                    queryCaching: false,
                    minChars: 1,

                    triggerAction: 'query',
                    anchor: '100%',
                    emptyText: 'select a reason',
                    tooltipText: 'Start typing for reason'
                },
                {
                    itemId: 'filter-by-meter',
                    xtype: 'combobox',
                    name: 'meter',
                    fieldLabel: Uni.I18n.translate('general.title.meter', 'ISU', 'Meter'),
                    labelAlign: 'top',

                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
                    store: 'Isu.store.IssueMeter',

                    listConfig: {
                        cls: 'isu-combo-color-list',
                        emptyText: 'No meter found'
                    },

                    queryMode: 'remote',
                    queryParam: 'like',
                    queryDelay: 100,
                    queryCaching: false,
                    minChars: 1,

                    triggerAction: 'query',
                    anchor: '100%',
                    emptyText: 'select a MRID of the meter',
                    tooltipText: 'Start typing for a MRID'
                }
            ]
        }
    ],

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [
                {
                    itemId: 'fApply',
                    ui: 'action',
                    text: 'Apply',
                    action: 'filter'
                },
                {
                    itemId: 'fReset',
                    text: 'Clear all',
                    action: 'reset'
                }
            ]
        }
    ]
});

Ext.define('Isu.view.workspace.issues.FilteringToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.TagButton'
    ],
    alias: 'widget.filtering-toolbar',
    title: 'Filters',
    name: 'filter',
    emptyText: 'None',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    /**
     * todo: I18n
     * @param filter Uni.component.filter.model.Filter
     */
    addFilterButtons: function (filter) {
        var me = this,
            btnClass = 'Uni.view.button.TagButton',
            container = me.getContainer(),
            assignee = filter.getAssignee().get('name'),
            reason = filter.getReason().get('name'),
            meter = filter.getMeter().get('name');


        container.removeAll();

        if (assignee) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-assignee',
                text: 'Assignee: ' + assignee,
                target: 'assignee'
            }));
        }

        if (reason) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-reason',
                text: 'Reason: ' + reason,
                target: 'reason'
            }));
        }

        if (meter) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-meter',
                text: 'Meter: ' + meter,
                target: 'meter'
            }));
        }

        if (filter.status().count()) {
            filter.status().each(function (status) {
                var c = container.add({
                    xtype: 'tag-button',
                    itemId: 'filter-by-status',
                    text: 'Status: ' + status.get('name'),
                    target: 'status',
                    targetId: status.getId()
                });
            });
        }
    }
});

Ext.define('Isu.store.IssueGrouping', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueGrouping',
    data: [
        {
            value: 'none',
            display: 'None'
        },
        {
            value: 'reason',
            display: 'Reason'
        }
    ]
});

Ext.define('Isu.view.workspace.issues.GroupingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Isu.store.IssueGrouping'
    ],
    alias: 'widget.grouping-toolbar',
    title: 'Group',
    name: 'group',
    showClearButton: false,
    content: {
        itemId: 'group_combobox',
        xtype: 'combobox',
        name: 'groupingcombo',
        store: 'Isu.store.IssueGrouping',
        editable: false,
        emptyText: 'None',
        queryMode: 'local',
        displayField: 'display',
        valueField: 'value',
        labelAlign: 'left'
    }
});

Ext.define('Isu.view.workspace.issues.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issue-sort-menu',
    itemId: 'SortMenu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'sortEl',
            text: 'Due date',
            action: 'dueDate'
        }
    ]
});

Ext.define('Isu.view.workspace.issues.SortingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.SortItemButton',
        'Isu.view.workspace.issues.SortMenu'
    ],
    alias: 'widget.sorting-toolbar',
    title: 'Sort',
    name: 'sortitemspanel',
    emptyText: 'None',
    tools: [
        {
            itemId: 'addSort',
            xtype: 'button',
            action: 'addSort',
            text: 'Add sort',
            menu: {
                xtype: 'issue-sort-menu'
            }
        }
    ],
    addSortButtons: function (sortModel) {
        var self = this,
            container = self.getContainer(),
            data = sortModel.getData(),
            menuItem,
            cls;

        container.removeAll();
        Ext.Object.each(data, function (key, value) {
            if (key != 'id' && value) {
                menuItem = self.down('issue-sort-menu [action=' + key + ']');
                cls = value == Isu.model.IssueSort.ASC
                    ? 'x-btn-sort-item-asc'
                    : 'x-btn-sort-item-desc';

                container.add({
                    itemId: 'sortingBy',
                    xtype: 'sort-item-btn',
                    text: menuItem.text,
                    sortName: key,
                    sortDirection: value,
                    iconCls: cls
                });
            }
        });
    }
});

Ext.define('Isu.model.IssuesGroups', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'reason',
            type: 'text'
        },
        {
            name: 'number',
            type: 'int'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/issue/groupedlist',
        reader: {
            type: 'json',
            root: 'data',
            totalProperty: 'totalCount'
        }
    }
});

Ext.define('Isu.store.IssuesGroups', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssuesGroups',
    pageSize: 10,
    autoLoad: false,

    listeners: {
        beforeload: function () {
            this.getProxy().setExtraParam('issueType', 'datacollection');
        }
    }
 });

Ext.define('Isu.view.workspace.issues.GroupGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.store.IssuesGroups'
    ],
    alias: 'widget.issue-group-grid',
    store: 'Isu.store.IssuesGroups',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'reason',
                text: 'Reason',
                dataIndex: 'reason',
                flex: 5
            },
            {
                itemId: 'issues_num',
                text: 'Issues',
                dataIndex: 'number',
                flex: 1
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('workspace.issues.groupGrid.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} items'),
                displayMoreMsg: Uni.I18n.translate('workspace.issues.groupGrid.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} items'),
                emptyMsg: '0 reasons'
            },
            {
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('workspace.issues.groupGrid.pagingtoolbartop.itemsPerPageMsg', 'ISU', 'Items per page')
            }
        ];

        me.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.IssueGroup', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.workspace.issues.GroupGrid'
    ],
    itemId: 'IssueGroup',
    alias: 'widget.issue-group',
    hidden: true,
    items: [
        {
            itemId: 'issue-group-grid',
            xtype: 'issue-group-grid'
        },
        {
            name: 'issue-group-info',
            hidden: true
        }
    ]
});

Ext.define('Isu.view.workspace.issues.TopPanel', {
    extend: 'Ext.container.Container',
    requires: [
        'Isu.view.workspace.issues.FilteringToolbar',
        'Isu.view.workspace.issues.GroupingToolbar',
        'Isu.view.workspace.issues.SortingToolbar',
        'Isu.view.workspace.issues.IssueGroup',
        'Ext.menu.Separator'
    ],
    alias: 'widget.issues-top-panel',
    items: [
        {
            xtype: 'filtering-toolbar',
            itemId: 'filtering-toolbar'
        },
        {
            xtype: 'menuseparator'
        },
        {
            xtype: 'grouping-toolbar',
            itemId: 'grouping-toolbar'
        },
        {
            xtype: 'menuseparator'
        },
        {
            xtype: 'sorting-toolbar',
            itemId: 'sorting-toolbar'
        },
        {
            xtype: 'issue-group',
            itemId: 'issue-group'
        }
    ]
});

Ext.define('Isu.view.workspace.issues.ActionMenu', {

    extend: 'Ext.menu.Menu',
    alias: 'widget.issue-action-menu',
    plain: true,
    items: [
        {   itemId: 'assign',
            text: 'Assign',
            action: 'assign'
        },
        {
            itemId: 'close',
            text: 'Close',
            action: 'close'
        },
        {
            itemId: 'addcomment',
            text: 'Add comment',
            action: 'addcomment'
        },
        {
            itemId: 'notify',
            text: 'Notify user',
            action: 'notify'
        },
        {
            itemId: 'send',
            text: 'Send to inspect',
            action: 'send'
        }
    ]
});

Ext.define('Isu.view.workspace.issues.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.view.workspace.issues.ActionMenu'
    ],
    alias: 'widget.issues-grid',
    store: 'Isu.store.Issues',
    issueType: null,
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'issues-grid-title',
                header: Uni.I18n.translate('general.title.title', 'ISU', 'Title'),
                dataIndex: 'reason',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var device = record.get('device'),
                        title = value.name + (device ? ' to ' + device.name + ' ' + device.serialNumber : ''),
                        url = me.router.getRoute(me.router.currentRoute + '/view').buildUrl({id: record.getId()});

                    return '<a href="' + url + '">' + title + '</a>';
                }
            },
            {
                itemId: 'issues-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'issues-grid-assignee',
                header: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee_type"><span class="isu-icon-{assignee_type} isu-assignee-type-icon"></span></tpl> {assignee_name}',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'issue-action-menu',
                    itemId: 'issue-action-menu',
                    issueType: me.issueType
                }
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} issues'),
                displayMoreMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} issues'),
                emptyMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.emptyMsg', 'ISU', 'There are no issues to display'),
                items: [
                    '->',
                    {
                        itemId: 'bulkAction',
                        xtype: 'button',
                        text: Uni.I18n.translate('general.title.bulkActions', 'ISU', 'Bulk action'),
                        action: 'bulkchangesissues',
                        hrefTarget: '',
                        href: me.router.getRoute('workspace/' + me.issueType.toLowerCase() + '/bulk').buildUrl()
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('workspace.issues.pagingtoolbarbottom.itemsPerPage', 'ISU', 'Issues per page')
            }
        ];

        me.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.DataCollectionPreviewForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay'
    ],
    alias: 'widget.datacollection-issue-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    ui: 'medium',
    showFilters: false,
    router: null,
    initComponent: function () {
        var me = this,
            displayFieldType;

        if (me.showFilters) {
            displayFieldType = 'filter-display';
        } else {
            displayFieldType = 'displayfield';
        }

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: displayFieldType,
                        itemId: 'reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: '_customer',
                        fieldLabel: Uni.I18n.translate('general.title.customer', 'ISU', 'Customer'),
                        name: 'customer'
                    },
                    {
                        itemId: '_location',
                        fieldLabel: Uni.I18n.translate('general.title.location', 'ISU', 'Location'),
                        name: 'service_location'
                    },
                    {
                        itemId: '_usagepoint',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'ISU', 'Usage point'),
                        name: 'usage_point'
                    },
                    {
                        xtype: displayFieldType,
                        itemId: 'device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var url = '',
                                result = '';

                            if (value) {
                                if (value.serialNumber) {
                                    url = me.router.getRoute('devices/device').buildUrl({mRID: value.serialNumber});
                                    result = '<a href="' + url + '">' + value.name + ' ' + value.serialNumber + '</a>';
                                } else {
                                    result = value.name + ' ' + value.serialNumber;
                                }
                            }

                            return result;
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: displayFieldType,
                        itemId: 'status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: '_dueDate',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                        name: 'dueDate',
                        renderer: Ext.util.Format.dateRenderer('M d, Y')
                    },
                    {
                        xtype: displayFieldType,
                        itemId: 'assignee',
                        fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                        name: 'assignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.none', 'ISU', 'None');
                        }
                    },
                    {
                        itemId: '_creationDate',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISU', 'Creation date'),
                        name: 'creationDate',
                        renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                    },
                    {
                        itemId: '_serviceCat',
                        fieldLabel: Uni.I18n.translate('general.title.serviceCategory', 'ISU', 'Service category'),
                        name: 'service_category'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.DataValidationPreviewForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay'
    ],
    alias: 'widget.datavalidation-issue-form',
    ui: 'medium',
    showFilters: false,
    router: null,
    initComponent: function () {
        var me = this,
            displayFieldType;

        if (me.showFilters) {
            displayFieldType = 'filter-display';
        } else {
            displayFieldType = 'displayfield';
        }

        me.items = [
            {
                xtype: 'container',
                layout: 'column',
                defaults: {
                    xtype: 'container',
                    layout: 'form',
                    columnWidth: 0.5
                },
                items: [
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                xtype: displayFieldType,
                                itemId: 'reason',
                                fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                                name: 'reason',
                                renderer: function (value) {
                                    return value.name ? value.name : '';
                                }
                            },
                            {
                                itemId: '_customer',
                                fieldLabel: Uni.I18n.translate('general.title.customer', 'ISU', 'Customer'),
                                name: 'customer'
                            },
                            {
                                itemId: '_location',
                                fieldLabel: Uni.I18n.translate('general.title.location', 'ISU', 'Location'),
                                name: 'service_location'
                            },
                            {
                                itemId: '_usagepoint',
                                fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'ISU', 'Usage point'),
                                name: 'usage_point'
                            },
                            {
                                xtype: displayFieldType,
                                itemId: 'device',
                                fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
                                name: 'device',
                                renderer: function (value) {
                                    var url = '',
                                        result = '';

                                    if (value) {
                                        if (value.serialNumber) {
                                            url = me.router.getRoute('devices/device').buildUrl({mRID: value.serialNumber});
                                            result = '<a href="' + url + '">' + value.name + ' ' + value.serialNumber + '</a>';
                                        } else {
                                            result = value.name + ' ' + value.serialNumber;
                                        }
                                    }

                                    return result;
                                }
                            }
                        ]
                    },
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                xtype: displayFieldType,
                                itemId: 'status',
                                fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                                name: 'status',
                                renderer: function (value) {
                                    return value.name ? value.name : '';
                                }
                            },
                            {
                                itemId: '_dueDate',
                                fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                                name: 'dueDate',
                                renderer: Ext.util.Format.dateRenderer('M d, Y')
                            },
                            {
                                xtype: displayFieldType,
                                itemId: 'assignee',
                                fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                                name: 'assignee',
                                renderer: function (value) {
                                    return value.name ? value.name : Uni.I18n.translate('general.none', 'ISU', 'None');
                                }
                            },
                            {
                                itemId: '_creationDate',
                                fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISU', 'Creation date'),
                                name: 'creationDate',
                                renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                            },
                            {
                                itemId: '_serviceCat',
                                fieldLabel: Uni.I18n.translate('general.title.serviceCategory', 'ISU', 'Service category'),
                                name: 'service_category'
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'container',
                layout: 'form',
                margin: '20 0 0 0',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: '&nbsp;',
                        value: '<b>' + Uni.I18n.translate('workspace.datavalidation.overview.title', 'ISU', 'Data validation') + '</b>'
                    },
                    {
                        itemId: '_validationRule',
                        fieldLabel: Uni.I18n.translate('general.title.validationRule', 'ISU', 'Validation rule'),
                        name: 'validationRule'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-preview',
    requires: [
        'Isu.view.workspace.issues.ActionMenu',
        'Isu.view.workspace.issues.DataCollectionPreviewForm',
        'Isu.view.workspace.issues.DataValidationPreviewForm'
    ],
    title: '',
    frame: true,
    issueType: null,
    router: null,

    initComponent: function () {
        var me =this,
            previewForm;

        switch (me.issueType) {
            case 'datacollection':
                previewForm = 'datacollection-issue-form';
                break;
            case 'datavalidation':
                previewForm = 'datavalidation-issue-form';
                break;
        }

        me.items = {
            xtype: previewForm,
            itemId: 'issues-preview-form',
            showFilters: true,
            router: me.router,
            bbar: {
                layout: {
                    type: 'vbox',
                    align: 'right'
                },
                items: {
                    text: Uni.I18n.translate('general.title.viewDetails', 'ISU', 'View details'),
                    itemId: 'view-details-link',
                    ui: 'link',
                    href: location.href
                }
            }
        };

        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'issue-action-menu',
                    itemId: 'issue-action-menu',
                    issueType: me.issueType
                }
            }
        ];

        me.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-setup',
    itemId: 'issuesOverview',
    issueType: null,
    router: null,

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.navigation.SubMenu',
        'Isu.view.workspace.issues.SideFilter',
        'Isu.view.workspace.issues.TopPanel',
        'Isu.view.workspace.issues.Grid',
        'Isu.view.workspace.issues.Preview'

        /*'Isu.view.workspace.issues.Filter',
        'Isu.view.workspace.issues.List',
        'Isu.view.workspace.issues.Item',
        'Isu.view.workspace.issues.SideFilter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'*/
    ],

    side: {
        itemId: 'navigation',
        xtype: 'panel',
        ui: 'medium',
        title: 'Navigation',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'overview',
                xtype: 'menu',
                title: 'Overview',
                ui: 'side-menu',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                floating: false,
                plain: true,
                items: [
                    {
                        text: 'Issues',
                        cls: 'current'
                    }
                ]
            },
            {   itemId: 'issues-side-filter',
                xtype: 'issues-side-filter'
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues'),
                items: [
                    {   itemId: 'issues-top-panel',
                        xtype: 'issues-top-panel'
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'issues-grid',
                            itemId: 'issues-grid',
                            issueType: me.issueType,
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('workspace.issues.empty.title', 'ISU', 'No issues found'),
                            reasons: [
                                Uni.I18n.translate('workspace.issues.empty.list.item1', 'ISU', 'No issues have been defined yet.'),
                                Uni.I18n.translate('workspace.issues.empty.list.item2', 'ISU', 'No issues comply to the filter.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'issues-preview',
                            itemId: 'issues-preview',
                            issueType: me.issueType,
                            router: me.router
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Uni.component.filter.store.Filterable',
        'Uni.component.sort.store.Sortable',
        'Isu.model.IssueFilter',
        'Uni.util.Hydrator'
    ],

    mixins: [
        'Uni.component.filter.store.Filterable',
        'Uni.component.sort.store.Sortable'
    ],

    model: 'Isu.model.Issues',
    pageSize: 10,
    autoLoad: false
});

Ext.define('Isu.store.Assignee', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Assignee',
    pageSize: 100,
    groupField: 'type',
    autoLoad: false,
    sorters: [{
        sorterFn: function(o1, o2){
            return o1.get('name').toUpperCase() > o2.get('name').toUpperCase()
        }
    }]

});

Ext.define('Isu.store.IssueMeter', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueMeter',
    pageSize: 50,
    autoLoad: false
});

Ext.define('Isu.model.UserGroupList', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'id',
            type: 'auto'
        },
        {
            name: 'name',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'auto'
        }
    ]
});

Ext.define('Isu.store.UserGroupList', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.UserGroupList',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/groups',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

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

    assignToMe: false,

    init: function () {
        this.control({
            'issues-setup': {
                afterrender: this.checkAssignee
            },
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

        this.on('showIssuesAssignedOnMe', function () {
            this.assignToMe = true;
        }, this);
    },

    showDataCollection: function () {
        this.showOverview('datacollection');
    },

    showDataValidation: function () {
        this.showOverview('datavalidation');
    },

    checkAssignee: function () {
        if (this.assignToMe) {
            this.assignedToMe();
        }
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
        window.location.replace(this.extraParamsModel.getQueryStringFromValues());
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
            me.assignToMe = false;
        }});
    }
});

Ext.define('Isu.util.FormErrorMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-form-error-message',
    ui: 'form-error-framed',
    text: null,
    defaultText: 'There are errors on this page that require your attention.',
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    errorIcon: null,
    defaultErrorIcon: 'x-uni-form-error-msg-icon',
    margin: '7 0 32 0',
    beforeRender: function () {
        var me = this;
        if (!me.text) {
            me.text = me.defaultText;
        }
        if (!me.errorIcon) {
            me.errorIcon = me.defaultErrorIcon
        }
        me.renew();
        me.callParent(arguments)
    },

    renew: function () {
        var me = this;
        me.removeAll(true);
        me.add([
            {
                xtype: 'box',
                height: 22,
                width: 26,
                cls: me.errorIcon
            },
            {
                ui: 'form-error',
                name: 'errormsgpanel',
                html: me.text
            }
        ]);
    },

    setText: function (text) {
        var me = this;
        me.text = text;
        me.renew();
    }

});

Ext.define('Isu.view.workspace.issues.AssignForm', {
    extend: 'Ext.form.Panel',
    defaults: {
        border: false
    },
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup'
    ],
    ui: 'medium',
    title: 'Assign issue',

    alias: 'widget.issues-assign-form',

    items: [
        {
            xtype: 'panel',
            margin: '0 0 20 0',
            layout : {
              type: 'vbox',
              align: 'left'
            },
            items: {
                itemId: 'form-errors',
                xtype: 'uni-form-error-message',
                name: 'form-errors',
                hidden: true
            }
        },
        {
            margin: '0 0 0 100',
            defaults: {
                border: false
            },
            items: [
                {
                    layout: 'hbox',
                    defaults: {
                        border: false
                    },
                    items: [
                        {
                            width: 80,
                            items: {
                                itemId: 'AssignTo',
                                xtype: 'label',
                                text: 'Assign to *'
                            }
                        },
                        {
                            itemId: 'radiogroup',
                            xtype: 'radiogroup',
                            formBind: false,
                            columns: 1,
                            vertical: true,
                            width: 100,
                            defaults: {
                                name: 'assignTo',
                                formBind: false,
                                submitValue: false
                            },
                            listeners: {
                                change: {
                                    fn: function (radiogroup, newValue, oldValue) {
                                        var form = radiogroup.up('issues-assign-form');
                                        form.assignToOnChange(radiogroup, newValue, oldValue);
                                    }
                                },
                                afterrender: {
                                    fn: function(rgroup){
                                        rgroup.reset();
                                    }
                                }
                            },
                            items: [
                                {
                                    itemId: 'USER',
                                    boxLabel: 'User',
                                    inputValue: 'USER'
                                },
                                {
                                    itemId: 'ROLE',
                                    boxLabel: 'User role',
                                    margin: '5 0 0 0',
                                    inputValue: 'ROLE'
                                },
                                {
                                    itemId: 'GROUP',
                                    boxLabel: 'User group',
                                    margin: '5 0 0 0',
                                    inputValue: 'GROUP'
                                }
                            ]
                        },
                        {
                            defaults: {
                                xtype: 'combobox',
                                queryMode: 'local',
                                valueField: 'id',
                                forceSelection: true,
                                anyMatch: true,
                                msgTarget: 'under',
                                validateOnChange: false,
                                validateOnBlur: false,
                                width: 300,
                                listeners: {
                                    errorchange: {
                                        fn: function (combo, errEl) {
                                            var form = combo.up('issues-assign-form');
                                            form.comboOnError(combo, errEl);
                                        }
                                    },
                                    focus: {
                                        fn: function (combo) {
                                            var radiobutton = combo.up().previousNode('radiogroup').down('[inputValue=' + combo.name + ']');
                                            radiobutton.setValue(true);
                                            var arrCombo = Ext.ComponentQuery.query('issues-assign-form combobox');
                                            Ext.Array.each(arrCombo, function (item) {
                                                item.allowBlank = true;
                                            });
                                            combo.allowBlank = false;
                                        }
                                    }
                                }
                            },
                            items: [
                                {
                                    itemId: 'Ucombo',
                                    name: 'USER',
                                    store: 'Isu.store.UserList',
                                    allowBlank: false,
                                    displayField: 'authenticationName'
                                },
                                {
                                    itemId: 'Rcombo',
                                    name: 'ROLE',
                                    store: 'Isu.store.UserRoleList',
                                    displayField: 'name'
                                },
                                {   itemId: 'Gcombo',
                                    name: 'GROUP',
                                    store: 'Isu.store.UserGroupList',
                                    displayField: 'name'
                                }
                            ]
                        }
                    ]
                },
                {
                    layout: 'hbox',
                    margin: '20 0 0 0',
                    defaults: {
                        border: false
                    },
                    items: [
                        {
                            width: 80,
                            items: {
                                itemId: 'Comment',
                                xtype: 'label',
                                text: 'Comment'
                            }
                        },
                        {
                            itemId: 'commentarea',
                            xtype: 'textareafield',
                            name: 'comment',
                            emptyText: 'Provide a comment \r\n(optionally)',
                            width: 397
                        }
                    ]
                }
            ]
        }
    ],
    listeners: {
        fielderrorchange: {
            fn: function (form, lable, error) {
                form.onFieldErrorChange(form, lable, error);
            }
        },
        afterrender: function (form) {
            var values = Ext.state.Manager.get('formAssignValues');
            Ext.ComponentQuery.query('issues-assign-form radiogroup')[0].down('[inputValue=USER]').setValue(true);

            if (values) {
                Ext.Object.each(values, function (key, value) {
                    if (key == 'comment') {
                        form.down('textareafield').setValue(value);
                    }
                    if (key == 'GROUP') {
                        form.down('combobox[name=GROUP]').setValue(value);
                    }
                    if (key == 'ROLE') {
                        form.down('combobox[name=ROLE]').setValue(value);
                    }
                    if (key == 'USER') {
                        form.down('combobox[name=USER]').setValue(value);
                    }
                });
            }
            var selRadio = Ext.state.Manager.get('formAssignRadio');
            if (selRadio) {
                var radio = form.down('radiogroup').down('[inputValue=' + selRadio + ']');
                radio.setValue(true);
            }
        }
    },

    assignToOnChange: function (radiogroup, newValue, oldValue) {
        var activeCombobox = radiogroup.next().down('[name=' + newValue.assignTo + ']'),
            inactiveCombobox = radiogroup.next().down('[name=' + oldValue.assignTo + ']'),
            currentBoxLabel = radiogroup.down('[checked=true]').boxLabel,
            tooltips = Ext.ComponentQuery.query('tooltip[anchor=top]');

        Ext.each(tooltips, function (tooltip) {
           tooltip.destroy();
        });

        Ext.create('Ext.tip.ToolTip', { target:  activeCombobox.getEl(), html: 'Start typing for ' + currentBoxLabel.toLowerCase() + 's', anchor: 'top' });
        if (!Ext.isEmpty(inactiveCombobox)) {
            inactiveCombobox.allowBlank = true;
        }
        activeCombobox.setDisabled(false);
        activeCombobox.allowBlank = false;
        activeCombobox.focus();
    },

    comboOnError: function (combo, errEl) {
        var radiobutton = combo.up().previousNode('radiogroup').down('[inputValue=' + combo.name + ']'),
            initMargin = radiobutton.margin ? radiobutton.margin.split(' ') : [0, 0, 0, 0],
            addMargin = 24,
            finMargin;

        initMargin[2] = errEl ? parseInt(initMargin[2]) + addMargin : parseInt(initMargin[2]) - addMargin;
        finMargin = initMargin.join(' ');
        radiobutton.setMargin(finMargin);
        radiobutton.margin = finMargin;
    },

    onFieldErrorChange: function (form, lable, error) {
        var formErrorsPanel;
        if (form.xtype == 'issues-assign-form') {
            formErrorsPanel = form.down('[name=form-errors]');

            if (error) {
                formErrorsPanel.hide();
                formErrorsPanel.removeAll();
                formErrorsPanel.add({
                    html: 'There are errors on this page that require your attention.',
                    itemId: 'error'
                });
                formErrorsPanel.show();
            }
        }
    },

    loadRecord: function(record) {
        var title = 'Assign issue "' + record.get('title') + '"'

        this.setTitle(title);
        this.callParent(arguments)
    }
});

Ext.define('Isu.view.workspace.issues.Assign', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.Hidden',
        'Isu.view.workspace.issues.AssignForm'
    ],
    alias: 'widget.issues-assign',

    content: {
        items: {
            xtype: 'issues-assign-form'
        },
        buttons: [
            {   itemId: 'Assign',
                text: 'Assign',
                name: 'assign',
                formBind: false
            },
            {
                itemId: 'Cancel',
                text: 'Cancel',
                name: 'cancel',
                ui: 'link',
                hrefTarget: '',
                href: '#/workspace/datacollection/issues'
            }
        ]
    }
});

Ext.define('Isu.model.UserList', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'id',
            type: 'auto'
        },
        {
            name: 'authenticationName',
            type: 'auto'
        },
        {
            name: 'description',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'auto'
        },
        {
            name: 'groups',
            type: 'auto'
        }
    ]
});

Ext.define('Isu.store.UserList', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.UserList',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/usr/users',
        reader: {
            type: 'json',
            root: 'users'
        }
    }
});

Ext.define('Isu.model.UserRoleList', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'id',
            type: 'auto'
        },
        {
            name: 'name',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'auto'
        }
    ]
});

Ext.define('Isu.store.UserRoleList', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.UserRoleList',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/roles',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.controller.AssignIssues', {
        extend: 'Ext.app.Controller',

        requires: [
        ],

        stores: [
            'Isu.store.UserList',
            'Isu.store.UserRoleList',
            'Isu.store.UserGroupList'
        ],

        views: [
            'workspace.issues.Assign'
        ],

        refs: [
            {
                ref: 'itemPanel',
                selector: 'issues-item'
            }
        ],

        init: function () {
            this.control({
                'issues-assign button[name=assign]': {
                    click: this.onSubmitForm
                }
            });
            this.getApplication().on('assignissue', this.onSubmitForm)
        },

        showOverview: function (issueId) {
            var self = this,
                model = self.getModel('Issues'),
                widget;

            model.load(issueId, {
                success: function (record) {
                    widget = Ext.widget('issues-assign');
                    widget.getCenterContainer().down('issues-assign-form').loadRecord(record);
                    self.getApplication().fireEvent('changecontentevent', widget);
                }
            });
        },

        onSubmitForm: function () {
            var self = this,
                assignPanel = Ext.ComponentQuery.query('issues-assign')[0],
                formPanel = assignPanel.down('issues-assign-form'),
                activeCombo = formPanel.down('combobox[allowBlank=false]'),
                form = formPanel.getForm(),
                record = formPanel.getRecord(),
                formValues = form.getValues(),
                url = '/api/isu/issue/assign',
                sendingData = {},
                preloader;


            if (form.isValid()) {
                sendingData.issues = [
                    {
                        id: record.getId(),
                        version: record.get('version')
                    }
                ];
                sendingData.assignee = {
                    id: activeCombo.findRecordByValue(activeCombo.getValue()).data.id,
                    type: activeCombo.name,
                    title: activeCombo.rawValue
                };
                sendingData.comment = formValues.comment;
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Assigning issue",
                    name: 'assign-issu-form-submit',
                    target: assignPanel
                });
                preloader.show();
                Ext.Ajax.request({
                    url: url,
                    method: 'PUT',
                    jsonData: sendingData,
                    autoAbort: true,
                    success: function (resp) {
                        var response = Ext.JSON.decode(resp.responseText),
                            successArr = response.data.success,
                            failureArr = response.data.failure,
                            activeCombo = assignPanel.down('issues-assign-form combobox[allowBlank=false]'),
                            msges = [];
                        if (failureArr.length > 0) {
                            Ext.Array.each(failureArr, function (item) {
                                Ext.Array.each(item.issues, function (issue) {
                                    var header = {},
                                        bodyItem = {};
                                    header.text = 'Failed to assign issue ' + record.data.reason_name + (record.data.device_name ? ' to ' + record.data.device_name + ' ' + record.raw.device.serialNumber : '') + ' to ' + activeCombo.rawValue;
                                    header.style = 'msgHeaderStyle';
                                    msges.push(header);
                                    bodyItem.text = item.reason;
                                    bodyItem.style = 'msgItemStyle';
                                    msges.push(bodyItem);
                                })
                            });

                            if (msges.length > 0) {
                                self.getApplication().fireEvent('isushowmsg', {
                                    type: 'error',
                                    closeBtn: true,
                                    msgBody: msges,
                                    y: 10,
                                    btns: [
                                        {
                                            text: 'Retry',
                                            hnd: function () {
                                                assignPanel.enable();
                                                self.getApplication().fireEvent('assignissue')
                                            }
                                        },
                                        {
                                            text: 'Cancel',
                                            cls: 'isu-btn-link',
                                            hrefTarget: '',
                                            href: '#/workspace/datacollection/issues',
                                            // this function is necessary and MUST be empty
                                            hnd: function () {

                                            }
                                        }
                                    ],
                                    listeners: {
                                        close: {
                                            fn: function () {
                                                assignPanel.enable();
                                            }
                                        }
                                    }
                                });
                                assignPanel.disable();
                            }
                        }

                        msges = [];

                        if (successArr.length > 0) {
                            Ext.Array.each(successArr, function () {
                                self.getApplication().fireEvent('acknowledge', 'Issue assigned to ' + activeCombo.rawValue);
                            });
                            window.location.href = '#/workspace/datacollection/issues';
                        }
                    },
                    callback: function () {
                        preloader.destroy();
                    }
                });
            }
        }
    }
);

Ext.define('Isu.view.workspace.issues.CloseForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.TextArea'
    ],
    alias: 'widget.issues-close-form',

    defaults: {
//        border: false
    },

    items: [
        {
            xype: 'container',
            border: 0,
            items: [
                {
                    itemId: 'radiogroup',
                    xtype: 'radiogroup',
                    fieldLabel: 'Reason *',
                    name: 'status',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    items: []
                },
                {
                    itemId: 'Comment',
                    xtype: 'textarea',
                    fieldLabel: 'Comment',
                    name: 'comment',
                    width: 500,
                    height: 150,
                    emptyText: 'Provide a comment (optionally)'
                }
            ]
        }
    ]
});

Ext.define('Isu.view.workspace.issues.Close', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-close',

    requires: [
        'Isu.view.workspace.issues.CloseForm'
    ],

    initComponent: function () {
        var self = this;
        self.callParent(arguments);
        self.addForm();
    },

    addForm: function () {

        var self = this;
//record.raw.device is undefined for the moment
        self.title = 'Close issue "' + self.record.data.reason_name + ' to ' + self.record.data.device_name + '"'; // + ' ' + self.record.raw.device.serialNumber+'"';

        self.getCenterContainer().add({
            flex: 1,
            minHeight: 305,
            border: false,
            header: false,
            recordTitle: self.title,
            bodyPadding: 10,
            defaults: {
                border: false
            },
            items: [
                {
                    xtype: 'panel',
                    ui: 'medium',
                    title: self.title

                },
                {
                    itemId: 'close-form',
                    xtype: 'issues-close-form',
                    padding: '30 50 0 50',
                    margin: '0',
                    defaults: {
                        padding: '0 0 30 0'
                    }

                },
                {
                    xtype: 'container',
                    padding: '0 155',
                    defaults: {
                        xtype: 'button',
                        margin: '0 10 0 0'
                    },
                    items: [
                        {
                            itemId: 'Close',
                            name: 'close',
                            text: 'Close',
                            formBind: true
                        },
                        {
                            itemId: 'Cancel',
                            text: 'Cancel',
                            name: 'cancel',
                            ui: 'link',
                            hrefTarget: '',
                            href: '#/workspace/datacollection/issues'
                        }
                    ]
                }
            ]
        });
    }
});

Ext.define('Isu.controller.CloseIssues', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'workspace.issues.Close'
    ],

    init: function () {
        this.control({
            'issues-close issues-close-form': {
                beforerender: this.issueClosingFormBeforeRenderEvent
            },
            'bulk-browse bulk-wizard bulk-step3 issues-close-form': {
                beforerender: this.issueClosingFormBeforeRenderEvent
            },
            'issues-close button[name=close]': {
                click: this.submitIssueClosing
            },
            'message-window': {
                remove: this.enableButtons
            }
        });
        this.getApplication().on('closeissue', this.submitIssueClosing)
    },

    showOverview: function (issueId) {
        var self = this,
            model = self.getModel('Issues'),
            widget;

        model.load(issueId, {
            success: function (record) {
                widget = Ext.widget('issues-close', {
                    record: record
                });
                self.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    enableButtons: function () {
        var  formButtons = Ext.ComponentQuery.query('issues-close button');

        Ext.each(formButtons, function (button) {
            button.enable();
        });

    },

    issueClosingFormBeforeRenderEvent: function (form) {
        var statusesContainer = form.down('[name=status]'),
            values = Ext.state.Manager.get('formCloseValues');
        Ext.Ajax.request({
            url: '/api/isu/statuses',
            method: 'GET',
            success: function (response) {
                var statuses = Ext.decode(response.responseText).data;
                Ext.each(statuses, function (status) {
                    if (!Ext.isEmpty(status.allowForClosing) && status.allowForClosing) {
                        statusesContainer.add({
                            boxLabel: status.name,
                            inputValue: status.id,
                            name: 'status'
                        })
                    }
                });
                if (Ext.isEmpty(values)) {
                    statusesContainer.items.items[0].setValue(true);
                } else {
                    statusesContainer.down('[inputValue=' + values.status + ']').setValue(true);
                }
            }
        });
        if (values) {
            form.down('textarea').setValue(values.comment);
        }
    },

    submitIssueClosing: function () {
        var self = this,
            closeView = Ext.ComponentQuery.query('issues-close')[0],
            record = closeView.record,
            formPanel = closeView.down('issues-close-form'),
            form = formPanel.getForm(),
            formValues = form.getValues(),
            url = '/api/isu/issue/close',
            sendingData = {},
            preloader;

        if (form.isValid()) {
            sendingData.issues = [
                {
                    id: record.data.id,
                    version: record.data.version
                }
            ];
            sendingData.status = formValues.status;
            sendingData.comment = formValues.comment.trim();
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Closing issue",
                name: 'close-issue-form-submit',
                target: closeView
            });
            preloader.show();

            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                jsonData: sendingData,
                success: function (response) {
                    var result = Ext.decode(response.responseText).data;
                    var header = {
                        style: 'msgHeaderStyle'
                    };
                    if (Ext.isEmpty(result.failure)) {
                        window.location.href = '#/workspace/datacollection/issues';
                        self.getApplication().fireEvent('acknowledge', 'Issue closed');
                    } else {
                        var msges = [],
                            bodyItem = {},
                            formButtons = Ext.ComponentQuery.query('issues-close button');

                        Ext.each(formButtons, function (button) {
                            button.disable();
                        });

                        header.text = 'Failed to close issue ' + formPanel.recordTitle;
                        msges.push(header);
                        bodyItem.text = result.failure[0].reason;
                        bodyItem.style = 'msgItemStyle';
                        msges.push(bodyItem);
                        self.getApplication().fireEvent('isushowmsg', {
                            type: 'error',
                            msgBody: msges,
                            y: 10,
                            closeBtn: true,
                            btns: [
                                {
                                    text: 'Retry',
                                    hnd: function () {
                                        formPanel.enable();
                                        self.getApplication().fireEvent('closeissue');
                                    }
                                },
                                {
                                    text: 'Cancel',
                                    cls: 'isu-btn-link',
                                    hrefTarget: '',
                                    href: '#/workspace/datacollection/issues',
                                    hnd: function () {
                                    }
                                }
                            ],
                            listeners: {
                                close: {
                                    fn: function () {
                                        formPanel.enable();
                                    }
                                }
                            }
                        });
                        formPanel.disable();
                    }
                },
                failure: function (response) {
                    var result;
                    if (response != null) {
                        result = Ext.decode(response.responseText, true);
                    }
                    if (result !== null) {
                        Ext.widget('messagebox', {
                            buttons: [
                                {
                                    text: 'Close',
                                    action: 'cancel',
                                    handler: function(btn){
                                        btn.up('messagebox').hide()
                                    }
                                }
                            ]
                        }).show({
                            ui: 'notification-error',
                            title: result.error,
                            msg: result.message,
                            icon: Ext.MessageBox.ERROR
                        })

                    } else {
                        Ext.widget('messagebox', {
                            buttons: [
                                {
                                    text: 'Close',
                                    action: 'cancel',
                                    handler: function(btn){
                                        btn.up('messagebox').hide()
                                    }
                                }
                            ]
                        }).show({
                            ui: 'notification-error',
                            title: 'Failed to close issue: ' + record.data.title,
                            msg: 'Issue already closed',
                            icon: Ext.MessageBox.ERROR
                        })
                    }
                },

                callback: function () {
                    preloader.destroy();
                }
            });
        }
    }
});

Ext.define('Isu.view.workspace.issues.bulk.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.wizard',
    cls: 'wizard',
    autoHeight: true,
    border: false,
    layout: 'card',
    activeItemId: 0,
    inWizard: false,
    includeSubTitle: false,
    buttonAlign: 'left',
    bodyCls: 'isu-bulk-wizard-no-border',

    requires: [
        'Ext.layout.container.Card'
    ],

    listeners: {
        render: function () {
            if (this.includeSubTitle) {
                this.setTitle('<b>' + this.titlePrefix + ' &#62;</b>' + ' Step 1 of ' + this.items.length + ': ' + this.getActiveItem().title);
            } else {
                this.setTitle('<b>' + this.titlePrefix + ' &#62;</b>' + ' Step 1 of ' + this.items.length);
            }
            this.inWizard = true;
            this.setButtonsState(this);
            this.fireEvent('wizardstarted', this);
        },

        beforerender: function () {
            Ext.each(this.getLayout().getLayoutItems(), function (card) {
                card.preventHeader = true;
            });
        },

        wizardpagechange: function (wizard) {
            this.onWizardPageChangeEvent(wizard);
        }
    },

    onWizardPageChangeEvent: function (wizard) {
        if (this.includeSubTitle) {
            wizard.getActiveItem().preventHeader = true;
            wizard.setTitle('<b>' + wizard.titlePrefix + ' &#62;</b>' + ' Step ' + (wizard.activeItemId + 1) + ' of ' + this.items.length + ': ' + this.getActiveItem().title);
        } else {
            wizard.setTitle('<b>' + wizard.titlePrefix + ' &#62;</b>' + ' Step ' + (wizard.activeItemId + 1) + ' of ' + this.items.length);
        }

        this.setButtonsState(wizard);
    },

    setButtonsState: function (wizard) {
        var toolbar = wizard.down('toolbar[name="wizar-toolbar"]');
        var activeItem = wizard.getActiveItem();

        toolbar.child('#prev').setDisabled(activeItem.buttonsConfig.prevbuttonDisabled);
        toolbar.child('#next').setDisabled(activeItem.buttonsConfig.nextbuttonDisabled);
        toolbar.child('#cancel').setDisabled(activeItem.buttonsConfig.cancelbuttonDisabled);
        toolbar.child('#finish').setDisabled(activeItem.buttonsConfig.confirmbuttonDisabled);

        toolbar.child('#prev').setVisible(activeItem.buttonsConfig.prevbuttonVisible);
        toolbar.child('#next').setVisible(activeItem.buttonsConfig.nextbuttonVisible);
        toolbar.child('#cancel').setVisible(activeItem.buttonsConfig.cancelbuttonVisible);
        toolbar.child('#finish').setVisible(activeItem.buttonsConfig.confirmbuttonVisible);
    },

    onPrevButtonClick: function (prev) {
        var wizard = prev.up('wizard');
        if (!wizard.getForm().isValid()) {
            var fields = wizard.down('issues-assign-form').getForm().getFields();
            fields.each(function (field) {
                field.disable();
            });
        } else if (wizard.down('issues-assign-form')) {
            var assignValues = wizard.getForm().getValues(),
                assignRadio = wizard.down('issues-assign-form').down('radiogroup').items.items[0].getGroupValue();
            Ext.state.Manager.set('formAssignRadio', assignRadio);
            Ext.state.Manager.set('formAssignValues', assignValues);
        } else if (wizard.down('issues-close-form')) {
            var closeValues = wizard.getForm().getValues(),
                closeRadio = wizard.down('issues-close-form').down('radiogroup').items.items[0].getGroupValue();
            Ext.state.Manager.set('formCloseRadio', closeRadio);
            Ext.state.Manager.set('formCloseValues', closeValues);
        }
        wizard.getLayout().setActiveItem(--wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
        wizard.fireEvent('wizardprev', wizard);
    },

    onNextButtonClick: function (next) {
        var wizard = next.up('wizard');
        wizard.getLayout().setActiveItem(++wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
        wizard.fireEvent('wizardnext', wizard);
    },

    onConfirmButtonClick: function (finish) {
        var wizard = finish.up('wizard');
        if (!wizard.getForm().isValid()) {
            var invalidFields = 'Please correct the following errors before resumitting<br>';
            wizard.getForm().getFields().each(function (field) {
                if (!field.isValid()) {
                    invalidFields += '<br><b>' + field.getFieldLabel() + '</b>';
                    invalidFields += '<br>' + field.getErrors(field.getValue());
                    invalidFields += '<br>';
                }
            });
            Ext.Msg.show({
                scope: this,
                title: 'Wizard Invalid',
                msg: invalidFields,
                buttons: Ext.Msg.OK,
                icon: Ext.Msg.ERROR
            });
        } else {
            wizard.inWizard = false;
            wizard.fireEvent('wizardfinished', wizard);
        }
    },

    onCancelButtonClick: function (cancel) {
        var wizard = cancel.up('wizard');
        if (wizard.getForm().isDirty()) {
            Ext.Msg.show({
                scope: this,
                title: 'Cancelling Wizard',
                msg: 'All changes will be lost. Are you sure you want to cancel?',
                buttons: Ext.Msg.YESNO,
                icon: Ext.Msg.QUESTION,
                fn: function (buttonId, text, opt) {
                    switch (buttonId) {
                        case 'yes':
                            wizard.fireEvent('wizardcancelled', wizard);
                            break;
                        case 'no':
                            break;
                    }
                }
            });
        } else {
            wizard.fireEvent('wizardcancelled', wizard);
        }
    },

    getActiveItem: function () {
        return this.items.items[this.activeItemId];
    },

    getActiveItemId: function () {
        return this.activeItemId;
    },

    initComponent: function () {
        Ext.apply(this, {
            dockedItems: [
                {
                    itemId: 'toolbarbot',
                    xtype: 'toolbar',
                    name: 'wizar-toolbar',
                    dock: 'bottom',
                    border: false,
                    items: [
                        {
                            xtype: 'button',
                            text: 'Back',
                            itemId: 'prev',
                            action: 'prevWizard',
                            scope: this,
                            handler: this.onPrevButtonClick
                        },
                        {
                            xtype: 'button',
                            text: 'Next',
                            itemId: 'next',
                            action: 'nextWizard',
                            scope: this,
                            handler: this.onNextButtonClick

                        },
                        {
                            xtype: 'button',
                            text: 'Confirm',
                            itemId: 'finish',
                            action: 'finishWizard',
                            hidden: true,
                            scope: this,
                            handler: this.onConfirmButtonClick
                        },
                        {
                            xtype: 'button',
                            text: 'Cancel',
                            ui: 'link',
                            itemId: 'cancel',
                            action: 'cancelWizard',
                            scope: this,
                            handler: this.onCancelButtonClick
                        }
                    ]
                }
            ]
        });

        this.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-list',
    itemId: 'issues-list',
    store: 'Isu.store.Issues',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'Title',
                header: Uni.I18n.translate('general.title.title', 'ISU', 'Title'),
                xtype: 'templatecolumn',
                tpl: '<a href="#/workspace/datacollection/issues/{id}">{title}</a>',
                flex: 2
            },
            {
                itemId: 'dueDate',
                header: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                itemId: 'status',
                header: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'assignee',
                header: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee_type"><span class="isu-icon-{assignee_type} isu-assignee-type-icon"></span></tpl> {assignee_name}',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: 'Isu.view.workspace.issues.ActionMenu'
            }
        ]
    },
    dockedItems: [
        {
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
            displayMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} issues'),
            displayMoreMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} issues'),
            emptyMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.emptyMsg', 'ISU', 'There are no issues to display'),
            items: [
                '->',
                {
                    itemId: 'bulkAction',
                    xtype: 'button',
                    text: Uni.I18n.translate('general.title.bulkActions', 'ISU', 'Bulk action'),
                    action: 'bulkchangesissues',
                    hrefTarget: '',
                    href: '#/workspace/datacollection/bulkaction'
                }
            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('workspace.issues.pagingtoolbarbottom.itemsPerPage', 'ISU', 'Issues per page')
        }
    ],

    initComponent: function () {
        var store = this.store,
            pagingToolbarTop = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbartop';
            }),
            pagingToolbarBottom = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbarbottom';
            });

        pagingToolbarTop && (pagingToolbarTop.store = store);
        pagingToolbarBottom && (pagingToolbarBottom.store = store);

        this.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.bulk.IssuesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'issues-selection-grid',

    store: 'Isu.store.Issues',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'workspace.issues.bulk.IssuesSelectionGrid.counterText',
            count,
            'MDC',
            '{0} issues selected'
        );
    },

    allLabel: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allLabel', 'MDC', 'All issues'),
    allDescription: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allDescription', 'MDC', 'Select all issues (related to filters and grouping on the issues screen)'),

    selectedLabel: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedLabel', 'MDC', 'Selected issues'),
    selectedDescription: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedDescription', 'MDC', 'Select issues in table'),

    cancelHref: '#/search',

    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'Title',
                header: 'Title',
                xtype: 'templatecolumn',
                tpl: '{reason.name}<tpl if="device"> to {device.serialNumber}</tpl>',
                flex: 2
            },
            {
                itemId: 'dueDate',
                header: 'Due date',
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                itemId: 'status',
                header: 'Status',
                xtype: 'templatecolumn',
                tpl: '<tpl if="status">{status.name}</tpl>',
                width: 100
            },
            {
                itemId: 'assignee',
                header: 'Assignee',
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type} isu-assignee-type-icon"></span></tpl> {assignee.name}',
                flex: 1
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
        this.getBottomToolbar().setVisible(false);
    }
});

Ext.define('Isu.view.workspace.issues.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step1',
    title: 'Select issues',
    border: false,

    requires: [
        'Isu.view.workspace.issues.List',
        'Isu.util.FormErrorMessage',
        'Isu.view.workspace.issues.bulk.IssuesSelectionGrid'
    ],

    items: [
        {
            name: 'step1-errors',
            layout: 'hbox',
            hidden: true,
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message'
                }
            ]
        },
        {
            xtype: 'issues-selection-grid'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step2',
    title: 'Select action',
    border: false,

    requires: [
        'Ext.form.RadioGroup'
    ],

    items: [
        {
            xtype: 'panel',
            border: false,

            items: [
                {
                    itemId: 'radiogroupStep2',
                    xtype: 'radiogroup',
                    columns: 1,
                    vertical: true,
                    defaults: {
                        name: 'operation',
                        submitValue: false
                    },

                    items: [
                        { itemId: 'Assign', boxLabel: 'Assign issues', name: 'operation', inputValue: 'assign', checked: true },
                        { itemId: 'Close', boxLabel: 'Close issues', name: 'operation', inputValue: 'close'}
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step3',
    title: 'Action details',

    requires: [
        'Isu.view.workspace.issues.CloseForm',
        'Isu.view.workspace.issues.AssignForm'
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step4',
    title: 'Confirmation',
    bodyCls: 'isu-bulk-wizard-no-border',

    initComponent: function () {
        this.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step5',
    title: 'Status',
    bodyCls: 'isu-bulk-wizard-no-border',

    initComponent: function () {
        this.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.issues.bulk.BulkWizard', {
    extend: 'Isu.view.workspace.issues.bulk.Wizard',
    alias: 'widget.bulk-wizard',
    itemId: 'bulk-wizard',
    titlePrefix: 'Bulk action',
    includeSubTitle: true,

    requires: [
        'Isu.view.workspace.issues.bulk.Step1',
        'Isu.view.workspace.issues.bulk.Step2',
        'Isu.view.workspace.issues.bulk.Step3',
        'Isu.view.workspace.issues.bulk.Step4',
        'Isu.view.workspace.issues.bulk.Step5'
    ],

    header: {
        title: 'Wizard',
        ui: 'large',
        style: {
            padding: '15px'
        }
    },

    items: [
        {
            itemId: 'bulk-step1',
            xtype: 'bulk-step1',
            cls: 'bulk-step',
            buttonsConfig: {
                prevbuttonDisabled: true,
                nextbuttonDisabled: false,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: true,
                cancelbuttonVisible: true,
                confirmbuttonVisible: false
            }
        },
        {
            itemId: 'bulk-step2',
            xtype: 'bulk-step2',
            cls: 'bulk-step',
            buttonsConfig: {
                prevbuttonDisabled: false,
                nextbuttonDisabled: false,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: true,
                cancelbuttonVisible: true,
                confirmbuttonVisible: false
            }
        },
        {
            itemId: 'bulk-step3',
            xtype: 'bulk-step3',
            cls: 'bulk-step',
            buttonsConfig: {
                prevbuttonDisabled: false,
                nextbuttonDisabled: false,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: true,
                cancelbuttonVisible: true,
                confirmbuttonVisible: false
            }
        },
        {
            itemId: 'bulk-step4',
            xtype: 'bulk-step4',
            cls: 'bulk-step',
            buttonsConfig: {
                prevbuttonDisabled: false,
                nextbuttonDisabled: true,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: false,

                prevbuttonVisible: true,
                nextbuttonVisible: false,
                cancelbuttonVisible: true,
                confirmbuttonVisible: true
            }
        },
        {
            itemId: 'bulk-step5',
            xtype: 'bulk-step5',
            cls: 'bulk-step',
            buttonsConfig: {
                prevbuttonDisabled: true,
                nextbuttonDisabled: true,
                cancelbuttonDisabled: true,
                confirmbuttonDisabled: true,

                prevbuttonVisible: false,
                nextbuttonVisible: false,
                cancelbuttonVisible: false,
                confirmbuttonVisible: false
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    onCancelButtonClick: function (cancel) {
        var wizard = cancel.up('wizard');
        Ext.state.Manager.clear('formAssignValues');
        Ext.state.Manager.clear('formCloseValues');
        wizard.fireEvent('wizardcancelled', wizard);
    },

    onConfirmButtonClick: function (finish) {
        var wizard = finish.up('wizard');
        var docked = wizard.getDockedItems('toolbar[dock="bottom"]')[0];
        docked.setVisible(false);

        wizard.getLayout().setActiveItem(++wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
        wizard.fireEvent('wizardfinished', wizard);
    },

    onNextButtonClick: function (next) {
        var wizard = next.up('wizard'),
            functionName = 'processValidateOnStep' + (wizard.activeItemId + 1);
        if (this.processValidate(functionName, wizard)) {
            wizard.getLayout().setActiveItem(++wizard.activeItemId);
            wizard.fireEvent('wizardpagechange', wizard);
            wizard.fireEvent('wizardnext', wizard);
        }
    },

    processValidate: function (func, wizard) {
        if (func in this) {
            return this[func](wizard);
        } else {
            return true;
        }
    },

    processValidateOnStep1: function (wizard) {
        var issuesGrid = wizard.down('issues-selection-grid'),
            step1ErrorPanel = wizard.down('[name=step1-errors]');

        if (!issuesGrid.isAllSelected() && Ext.isEmpty(issuesGrid.view.getSelectionModel().getSelection())) {
            step1ErrorPanel.setVisible(true);
            return false;
        } else {
            step1ErrorPanel.setVisible(false);
            return true;
        }
    },

    processValidateOnStep3: function (wizard) {
        var assignForm = wizard.down('bulk-step3').down('issues-assign-form'),
            formErrorsPanel,
            activeRadioButton,
            comboBox;

        if (!Ext.isEmpty(assignForm)) {
            formErrorsPanel = assignForm.down('[name=form-errors]');
            formErrorsPanel.hide();
            //      formErrorsPanel.removeAll();
            activeRadioButton = assignForm.down('radiogroup').down('[checked=true]')
            comboBox = wizard.down('bulk-step3').down('issues-assign-form').down('combobox[name=' + activeRadioButton.inputValue + ']');
            if (Ext.isEmpty(comboBox.getValue())) {
                /*formErrorsPanel.add({
                 text: 'You must choose \'' + activeRadioButton.boxLabel + '\' before you can proceed'
                 });*/
                formErrorsPanel.setText('You must choose \'' + activeRadioButton.boxLabel + '\' before you can proceed');
                formErrorsPanel.show();
                return false;
            }
        }
        return true;
    }

});

Ext.define('Isu.view.workspace.issues.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    itemId: 'bulkNavigation',
    alias: 'widget.bulk-navigation',
    componentCls: 'isu-bulk-navigation',
    width: 200,
    jumpForward: true,
    items: [
        {
            itemId: 'SelectIssues',
            text: 'Select issues'
        },
        {
            itemId: 'SelectAction',
            text: 'Select action'
        },
        {
            itemId: 'actionDetails',
            text: 'Action details'
        },
        {   itemId: 'Confirmation',
            text: 'Confirmation'
        },
        {
            itemId: 'Status',
            text: 'Status'
        }
    ]

});

Ext.define('Isu.view.workspace.issues.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bulk-browse',
    itemId: 'bulk-browse',
    componentCls: 'isu-bulk-browse',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.workspace.issues.bulk.BulkWizard',
        'Isu.view.workspace.issues.bulk.Navigation'
    ],

    side: {
        itemId: 'Bulkpanel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'bulkNavigation',
                xtype: 'bulk-navigation'
            }
        ]
    },

    content: [
        {
            xtype: 'bulk-wizard',
            defaults: {
                cls: 'content-wrapper'
            }
        }
    ]
});

Ext.define('Isu.view.ext.button.IssuesGridAction', {
    extend: 'Ext.button.Split',
    cls: 'isu-grid-action-btn',
    menuAlign: 'tl-bl',
    menu: {
        xtype: 'menu',
        name: 'issueactionmenu',
        shadow: false,
        border: false,
        plain: true,
        cls: 'issue-action-menu',
        items: [
            {
                text: 'Assign'
            },
            {
                text: 'Close'
            }
        ]
    }
});

Ext.define('Isu.model.BulkChangeIssues', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'operation', type: 'string'},
        {name: 'status', type: 'string'},
        {name: 'comment', type: 'string'},
        {name: 'assignee', type: 'auto'}
    ],

    hasMany: {model: 'Isu.model.BulkIssues', name: 'issues'}
});

Ext.define('Isu.store.BulkChangeIssues', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.BulkChangeIssues',

    requires: [
        'Ext.data.proxy.SessionStorage'
    ],

    proxy: {
        type: 'sessionstorage',
        id  : 'bulkProxy'
    }
});

Ext.define('Isu.controller.BulkChangeIssues', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssuesGroups',
        'Isu.store.BulkChangeIssues'
    ],

    views: [
        'workspace.issues.bulk.Browse',
        'ext.button.IssuesGridAction',
        'Uni.view.button.SortItemButton'
    ],

    listeners: {
        retryRequest: function (wizard, failedItems) {
            this.setFailedBulkRecordIssues(failedItems);
            this.onWizardFinishedEvent(wizard);
        }
    },

    mixins: [
        'Isu.util.IsuGrid'
    ],

    refs: [
        {
            selector: 'bulk-browse bulk-navigation',
            ref: 'bulkNavigation'
        }
    ],

    init: function () {
        this.control({
            'bulk-browse bulk-wizard': {
                wizardnext: this.onWizardNextEvent,
                wizardprev: this.onWizardPrevEvent,
                wizardstarted: this.onWizardStartedEvent,
                wizardfinished: this.onWizardFinishedEvent,
                wizardcancelled: this.onWizardCancelledEvent
            },
            'bulk-browse bulk-navigation': {
                movetostep: this.setActivePage
            },
            'bulk-browse bulk-wizard bulk-step1 issues-selection-grid': {
                afterrender: this.onIssuesListAfterRender
            },
            'bulk-browse bulk-wizard bulk-step1 issues-list gridview': {
                refresh: this.setAssigneeTypeIconTooltip
            },
            'bulk-browse bulk-wizard bulk-step2 radiogroup': {
                change: this.onStep2RadiogroupChangeEvent,
                afterrender: this.getDefaultStep2Operation
            },
            'bulk-browse bulk-wizard bulk-step3 issues-close-form radiogroup': {
                change: this.onStep3RadiogroupCloseChangeEvent,
                afterrender: this.getDefaultCloseStatus
            },
            'bulk-browse bulk-step4': {
                beforeactivate: this.beforeStep4
            }
        });
    },

    showOverview: function () {
        var me = this,
            issuesStore = this.getStore('Isu.store.Issues'),
            issuesStoreRoxy = issuesStore.getProxy(),
            extraParamsModel = new Isu.model.ExtraParams(),
            widget;

        issuesStoreRoxy.extraParams = {};
        issuesStoreRoxy.setExtraParam('status', extraParamsModel.getDefaults().status);

        widget = Ext.widget('bulk-browse');
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    setActivePage: function (index) {
        var wizard = this.createdWizard;
        wizard.show();
        wizard.activeItemId = index - 1;
        wizard.getLayout().setActiveItem(wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
    },


    setFailedBulkRecordIssues: function (failedIssues) {
        var record = this.getBulkRecord(),
            previousIssues = record.get('issues'),
            leftIssues = [];

        Ext.each(previousIssues, function (issue) {
            if (Ext.Array.contains(failedIssues, issue.get('id'))) {
                leftIssues.push(issue);
            }
        });

        record.set('issues', leftIssues);
        record.commit();
    },

    onIssuesListAfterRender: function (grid) {
        var extraParamsModel = new Isu.model.ExtraParams();

        grid.mask();
        grid.store.load({
            params: {status: extraParamsModel.getDefaults().status},
            start: 0,
            limit: 99999,
            callback: function () {
                grid.unmask();
            }
        });
    },

    onBulkActionEvent: function () {
        var widget = Ext.widget('bulk-browse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onWizardPrevEvent: function (wizard) {
        var index = wizard.getActiveItemId();
        this.setBulkActionListActiveItem(wizard);
        this.getBulkNavigation().movePrevStep();
    },

    onWizardNextEvent: function (wizard) {
        var index = wizard.getActiveItemId();
        this.setBulkActionListActiveItem(wizard);
        var functionName = 'processNextOnStep' + index;
        this.processStep(functionName, wizard);
        this.getBulkNavigation().moveNextStep();
    },

    onWizardStartedEvent: function (wizard) {
        this.createdWizard = wizard;
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardFinishedEvent: function (wizard) {
        var me = this,
            step5panel = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step5'),
            record = me.getBulkRecord(),
            requestData = me.getRequestData(record),
            operation = record.get('operation'),
            requestUrl = '/api/isu/issue/' + operation,
            warnIssues = [],
            failedIssues = [];

        this.setBulkActionListActiveItem(wizard);

        var pb = Ext.create('Ext.ProgressBar', {width: '50%'});
        step5panel.removeAll(true);
        step5panel.add(
            pb.wait({
                interval: 50,
                increment: 20,
                text: (operation === 'assign' ? 'Assigning ' : 'Closing ') + requestData.issues.length + ' issue(s). Please wait...'
            })
        );

        Ext.Ajax.request({
            url: requestUrl,
            method: 'PUT',
            jsonData: requestData,
            success: function (response) {
                var obj = Ext.decode(response.responseText).data,
                    successCount = obj.success.length,
                    warnCount = 0,
                    failedCount = 0,
                    successMessage,
                    warnMessage,
                    failedMessage,
                    warnList = '',
                    failList = '';

                if (!Ext.isEmpty(obj.success)) {
                    switch (operation) {
                        case 'assign':
                            if (successCount > 0) {
                                successMessage = '<h3>Successfully assigned ' + successCount + (successCount > 1 ? ' issues' : ' issue')
                                    + ' to ' + record.get('assignee').title + '</h3><br>';
                            }
                            break;
                        case 'close':
                            if (successCount > 0) {
                                successMessage = '<h3>Successfully closed ' + successCount + (successCount > 1 ? ' issues' : ' issue') + '</h3><br>';
                            }
                    }
                }

                if (!Ext.isEmpty(obj.failure)) {
                    Ext.each(obj.failure, function (fail) {
                        switch (fail.reason) {
                            case 'Issue doesn\'t exist':
                                warnList += '<h4>' + fail.reason + ':</h4><ul style="list-style: none; padding-left: 1em;">';
                                Ext.each(fail.issues, function (issue) {
                                    warnCount += 1;
                                    warnIssues.push(issue.id);
                                    warnList += '<li>- <a href="javascript:void(0)">' + issue.title + '</a></li>';
                                });
                                warnList += '</ul>';
                                break;
                            default:
                                failList += '<h4>' + fail.reason + ':</h4><ul style="list-style: none; padding-left: 1em;">';
                                Ext.each(fail.issues, function (issue) {
                                    failedCount += 1;
                                    failedIssues.push(issue.id);
                                    failList += '<li>- <a href="javascript:void(0)">' + issue.title + '</a></li>';
                                });
                                failList += '</ul>';
                        }
                    });

                    switch (operation) {
                        case 'assign':
                            if (warnCount > 0) {
                                warnMessage = '<h3>Unable to assign ' + warnCount + (warnCount > 1 ? ' issues' : ' issue') + '</h3><br>' + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = '<h3>Failed to assign ' + failedCount + (failedCount > 1 ? ' issues' : ' issue') + '</h3><br>' + failList;
                            }
                            break;
                        case 'close':
                            if (warnCount > 0) {
                                warnMessage = '<h3>Unable to close ' + warnCount + (warnCount > 1 ? ' issues' : ' issue') + '</h3><br>' + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = '<h3>Failed to close ' + failedCount + (failedCount > 1 ? ' issues' : ' issue') + '</h3><br>' + failList;
                            }
                    }
                }

                step5panel.removeAll(true);

                if (successCount > 0) {
                    var successMsgParams = {
                        type: 'success',
                        msgBody: [
                            {html: successMessage}
                        ],
                        closeBtn: false
                    };
                    if (failedCount == 0) {
                        successMsgParams.btns = [
                            {text: "OK", hnd: function () {
                                step5panel.removeAll(true);
                                Ext.History.back();
                            }}
                        ];
                    }
                    var successPanel = Ext.widget('message-panel', successMsgParams);
                    successPanel.addClass('isu-bulk-message-panel');
                    step5panel.add(successPanel);
                }

                if (warnCount > 0) {
                    var warnMessageParams = {
                        type: 'attention',
                        msgBody: [
                            {html: warnMessage}
                        ],
                        btns: [],
                        closeBtn: false
                    };
                    var warnPanel = Ext.widget('message-panel', warnMessageParams);
                    warnPanel.addClass('isu-bulk-message-panel');
                    step5panel.add(warnPanel);
                }

                if (failedCount > 0) {
                    var failedMessageParams = {
                        type: 'error',
                        msgBody: [
                            {html: failedMessage}
                        ],
                        btns: [
                            {text: "Retry", hnd: function () {
                                me.fireEvent('retryRequest', wizard, failedIssues);
                            }},
                            {text: "Finish", hnd: function () {
                                Ext.History.back();
                            }}
                        ],
                        closeBtn: false
                    };
                    var failedPanel = Ext.widget('message-panel', failedMessageParams);
                    failedPanel.addClass('isu-bulk-message-panel');
                    step5panel.add(failedPanel);
                }
            },
            failure: function (response) {
                step5panel.removeAll(true);
                Ext.History.back();
            }
        });
    },

    getRequestData: function (bulkStoreRecord) {
        var requestData = {issues: []},
            operation = bulkStoreRecord.get('operation'),
            issues = bulkStoreRecord.get('issues');

        Ext.iterate(issues, function (issue) {
            requestData.issues.push(
                {
                    id: issue.get('id'),
                    version: issue.get('version')
                }
            );
        });

        switch (operation) {
            case 'assign':
                requestData.assignee = {
                    id: bulkStoreRecord.get('assignee').id,
                    type: bulkStoreRecord.get('assignee').type
                };
                break;
            case 'close':
                requestData.status = bulkStoreRecord.get('status');
                break;
        }

        requestData.comment = bulkStoreRecord.get('comment');

        return requestData;
    },

    onWizardCancelledEvent: function (wizard) {
        window.location.href = '#/workspace/datacollection/issues/'
    },

    setBulkActionListActiveItem: function (wizard) {
        var index = wizard.getActiveItemId();
    },

    processStep: function (func, wizard) {
        if (func in this) {
            this[func](wizard);
        }
    },

    getBulkRecord: function () {
        var bulkStore = Ext.getStore('Isu.store.BulkChangeIssues'),
            bulkRecord = bulkStore.getAt(0);

        if (!bulkRecord) {
            bulkStore.add({
                operation: 'assign'
            });
        }

        return bulkStore.getAt(0);
    },

    onStep2RadiogroupChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('operation', newValue.operation);
        record.commit();
    },

    onStep3RadiogroupCloseChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('status', newValue.status);
        record.set('statusName', radiogroup.getChecked()[0].boxLabel)
        record.commit();
    },

    getDefaultStep2Operation: function () {
        var formPanel = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step2').down('panel'),
            default_operation = formPanel.down('radiogroup').getValue().operation,
            record = this.getBulkRecord();
        record.set('operation', default_operation);
        record.commit();
    },

    getDefaultCloseStatus: function () {
        var formPanel = Ext.ComponentQuery.query('bulk-browse')[0].down('bulk-wizard').down('bulk-step3').down('issues-close-form'),
            default_status = formPanel.down('radiogroup').getValue().status,
            record = this.getBulkRecord();
        record.set('status', default_status);
        record.commit();
    },

    processNextOnStep1: function (wizard) {
        var record = this.getBulkRecord(),
            grid = wizard.down('bulk-step1').down('issues-selection-grid'),
            selection = grid.getSelectionModel().getSelection();

        if (grid.isAllSelected()) {
            selection = grid.store.data.items;
        }

        record.set('issues', selection);
        record.commit();
    },

    processNextOnStep2: function (wizard) {
        var record = this.getBulkRecord(),
            step3Panel = wizard.down('bulk-step3'),
            operation = record.get('operation'),
            view,
            widget;

        switch (operation) {
            case 'assign':
                view = 'issues-assign-form';
                break;
            case  'close':
                view = 'issues-close-form';
                break;
        }

        widget = Ext.widget(view);

        if (!Ext.isEmpty(widget.items.getAt(1))) {
            widget.items.getAt(1).margin = '0';
        }

        if (widget) {
            step3Panel.removeAll(true);
            step3Panel.add(widget);
        }
    },

    processNextOnStep3: function (wizard) {
        var formPanel = wizard.down('bulk-step3').down('form'),
            form = formPanel.getForm();

        if (form.isValid()) {
            var record = this.getBulkRecord(),
                step4Panel = wizard.down('bulk-step4'),
                operation = record.get('operation'),
                message, widget;

            switch (operation) {
                case 'assign':
                    var activeRadio = formPanel.down('radiogroup').down('radio[checked=true]').inputValue,
                        activeCombo = formPanel.down('combo[name=' + activeRadio + ']');
                    record.set('assignee', {
                        id: activeCombo.findRecordByValue(activeCombo.getValue()).data.id,
                        type: activeCombo.name,
                        title: activeCombo.rawValue
                    });
                    message = '<h3>Assign ' + record.get('issues').length + (record.get('issues').length > 1 ? ' issues' : ' issue') + ' to ' + record.get('assignee').title + '?</h3><br>'
                        + 'The selected issue(s) will be assigned to ' + record.get('assignee').title;
                    break;

                case 'close':
                    message = '<h3>Close ' + record.get('issues').length + (record.get('issues').length > 1 ? ' issues' : ' issue') + '?</h3><br>'
                        + 'The selected issue(s) will be closed with status "<b>' + record.get('statusName') + '</b>"';
                    break;
            }

            record.set('comment', formPanel.down('textarea').getValue().trim());

            widget = Ext.widget('container', {
                cls: 'isu-bulk-assign-confirmation-request-panel',
                html: message
            });

            step4Panel.removeAll(true);
            step4Panel.add(widget);
        }
    },

    beforeStep4: function () {
        if (this.getBulkRecord().get('operation') == 'assign') {
            var form = Ext.ComponentQuery.query('bulk-step3 issues-assign-form')[0].getForm();
            return !form || form.isValid();
        }
    }
});

Ext.define('Isu.view.workspace.issues.MessagePanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.message-panel',
    msgHeaderStyle: {
        fontSize: '16px',
        margin: '10 0 0 0'
    },
    msgItemStyle: {
        fontSize: '12px',
        margin: '5 0 0 10'
    },
    itemId: 'message-panel',
    autoShow: true,
    resizable: false,
    bodyBorder: false,
    shadow: false,
    animCollapse: true,
    border: false,
    header: false,
    cls: 'isu-msg-panel',
    margin: '2',
    defaults: {
        style: {
            opacity: 1
        }
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    types: {
        attention: {
            iconCls: 'isu-icon-attention isu-msg-attention-icon',
            colorCls: 'isu-msg-attention'
        },
        question: {
            iconCls: 'isu-icon-help isu-msg-question-icon',
            colorCls: 'isu-msg-question'
        },
        success: {
            iconCls: 'isu-icon-ok isu-msg-success-icon',
            colorCls: 'isu-msg-success'
        },
        error: {
            iconCls: 'isu-icon-attention isu-msg-error-icon',
            colorCls: 'isu-msg-error'
        },
        notify: {
            iconCls: '',
            colorCls: 'isu-msg-notify'
        }
    },

    items: [
        //MAIN BODY
        {
            xtype: 'panel',
            cls: 'isu-msg-panel',
            defaults: {
                opacity: 1
            },
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                // ICON
                {
                    itemId: 'msgIcon',
                    xtype: 'panel',
                    name: 'msgiconpanel',
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    cls: 'isu-msg-panel',
                    defaults: {
                        opacity: 1
                    }
                },
                // MESSAGE
                {
                    itemId : 'msgmessage',
                    xtype: 'panel',
                    name: 'msgmessagepanel',
                    cls: 'isu-msg-panel',
                    defaults: {
                        opacity: 1
                    },
                    layout: {
                        type: 'vbox',
                        align: 'left'
                    }
                },
                // CLOSE BTN
                {
                    itemId : 'closeBTN',
                    xtype: 'panel',
                    name: 'msgclosepanel',
                    cls: 'isu-msg-panel',
                    defaults: {
                        opacity: 1
                    },
                    layout: {
                        type: 'hbox'
                    }
                }
            ]
        },
        // BOTTOM BAR
        {
            xtype: 'panel',
            name: 'msgbottompanel',
            cls: 'isu-msg-panel',
            border: 0,
            defaults: {
                opacity: 1
            },
            padding: '0 0 0 40'
        }
    ]
})
;

Ext.define('Isu.view.workspace.issues.MessageWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.message-window',
    autoShow: true,
    resizable: false,
    bodyBorder: false,
    shadow: false,
    y: 10,
    animCollapse: true,
    border: false,
    cls: 'isu-msg-window',
    header: false,
    layout: {
        type: 'vbox',
        align: 'center'
    }
});

/*
 params = {
 type: ('error', 'question', 'notify', 'success', 'attention') ,
 msgBody: [
 {text: "some text", style: ('msgHeaderStyle', 'msgItemStyle')},
 {text: "", style: {}},
 {text: "", style: {}},
 {text: "", style: {}}
 ],
 btns: [
 {text: "button text", handler: function},
 {text: "button text", handler: function}
 ],
 closeBtn: (true/false),
 showTime: milliseconds
 }
 this.getApplication().fireEvent('isushowmsg', params); // add message to common message queue
 Ext.widget('message-panel', params) // create a message panel
 */


Ext.define('Isu.controller.MessageWindow', {
    extend: 'Ext.app.Controller',
    views: [
        'workspace.issues.MessagePanel',
        'workspace.issues.MessageWindow'
    ],
    init: function () {

        this.control({
            'message-panel': {
                afterrender: this.fillWindow
            }
        });
        this.getApplication().on('isushowmsg', this.showMsg);
    },

    randomDirection: function () {
        var direct = [
            Ext.Component.DIRECTION_TOP,
            Ext.Component.DIRECTION_BOTTOM,
            Ext.Component.DIRECTION_RIGHT,
            Ext.Component.DIRECTION_LEFT
        ];
        return direct[Math.floor(Math.random() * direct.length)];
    },


    showMsg: function (params) {
        var msgWindow;
        if (this.msgWindow == undefined) {
            msgWindow = Ext.widget('message-window');
            this.msgWindow = msgWindow;
        } else {
            msgWindow = this.msgWindow;
        }
        params.xtype = 'message-panel';
        msgWindow.add(params);
        msgWindow.center();
        msgWindow.setPosition(msgWindow.x, 50, false)
    },

    initMsgWindow: function (panel) {
        var cls = panel.types[panel.type],
            me = this;
        if (cls.colorCls) {
            panel.addCls(cls.colorCls);
        }
        panel.on('collapse', function (pan) {
            if (pan.isClosing) {
                pan.close();
            } else {
                pan.expand(true);
            }
        });

        panel.collapseClose = function () {
            panel.isClosing = true;
            panel.collapse(me.randomDirection(), true);
        };

        if (panel.showTime) {
            var runner = new Ext.util.TaskRunner(),
                task = runner.newTask({
                    interval: panel.showTime,
                    run: panel.collapseClose
                });
            task.start();
        }
    },

    fillMsgPanel: function (panel) {
        var msgPanel = Ext.ComponentQuery.query('panel[name=msgmessagepanel]', panel)[0],
            msgBody = [];
        Ext.Array.each(panel.msgBody, function (item) {
            item.xtype = 'label';
            item.style = panel[item.style];
            item.itemId = 'msgmessagepanel';
            msgBody.push(item)
        });
        msgPanel.add(msgBody);
    },

    fillIconPanel: function (panel) {
        var iconPanel = Ext.ComponentQuery.query('panel[name=msgiconpanel]', panel)[0],
            cls = panel.types[panel.type].iconCls;
        iconPanel.add({
            xtype: 'box',
            cls: cls
        });
    },

    fillClosePanel: function (panel) {
        var iconPanel = Ext.ComponentQuery.query('panel[name=msgclosepanel]', panel)[0];
        if (panel.closeBtn) {
            iconPanel.add({
                xtype: 'button',
                name: 'msgwindowclosebtn',
                iconCls: 'isu-icon-cancel-circled2',
                cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-medium',
                style: {
                    fontSize: '20px',
                    padding: '0px'
                },
                width: 28,
                height: 24,
                handler: function () {
                    panel.close()
                }
            });
        }
    },

    fillBottomPanel: function (panel) {
        var buttons = [],
            bottomPanel = Ext.ComponentQuery.query('panel[name=msgbottompanel]', panel)[0];
        Ext.Array.each(panel.btns, function (item) {
            item.xtype = 'button';
            item.margin = '0 0 0 5';
            item.handler = function(){
                item.hnd();
                panel.close();
            };
            buttons.push(item)
        });
        bottomPanel.add(buttons)
    },

    fillWindow: function (panel) {
        this.initMsgWindow(panel);
        this.fillMsgPanel(panel);
        this.fillIconPanel(panel);
        this.fillClosePanel(panel);
        this.fillBottomPanel(panel);
    }
})
;

Ext.define('Isu.view.administration.datacollection.issueassignmentrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Ext.grid.column.Action'
    ],
    alias: 'widget.issues-assignment-rules-list',
    store: 'Isu.store.AssignmentRules',
    height: 285,

    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                header: 'Description',
                dataIndex: 'description',
                tdCls: 'isu-grid-description',
                flex: 1
            },
            {
                header: 'Assign to',
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type} isu-assignee-type-icon"></span></tpl> {assignee.name}',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Isu.view.administration.datacollection.issueassignmentrules.ActionMenu'
            }
        ]
    },
    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top'
        }
    ],


    initComponent: function () {
        var self = this,
            store;

        self.callParent(arguments);

        store = self.getStore();

        self.onStoreLoad(store);

        store.on({
            load: {
                fn: self.onStoreLoad,
                scope: self
            }
        });

        store.load();
    },

    onStoreLoad: function (store) {
        var storeTotal = store.getCount();

        if (storeTotal) {
            this.setTotal(storeTotal);
        }
    },

    setTotal: function (total) {
        var self = this,
            gridTop;

        if (self) {
            gridTop = self.getDockedItems('toolbar[dock="top"]')[0];
            gridTop.removeAll();
            gridTop.add({
                xtype: 'component',
                html: total + ' rule' + (total > 1 ? 's' : '')
            });
        }
    }
});


Ext.define('Isu.view.administration.datacollection.issueassignmentrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issueassignmentrules.List',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.issue-assignment-rules-overview',

    content: {
        itemId: 'title',
        xtype: 'panel',
        ui: 'large',
        title: Uni.I18n.translate('issue.administration.assignment', 'ISU', 'Issue assignment rules'),
        items: {
            itemId: 'issues-rules-list',
            xtype: 'preview-container',
            grid: {
                xtype: 'issues-assignment-rules-list'
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('issueAssignment.empty.title', 'ISU', 'No issue assignment rules found'),
                reasons: [
                    Uni.I18n.translate('issueAssignment.empty.list.item', 'ISU', 'No issue assignment rules have been defined yet.')
                ]
            },
            previewComponent: null
        }
    }
});

Ext.define('Isu.view.ext.button.GridAction', {
    extend: 'Ext.button.Button',
    alias: 'widget.grid-action',
    cls: 'isu-grid-action-btn',
    menuAlign: 'tl-bl'

});

Ext.define('Isu.view.administration.datacollection.issueassignmentrules.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.rule-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [

    ]
});

Ext.define('Isu.model.AssignmentRules', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            name: 'assignee',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'int'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/assign',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.AssignmentRules', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.AssignmentRules',
    pageSize: 100,
    autoLoad: false
});

Ext.define('Isu.controller.IssueAssignmentRules', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.AssignmentRules'
    ],
    views: [
        'administration.datacollection.issueassignmentrules.Overview',
        'ext.button.GridAction',
        'administration.datacollection.issueassignmentrules.ActionMenu'
    ],

    mixins: {
            isuGrid: 'Isu.util.IsuGrid'
    },

    init: function () {
        this.control({
            'issue-assignment-rules-overview issues-assignment-rules-list gridview': {
                refresh: this.onGridRefresh
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('issue-assignment-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onGridRefresh: function (grid) {
        this.setAssigneeTypeIconTooltip(grid);
        this.setDescriptionTooltip(grid);
    }
});

Ext.define('Isu.view.administration.datacollection.issuecreationrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-creation-rules-list',
    store: 'Isu.store.CreationRule',
    columns: {
        items: [
            {
                itemId: 'Name',
                header: Uni.I18n.translate('general.title.name', 'ISU', 'Name'),
                dataIndex: 'name',
                tdCls: 'isu-grid-description',
                flex: 1
            },
            {
                itemId: 'templateColumn',
                header: Uni.I18n.translate('general.title.ruleTemplate', 'ISU', 'Rule template'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="template">{template.name}</tpl>',
                tdCls: 'isu-grid-description',
                flex: 1
            },
            {
                itemId : 'issueType',
                header: Uni.I18n.translate('general.title.issueType', 'ISU', 'Issue type'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="issueType">{issueType.name}</tpl>',
                tdCls: 'isu-grid-description',
                flex: 1
            },
            {   itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: 'Isu.view.administration.datacollection.issuecreationrules.ActionMenu'
            }
        ]
    },
    dockedItems: [
        {
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
            displayMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} issue creation rules'),
            displayMoreMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} issue creation rules'),
            emptyMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.emptyMsg', 'ISU', 'There are no issue creation rules to display'),
            items: [
                '->',
                {
                    itemId: 'createRule',
                    xtype: 'button',
                    text: Uni.I18n.translate('administration.issueCreationRules.add', 'ISU', 'Create rule'),
                    href: '#/administration/creationrules/add',
                    action: 'create'
                }
            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbarbottom.itemsPerPage', 'ISU', 'Issue creation rules per page')
        }
    ],

    initComponent: function () {
        var store = this.store,
            pagingToolbarTop = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbartop';
            }),
            pagingToolbarBottom = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbarbottom';
            });

        pagingToolbarTop && (pagingToolbarTop.store = store);
        pagingToolbarBottom && (pagingToolbarBottom.store = store);

        this.callParent(arguments);
    }
});

Ext.define('Isu.view.administration.datacollection.issuecreationrules.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.creation-rule-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit',
            text: 'Edit',
            action: 'edit'
        },
        {
            itemId: 'delete',
            text: 'Delete',
            action: 'delete'
        }
    ]
});

Ext.define('Isu.view.administration.datacollection.issuecreationrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.administration.datacollection.issuecreationrules.ActionMenu'
    ],
    alias: 'widget.issue-creation-rules-item',
    title: 'Details',
    itemId: 'issue-creation-rules-item',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'creation-rule-action-menu'
            }
        }
    ],
    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.title.name', 'ISU', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.ruleTemplate', 'ISU', 'Rule template'),
                        name: 'template_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.issueType', 'ISU', 'Issue type'),
                        name: 'issueType_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.issueReason', 'ISU', 'Issue reason'),
                        name: 'reason_name'
                    },

                    {
                        fieldLabel: Uni.I18n.translate('general.title.dueIn', 'ISU', 'Due in'),
                        name: 'due_in'
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.title.created', 'ISU', 'Created'),
                        name: 'creationDate',
                        renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.lastModified', 'ISU', 'Last modified'),
                        name: 'modificationDate',
                        renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                    }
                ]
            }
        ]
    }
});

Ext.define('Isu.view.administration.datacollection.issuecreationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issuecreationrules.List',
        'Isu.view.administration.datacollection.issuecreationrules.Item'
    ],
    alias: 'widget.issue-creation-rules-overview',
    itemId: 'creation-rules-overview',
    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    itemId: 'pageTitle',
                    title: 'Issue creation rules',
                    ui: 'large',
                    margin: '0 0 20 0'
                },
                {
                    itemId: 'creation-rules-list',
                    xtype: 'issues-creation-rules-list',
                    margin: '0 15 20 0'
                },
                {
                    itemId: 'creation-rules-item',
                    xtype: 'issue-creation-rules-item',
                    margin: '0 15 0 0'
                }
            ]
        }
    ]
});

Ext.define('Isu.view.administration.datacollection.issuecreationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issue-creation-rules-overview',
    itemId: 'creation-rules-overview',

    requires: [
        'Isu.view.administration.datacollection.issuecreationrules.List',
        'Isu.view.administration.datacollection.issuecreationrules.Item',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('administration.issueCreationRules.title', 'ISU', 'Issue creation rules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        itemId: 'creation-rules-list',
                        xtype: 'issues-creation-rules-list'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('administration.issueCreationRules.empty.title', 'ISU', 'No issue creation rules found'),
                        reasons: [
                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item1', 'ISU', 'No issue creation rules have been defined yet.'),
                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item2', 'ISU', 'No issue creation rules comply to the filter.')
                        ],
                        stepItems: [
                            {
                                itemId: 'createRule',
                                text: Uni.I18n.translate('administration.issueCreationRules.add', 'ISU', 'Create rule'),
                                href: '#/administration/creationrules/add',
                                action: 'create'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'issue-creation-rules-item'
                    }
                }
            ]
        }
    ]
});

Ext.define('Isu.model.CreationRule', {
    extend: 'Ext.data.Model',
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'parameters',
            type: 'auto'
        },
        {
            name: 'comment',
            type: 'string'
        },
        {
            name: 'creationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'modificationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            type: 'int'
        },
        {
            name: 'template',
            type: 'auto'
        },
        {
            name: 'issueType',
            type: 'auto'
        },
        {
            name: 'reason',
            type: 'auto'
        },
        {
            name: 'dueIn',
            type: 'auto'
        },
        {
            name: 'title',
            mapping: 'name'
        },
        {
            name: 'issueType_name',
            mapping: 'issueType.name'
        },
        {
            name: 'reason_name',
            mapping: 'reason.name'
        },
        {
            name: 'template_name',
            mapping: 'template.name'
        },
        {
            name: 'due_in',
            mapping: function (data) {
                var dueIn = '';

                if (data.dueIn && data.dueIn.number) {
                    dueIn =  data.dueIn.number + ' ' + data.dueIn.type;
                }

                return dueIn;
            }
        }
    ],

    associations: [
        {
            name: 'actions',
            type: 'hasMany',
            model: 'Isu.model.CreationRuleAction',
            associationKey: 'actions'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/creationrules',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.CreationRule', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRule',
    pageSize: 10,
    autoLoad: false
});

Ext.define('Isu.controller.IssueCreationRules', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.CreationRule'
    ],
    views: [
        'Isu.view.administration.datacollection.issuecreationrules.Overview',
        'Isu.view.ext.button.GridAction',
        'Isu.view.administration.datacollection.issuecreationrules.ActionMenu',
        'Isu.view.workspace.issues.MessagePanel'
    ],

    mixins: {
        isuGrid: 'Isu.util.IsuGrid'
    },

    refs: [
        {
            ref: 'page',
            selector: 'issue-creation-rules-overview'
        },
        {
            ref: 'itemPanel',
            selector: 'issue-creation-rules-overview issue-creation-rules-item'
        },
        {
            ref: 'rulesGridPagingToolbarTop',
            selector: 'issue-creation-rules-overview issues-creation-rules-list pagingtoolbartop'
        }
    ],

    init: function () {
        this.control({
            'issue-creation-rules-overview issues-creation-rules-list': {
                select: this.loadGridItemDetail
            },
            'issue-creation-rules-overview issues-creation-rules-list gridview': {
                refresh: this.setAssigneeTypeIconTooltip
            },
            'issues-creation-rules-list uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'creation-rule-action-menu': {
                click: this.chooseAction
            },
            'issue-creation-rules-overview button[action=create]': {
                click: this.createRule
            }
        });

        this.gridItemModel = this.getModel('Isu.model.CreationRule');
    },

    showOverview: function () {
        var widget = Ext.widget('issue-creation-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    chooseAction: function (menu, item) {
        var action = item.action;
        var id = menu.record.getId();
        var router = this.getController('Uni.controller.history.Router');

        switch (action) {
            case 'delete':
                this.showDeleteConfirmation(menu.record);
                break;
            case 'edit':
                router.getRoute('administration/creationrules/edit').forward({id: id});
                break;
        }
    },

    createRule: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/creationrules/add').forward();
    },

    showDeleteConfirmation: function (rule) {
        var self = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.msg', 'ISU', 'This issue creation rule will disappear from the list.<br>Issues will not be created automatically by this rule.'),
            title: Ext.String.format(Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.title', 'ISU', 'Delete rule "{0}"?'), rule.get('name')),
            config: {
                me: self,
                rule: rule
            },
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        self.deleteRule(rule);
                        break;
                    case 'cancel':
                        this.close();
                        break;
                }
            }
        });
    },

    deleteRule: function (rule) {
        var self = this,
            store = this.getStore('Isu.store.CreationRule'),
            page = this.getPage();

        page.setLoading('Removing...');
        rule.destroy({
            params: {
                version: rule.get('version')
            },
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.response.status == 204) {
                    store.loadPage(1);
                    self.getApplication().fireEvent('acknowledge', Uni.I18n.translate('administration.issueCreationRules.deleteSuccess.msg', 'ISU', 'Issue creation rule deleted'));
                }
            }
        });
    }
});

Ext.define('Isu.view.workspace.issues.Form', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issue-form',
    layout: 'column',
    itemId: 'issue-detailed-form',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    ui: 'medium',

    items: [
        {
            items: [
                {
                    itemId: '_reason',
                    xtype: 'displayfield',
                    fieldLabel: 'Reason',
                    name: 'reason_name'
                },
                {
                    itemId: '_customer',
                    xtype: 'displayfield',
                    fieldLabel: 'Customer',
                    name: 'customer'
                },
                {
                    itemId: '_location',
                    xtype: 'displayfield',
                    fieldLabel: 'Location',
                    name: 'service_location'
                },
                {
                    itemId: '_usagepoint',
                    xtype: 'displayfield',
                    fieldLabel: 'Usage point',
                    name: 'usage_point'
                },
                {
                    itemId: '_devicename',
                    xtype: 'displayfield',
                    fieldLabel: 'Device',
                    name: 'devicelink'
                }
            ]
        },
        {
            items: [
                {
                    itemId: '_status',
                    xtype: 'displayfield',
                    fieldLabel: 'Status',
                    name: 'status_name'
                },
                {
                    itemId: '_dueDate',
                    xtype: 'displayfield',
                    fieldLabel: 'Due date',
                    name: 'dueDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y')
                },
                {
                    itemId: '_assignee',
                    xtype: 'displayfield',
                    fieldLabel: 'Assignee',
                    name: 'assignee_name'
                },
                {
                    itemId: '_creationDate',
                    xtype: 'displayfield',
                    fieldLabel: 'Creation date',
                    name: 'creationDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                },
                {
                    itemId: '_serviceCat',
                    xtype: 'displayfield',
                    fieldLabel: 'Service category',
                    name: 'service_category'
                }
            ]
        }
    ]
});

Ext.define('Isu.view.ext.button.Action', {
    extend: 'Ext.button.Split',
    alias: 'widget.action-btn',
    cls: 'isu-action-button-inactive',
    menuActiveCls: 'isu-action-button-active',
    iconCls: 'isu-action-icon',
    menuAlign: 'tr-br?',
    listeners: {
        click: {
            fn: function (btn, e) {
                btn.showMenu();
            }
        },
        menushow: {
            fn: function (btn, menu) {
                btn.removeCls('isu-action-button-inactive');
            }
        },
        menuhide: {
            fn: function (btn, menu) {
                btn.addCls('isu-action-button-inactive');
            }
        }
    }
});

Ext.define('Isu.model.IssueComments', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
        {
            name: 'author',
            type: 'auto'
        },
        {
            name: 'comment',
            type: 'string'
        },
        {
            name: 'creationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            type: 'int'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/isu/issue/{issue_id}/comments',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.IssueComments', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueComments',
    autoLoad: false,

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'data'
        }
    },

    sorters: [{
        sorterFn: function(o1, o2){
            return o1.get('creationDate') > o2.get('creationDate')
        }
    }]
});

Ext.define('Isu.view.workspace.issues.comment.AddForm', {
    extend: 'Ext.form.Panel',
    title: 'Add comment',
    alias: 'widget.comment-add-form',
    layout: 'fit',
    items: {
        itemId: 'comment-area',
        xtype: 'textareafield',
        label: 'comment',
        name: 'comment'
    },

    bbar: {
        layout: {
            type: 'hbox',
            align: 'left'
        },
        items: [
            {
                itemId: 'add',
                text: 'Add',
                ui: 'action',
                action: 'send',
                disabled: true
            },
            {
                itemId: 'cancel',
                text: 'Cancel',
                action: 'cancel',
                ui: 'link'
            }
        ]
    }
});

Ext.define('Isu.view.workspace.issues.comment.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.Action',
        'Isu.store.IssueComments',
        'Isu.view.workspace.issues.comment.AddForm'
    ],
    alias: 'widget.issue-comments',
    title: 'Comments',
    ui: 'medium',
    buttonAlign: 'left',
    items: [
        {
            itemId: 'dataview',
            xtype: 'dataview',
            title: 'User Images',
            deferEmptyText: false,
            emptyText: '<h3>There are no comments yet on this issue </h3>',
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '<p><span class="isu-icon-USER"></span><b>{author.name}</b> added a comment - {[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</p>',
                '<p>{comment}</p>',
                '</tpl>',
                {
                    formatCreationDate: function (date) {
                        date = Ext.isDate(date) ? date : new Date(date);
                        return Ext.Date.format(date, 'M d, Y (H:i)');
                    }
                }
            ),
            header: 'Name',
            dataIndex: 'name'
        },
        {
            itemId: 'comment-add-form',
            xtype: 'comment-add-form',
            hidden: true
        }
    ],

    afterRender: function(){
        var queryStrings = Ext.util.History.getToken(),
            index = queryStrings.lastIndexOf("/"),
            urlStr = queryStrings.substring(index + 1);
        if (urlStr == 'addcomment') {
            this.child('toolbar').child('#Add').hide();
            this.child('#comment-add-form').show()
        }
        this.callParent(arguments)
    },

    buttons: [
        {
            itemId: 'Add',
            ui: 'action',
            text: 'Add comment',
            action: 'add'
        }
    ]
});

Ext.define('Isu.view.workspace.issues.DetailOverview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.workspace.issues.Form',
        'Isu.view.workspace.issues.comment.List'
    ],
    alias: 'widget.issue-detail-overview',
    title: 'Issue detail overview',

    content: [
        {
            ui: 'large',
            items: [
                {
                    itemId: 'detailedPage',
                    xtype: 'issue-form'
                },
                {
                    itemId: 'issue-comments',
                    xtype: 'issue-comments'
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'top',
                    defaultButtonUI: 'link',
                    rtl: false,
                    items: [
                        {
                            xtype: 'tbfill'
                        },
                        {
                            text: 'Previous',
                            action: 'prev'
                        },
                        {
                            text: 'Next',
                            action: 'next'
                        }
                    ]
                }
            ]
        }
    ]
});

Ext.define('Isu.controller.IssueDetail', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssueComments'
    ],

    views: [
        'workspace.issues.DetailOverview'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issue-detail-overview'
        },
        {
            ref: 'detailPanel',
            selector: 'issue-detail-overview issue-detail'
        },
        {
            ref: 'commentsPanel',
            selector: 'issue-detail-overview issue-comments dataview'
        },
        {
            ref: 'commentForm',
            selector: 'issue-detail-overview issue-comments comment-add-form'
        },
        {
            ref: 'commentInput',
            selector: 'issue-detail-overview issue-comments comment-add-form textareafield'
        },
        {
            ref: 'addCommentButton',
            selector: 'issue-detail-overview issue-comments button[action=add]'
        },
        {
            ref: 'sendCommentButton',
            selector: 'issue-detail-overview form button[action=send]'
        }
    ],

    init: function () {
        this.control({
            'issue-detail-overview issue-comments button[action=add]': {
                click: this.showCommentForm
            },
            'issue-detail-overview form button[action=cancel]': {
                click: this.hideCommentForm
            },
            'issue-detail-overview form button[action=send]': {
                click: this.addComment
            },
            'issue-detail-overview form textareafield': {
                change: this.validateCommentForm
            }
        });
    },

    showOverview: function (id, showCommentForm) {
        var self = this,
            widget = Ext.widget('issue-detail-overview'),
            issueDetailModel = self.getModel('Isu.model.Issues'),
            form = widget.down('issue-form');

        showCommentForm && self.showCommentForm();
        issueDetailModel.load(id, {
            success: function (record) {
                form.loadRecord(record);
                form.setTitle(record.get('title'));

                var store = record.comments();

                // todo: this is dirty solution, rewrite in to the more solid one
                store.getProxy().url = store.getProxy().url.replace('{issue_id}', record.getId());
                store.clearFilter();

                store.proxy.url = '/api/isu/issue/' + id + '/comments';
                store.load();
                self.getApplication().fireEvent('changecontentevent', widget);
                self.getCommentsPanel().bindStore(store);
                self.setNavigationButtons(record);
            },
            failure: function () {
                window.location.href = '#/workspace/datacollection/issues';
            }
        });
    },

    showCommentForm: function (btn) {
        this.getCommentForm().show();
        this.getCommentInput().focus();
        btn.hide();
    },

    hideCommentForm: function () {
        var form = this.getCommentForm(),
            button = this.getAddCommentButton();

        form.down('textareafield').reset();
        form.hide();
        button.show();
    },

    validateCommentForm: function (form, newValue) {
        var sendBtn = this.getSendCommentButton();
        sendBtn.setDisabled(!newValue.trim().length);
    },

    addComment: function () {
        var self = this,
            commentsPanel = self.getCommentsPanel(),
            commentsStore = commentsPanel.getStore()
            ;

        var store = commentsPanel.getStore();
        store.add(self.getCommentForm().getValues());
        store.sync();

        self.hideCommentForm();
    },

    setNavigationButtons: function (model) {
        var prevBtn = this.getPage().down('[action=prev]'),
            nextBtn = this.getPage().down('[action=next]'),
            issueStore = this.getStore('Isu.store.Issues'),
            currentIndex = issueStore.indexOf(model),
            router = this.getController('Uni.controller.history.Router'),
            prevIndex,
            nextIndex;

        if (currentIndex != -1) {
            currentIndex && (prevIndex = currentIndex - 1);
            (issueStore.getCount() > (currentIndex + 1)) && (nextIndex = currentIndex + 1);

            if (prevIndex || prevIndex == 0) {
                prevBtn.on('click', function () {
                    router.getRoute('workspace/datacollection/issues/view').forward({id: issueStore.getAt(prevIndex).getId()});
                });
            } else {
                prevBtn.setDisabled(true);
            }

            if (nextIndex) {
                nextBtn.on('click', function () {
                    router.getRoute('workspace/datacollection/issues/view').forward({id: issueStore.getAt(nextIndex).getId()});
                });
            } else {
                nextBtn.setDisabled(true);
            }

            prevBtn.show();
            nextBtn.show();
        } else {
            prevBtn.hide();
            nextBtn.hide();
        }
    }
});

Ext.define('Isu.controller.AdministrationDataCollection', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
    ]
});

Ext.define('Isu.view.workspace.issues.component.UserCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.issues-user-combo',
    store: 'Isu.store.Users',
    displayField: 'name',
    valueField: 'id',
    triggerAction: 'query',
    queryMode: 'remote',
    queryParam: 'like',
    allQuery: '',
    lastQuery: '',
    queryDelay: 100,
    minChars: 0,
    disableKeyFilter: true,
    queryCaching: false,
    typeAhead: true,
    anchor: '100%',
    forceSelection: true,
    formBind: false,
    emptyText: 'select user',
    listConfig: {
        getInnerTpl: function(displayField) {
            return '<img src="../../apps/isu/resources/images/icons/USER.png"/> {' + displayField + '}';
        }
    }
});



Ext.define('Isu.util.CreatingControl', {
    requires: [
        'Isu.view.workspace.issues.component.UserCombo'
    ],
    createControl: function (obj) {
        var control = false;

        switch (obj.control.xtype.toLowerCase()) {
            case 'textfield':
                control = Ext.isEmpty(obj.suffix) ? this.createTextField(obj) : this.suffixAppender(this.createTextField, obj.suffix);
                break;
            case 'numberfield':
                control = Ext.isEmpty(obj.suffix) ? this.createNumberField(obj) : this.suffixAppender(this.createNumberField(obj), obj.suffix);
                break;
            case 'combobox':
                control = Ext.isEmpty(obj.suffix) ? this.createCombobox(obj) : this.suffixAppender(this.createCombobox(obj), obj.suffix);
                break;
            case 'textarea':
                control = Ext.isEmpty(obj.suffix) ? this.createTextArea(obj) : this.suffixAppender(this.createTextArea(obj), obj.suffix);
                break;
            case 'emaillist':
                control = Ext.isEmpty(obj.suffix) ? this.createEmailList(obj) : this.suffixAppender(this.createEmailList(obj), obj.suffix);
                break;
            case 'usercombobox':
                control = Ext.isEmpty(obj.suffix) ? this.createUserCombobox(obj) : this.suffixAppender(this.createUserCombobox(obj), obj.suffix);
                break;
            case 'trendperiodcontrol':
                control = this.createTrendPeriodControl(obj);
                break;
        }

        return control;
    },

    createTextField: function (obj) {
        var textField = {
            xtype: 'textfield',
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required,
            formBind: false
        };

        obj.constraint.max && (textField.maxLength = obj.constraint.max);
        obj.constraint.min && (textField.minLength = obj.constraint.min);
        obj.constraint.regexp && (textField.regex = new RegExp(obj.constraint.regexp));
        obj.defaultValue && (textField.value = obj.defaultValue);
        obj.help && (textField.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (textField.dependOn = obj.dependOn);

        return textField;
    },

    createNumberField: function (obj) {
        var numberField = {
            xtype: 'numberfield',
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required,
            formBind: false
        };

        obj.constraint.max && (numberField.maxValue = obj.constraint.max);
        obj.constraint.min && (numberField.minValue = obj.constraint.min);
        obj.defaultValue && (numberField.value = obj.defaultValue);
        obj.help && (numberField.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (numberField.dependOn = obj.dependOn);

        return numberField;
    },

    createCombobox: function (obj) {
        var comboboxStore = Ext.create('Ext.data.Store', {
                fields: ['id', 'title'],
                data: obj.defaultValues
            }),
            combobox = {
                xtype: 'combobox',
                name: obj.key,
                fieldLabel: obj.label,
                allowBlank: !obj.constraint.required,
                required: obj.constraint.required,
                store: comboboxStore,
                queryMode: 'local',
                displayField: 'title',
                valueField: 'id',
                editable: false,
                formBind: false
            };

        obj.defaultValue && (combobox.value = obj.defaultValue.id);
        obj.help && (combobox.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (combobox.dependOn = obj.dependOn);

        return combobox;
    },

    createTextArea: function (obj) {
        var textareafield = {
            xtype: 'textareafield',
            itemId: 'emailBody',
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required,
            height: 150,
            formBind: false
        };

        obj.constraint.max && (textareafield.maxLength = obj.constraint.max);
        obj.constraint.min && (textareafield.minLength = obj.constraint.min);
        obj.constraint.regexp && (textareafield.regex = new RegExp(obj.constraint.regexp));
        obj.defaultValue && (textareafield.value = obj.defaultValue);
        obj.help && (textareafield.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (textareafield.dependOn = obj.dependOn);

        return textareafield;
    },

    createEmailList: function (obj) {
        var emailList = {
            xtype: 'textarea',
            itemId: 'emailList',
            name: obj.key,
            height: 150,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required,
            fieldLabel: obj.label,
            emptyText: 'user@example.com',
            regex: /^((([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z?]{2,5}){1,25})*(\n?)*)*$/,
            regexText: 'This field should contains one e-mail address per line',
            formBind: false
        };

        obj.constraint.max && (emailList.maxLength = obj.constraint.max);
        obj.constraint.min && (emailList.minLength = obj.constraint.min);
        obj.help && (emailList.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (emailList.dependOn = obj.dependOn);

        return emailList;
    },

    createUserCombobox: function (obj) {
        var userCombobox = {
            xtype: 'issues-user-combo',
            itemId: 'userCombo',
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required
        };

        obj.help && (userCombobox.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (userCombobox.dependOn = obj.dependOn);

        return userCombobox;
    },

    createTrendPeriodControl: function (obj) {
        var trendPeriodControl = {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                fieldLabel: obj.label,
                name: obj.key,
                required: obj.constraint.required,
                items: []
            },
            trendPeriod = this.createNumberField(obj),
            trendPeriodUnit = this.createCombobox(obj.control.unitParameter);

        delete trendPeriod.fieldLabel;
        delete trendPeriod.required;
        trendPeriod.width = 150;
        trendPeriod.margin = '0 10 0 0' ;
        trendPeriodUnit.flex = 1;
        trendPeriodControl.items.push(trendPeriod, trendPeriodUnit);

        return trendPeriodControl;
    },

    suffixAppender: function (field, suffix) {
        field.columnWidth = 1;
        return {
            xtype: 'fieldcontainer',
            layout: 'column',
            name: field.name,
            defaults: {
                labelWidth: 150,
                anchor: '100%',
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [ field, { xtype: 'displayfield', margin: '0 0 0 5', submitValue: false, value: suffix } ]
        };
    }
});

Ext.define('Isu.view.workspace.issues.NotifySend', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.notify-user',
    content: [
        {
            items: [
                {
                    xtype: 'panel',
                    ui: 'large',
                    itemId: 'notifyPanel'
                },
                {
                    xtype: 'form',
                    width: '40%',
                    defaults: {
                        labelWidth: 160,
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            itemId: 'errors',
                            name: 'errors',
                            layout: 'hbox',
                            margin: '0 0 20 100',
                            hidden: true,
                            defaults: {
                                xtype: 'container',
                                cls: 'isu-error-panel'
                            }
                        }
                    ],
                    buttons: [
                        {
                            itemId: 'notifySend',
                            action: 'notifySend',
                            ui: 'action'
                        },
                        {
                            text: 'Cancel',
                            action: 'cancel',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/workspace/datacollection/issues';
                                    }
                                }
                            }
                        }
                    ]
                }
            ]
        }
    ]
});




Ext.define('Isu.model.Actions', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'text'
        },
        {
            name: 'issueType',
            type: 'text'
        },
        {
            name: 'parameters',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/actions',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.Actions', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    model: 'Isu.model.Actions',
    autoLoad: false,

    listeners: {
        beforeload: function () {
            this.getProxy().setExtraParam('issueType', 'datacollection');
        }
    }
});

Ext.define('Isu.controller.NotifySend', {
    extend: 'Ext.app.Controller',
    actionId: null,
    issId: null,
    views: [
        'workspace.issues.NotifySend'
    ],

    stores: [
        'Actions'
    ],

    refs: [
        {
            ref: 'notifyView',
            selector: 'notify-user'
        }
    ],

    mixins: [
        'Isu.util.CreatingControl'
    ],

    init: function () {
        this.control({
            'notify-user button[action=notifySend]': {
                click: this.submit
            }
        });
    },

    showNotifySend: function (id) {
        var self = this,
            widget = Ext.widget('notify-user'),
            view = self.getNotifyView();
        self.issId = id;
        self.getApplication().fireEvent('changecontentevent', widget);
        self.getStore('Actions').load({
            callback: function (records) {
                var str = window.location.href;
                if (str.match(/notify/) !== null) {
                    view.down('#notifyPanel').setTitle('Notify user');
                    view.down('#notifySend').setText('Notify');
                    self.notifyUser(records, view);
                } else {
                    view.down('#notifyPanel').setTitle('Send to inspect');
                    view.down('#notifySend').setText('Send to inspect');
                    self.sendSomeone(records, view);
                }
            }
        })
    },

    notifyUser: function (records, view) {
        var self = this,
            control;
        Ext.Array.each(records, function (record) {
            if (record.data.parameters.recepients && record.data.parameters.recepients.control.xtype === 'emailList') {
                control = self.createControl(record.data.parameters.recepients);
                self.actionId = record.data.id;
                view.down('form').add(control);
            }
            if (record.data.parameters.emailBody && record.data.parameters.emailBody.control.xtype === 'textArea') {
                control = self.createControl(record.data.parameters.emailBody);
                view.down('form').add(control);
            }
        })
    },

    sendSomeone: function (records, view) {
        var self = this,
            control;
        Ext.Array.each(records, function (record) {
            if (record.data.parameters.user && record.data.parameters.user.control.xtype === 'userCombobox') {
                self.actionId = record.data.id;
                control = self.createControl(record.data.parameters.user);
                view.down('form').add(control);
            }
        });
    },

    trimFields: function () {
        var self = this,
            emailListField = self.getNotifyView().down('#emailList'),
            emailBodyField = self.getNotifyView().down('#emailBody'),
            emailListTrim,
            emailBodyTrim;
        if (!Ext.isEmpty(emailListField.value)) {
            emailListTrim = Ext.util.Format.trim(emailListField.value);
            emailListField.setValue(emailListTrim);
        }
        if (!Ext.isEmpty(emailBodyField.value)) {
            emailBodyTrim = Ext.util.Format.trim(emailBodyField.value);
            emailBodyField.setValue(emailBodyTrim);
        }
    },

    submit: function () {
        var self = this,
            notifyView = self.getNotifyView(),
            form = notifyView.down('form').getForm(),
            sendingData = {},
            preloader,
            str = window.location.href,
            formErrorsPanel = Ext.ComponentQuery.query('notify-user panel[name=errors]')[0];
        if (str.match(/notify/) !== null) {
            self.trimFields();
            if (form.isValid()) {
                formErrorsPanel.hide();
                sendingData.id = self.actionId;
                sendingData.parameters = form.getValues();
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Notifying user",
                    target: notifyView
                });
                preloader.show();
                self.sendData(sendingData, preloader);
            } else {
                self.showErrorsPanel();
            }
        } else {
            if (notifyView.down('#userCombo').value !== null && form.isValid()) {
                sendingData.parameters = {};
                notifyView.down('#userCombo').clearInvalid();
                formErrorsPanel.hide();
                sendingData.id = self.actionId;
                sendingData.parameters.user = form.getValues().user.toString();
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Sending data",
                    target: notifyView
                });
                preloader.show();
                self.sendData(sendingData, preloader);
            } else {
                self.showErrorsPanel();
                notifyView.down('#userCombo').markInvalid('This is a required field');
            }
        }
    },

    sendData: function(sendingData, preloader) {
        var self = this;
        Ext.Ajax.request({
            url: '/api/isu/issue/' + self.issId + '/action',
            method: 'PUT',
            jsonData: sendingData,
            success: function () {
                window.location.href = '#/workspace/datacollection/issues';
                self.getApplication().fireEvent('acknowledge', 'Operation completed successfully!');
            },
            failure: function () {
                var title = 'Error',
                    message = 'Operation Failed!';

                self.getApplication().getController('Uni.controller.Error').showError(title, message);
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    showErrorsPanel: function() {
        var formErrorsPanel = Ext.ComponentQuery.query('notify-user panel[name=errors]')[0];
        formErrorsPanel.hide();
        formErrorsPanel.removeAll();
        formErrorsPanel.add({
            html: 'There are errors on this page that require your attention.'
        });
        formErrorsPanel.show();
    }
});




Ext.define('Isu.view.workspace.datavalidation.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.datavalidation-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            title: 'overview',
            xtype: 'menu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('workspace.datavalidation.overview.title', 'ISU', 'Data validation'),
            flex: 1
        }
    ]
});

Ext.define('Isu.controller.DataValidation', {
    extend: 'Ext.app.Controller',

    views: [
        'Isu.view.workspace.datavalidation.Overview'
    ],

    showOverview: function () {
        var widget = Ext.widget('datavalidation-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});

Ext.define('Isu.model.User', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
        },
        {
            name: 'type',
            displayValue: 'Type',
            type: 'auto'
        },
        {
            name: 'name',
            displayValue: 'Name',
            type: 'auto'
        }
    ],

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/users',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});


Ext.define('Isu.store.Users', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.User',
    autoLoad: false
});

Ext.define('Isu.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Isu.controller.history.Workspace',
        'Isu.controller.history.Administration',
        'Isu.controller.Issues',
        'Isu.controller.AssignIssues',
        'Isu.controller.CloseIssues',
        'Isu.controller.BulkChangeIssues',
        'Isu.controller.MessageWindow',
        'Isu.controller.IssueAssignmentRules',
        'Isu.controller.IssueCreationRules',
        'Isu.controller.IssueDetail',
        'Isu.controller.AdministrationDataCollection',
        'Isu.controller.NotifySend',
        'Isu.controller.DataValidation'
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.Users'
    ],

    config: {
        navigationController: null,
        configurationController: null
    },

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        this.initNavigation();
        this.initMenu();
    },

    initMenu: function () {
        var me = this;

        var workspaceItem = Ext.create('Uni.model.MenuItem', {
            text: 'Workspace',
            glyph: 'workspace',
            portal: 'workspace',
            index: 30
        });

        Uni.store.MenuItems.add(workspaceItem);

        var administrationItem = Ext.create('Uni.model.MenuItem', {
            text: 'Administration',
            glyph: 'settings',
            portal: 'administration',
            index: 10
        });

        Uni.store.MenuItems.add(administrationItem);

        var router = me.getController('Uni.controller.history.Router'),
            historian0 = me.getController('Isu.controller.history.Workspace'), // Forces route registration.
            historian1 = me.getController('Isu.controller.history.Administration'); // Forces route registration.

        var datacollection = Ext.create('Uni.model.PortalItem', {
            title: 'Data collection',
            portal: 'workspace',
            route: 'datacollection',
            items: [
                {
                    text: 'Overview',
                    href: router.getRoute('workspace/datacollection').buildUrl()
                },
                {
                    text: 'Issues',
                    href: router.getRoute('workspace/datacollection/issues').buildUrl()
                },
                {
                    text: 'My open issues',
                    handler: function () {
                        router.getRoute('workspace/datacollection/issues').forward();
                        me.getController('Isu.controller.Issues').fireEvent('showIssuesAssignedOnMe');
                    }
                }
            ]
        });

//        var dataexchange = Ext.create('Uni.model.PortalItem', {
//            title: 'Data exchange',
//            portal: 'workspace',
//            route: 'dataexchange',
//            items: [
//                {
//                    text: 'Overview',
//                    href: '#/workspace/dataexchange'
//                },
//                {
//                    text: 'Issues',
//                    href: '#/workspace/dataexchange/issues'
//                }
//            ]
//        });
//        var dataoperation = Ext.create('Uni.model.PortalItem', {
//            title: 'Data operation',
//            portal: 'workspace',
//            route: 'dataoperation',
//            items: [
//                {
//                    text: 'Overview',
//                    href: '#/workspace/dataoperation'
//                },
//                {
//                    text: 'Issues',
//                    href: '#/workspace/dataoperation/issues'
//                }
//            ]
//        });
//
        var datavalidation = Ext.create('Uni.model.PortalItem', {
            title: 'Data validation',
            portal: 'workspace',
            route: 'datavalidation',
            items: [
                {
                    text: 'Overview',
                    href: '#/workspace/datavalidation'
                },
                {
                    text: 'Issues',
                    href: '#/workspace/datavalidation/issues'
                }
            ]
        });

        var issuemanagement = Ext.create('Uni.model.PortalItem', {
            title: 'Issue management',
            portal: 'administration',
            route: 'issuemanagement',
            items: [
                {
                    text: 'Issue assignment rules',
                    href: router.getRoute('administration/assignmentrules').buildUrl()
                },
                {
                    text: 'Issue creation rules',
                    href: router.getRoute('administration/creationrules').buildUrl()
                }
            ]
        });

        Uni.store.PortalItems.add(
            datacollection,
//            dataexchange,
//            dataoperation,
            datavalidation,
            issuemanagement
        );
    },

    initNavigation: function () {
        var navigationController = this.getController('Uni.controller.Navigation'),
            configurationController = this.getController('Uni.controller.Configuration');

        this.setNavigationController(navigationController);
        this.setConfigurationController(configurationController);
    }
});

Ext.define('Isu.model.CreationRuleAction', {
    extend: 'Ext.data.Model',
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'type',
            type: 'auto'
        },
        {
            name: 'phase',
            type: 'auto'
        },
        {
            name: 'parameters',
            type: 'auto'
        }
    ]
});

Ext.define('Isu.view.administration.datacollection.issuecreationrules.ActionsList', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Isu.model.CreationRuleAction'
    ],
    alias: 'widget.issues-creation-rules-actions-list',
    store: Ext.create('Ext.data.Store', {
        model: 'Isu.model.CreationRuleAction'
    }),
    enableColumnHide: false,
    columns: {
        items: [
            {
                itemId: 'description',
                header: 'Description',
                xtype: 'templatecolumn',
                tpl: '{type.name}',
                flex: 1
            },
            {
                itemId: 'phase',
                header: 'When to perform',
                xtype: 'templatecolumn',
                tpl: new Ext.XTemplate('{[this.getWhenToPerform(values.phase.uuid)]}', {
                    getWhenToPerform: function (uuid) {
                        var phasesStore = Ext.getStore('Isu.store.CreationRuleActionPhases'),
                            whenToPerform = phasesStore.getById(uuid).get('title');

                        return (whenToPerform || '');
                    }
                }),
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: [
                    {
                        text: Uni.I18n.translate('general.remove', 'ISU', 'Remove'),
                        action: 'delete'
                    }
                ]
            }
        ]
    }
});

Ext.define('Isu.view.administration.datacollection.issuecreationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.datacollection.issuecreationrules.ActionsList'
    ],
    alias: 'widget.issues-creation-rules-edit',
    content: [
        {
            name: 'pageTitle',
            ui: 'large',
            items: [
                {
                    xtype: 'form',
                    defaults: {
                        labelWidth: 150,
                        validateOnChange: false,
                        validateOnBlur: false,
                        width: 700
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            hidden: true
                        },
                        {
                            itemId: 'name',
                            xtype: 'textfield',
                            name: 'name',
                            fieldLabel: 'Name',
                            required: true,
                            allowBlank: false,
                            maxLength: 80
                        },
                        {
                            itemId: 'issueType',
                            xtype: 'combobox',
                            name: 'issueType',
                            fieldLabel: 'Issue type',
                            required: true,
                            store: 'Isu.store.IssueType',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'uid',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            itemId: 'ruleTemplate',
                            xtype: 'combobox',
                            name: 'template',
                            fieldLabel: 'Rule template',
                            required: true,
                            store: 'Isu.store.CreationRuleTemplate',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'uid',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            itemId: 'templateDetails',
                            xtype: 'container',
                            name: 'templateDetails',
                            defaults: {
                                labelWidth: 150,
                                margin: '0 0 10 0',
                                validateOnChange: false,
                                validateOnBlur: false,
                                width: 700
                            }
                        },
                        {
                            itemId: 'issueReason',
                            xtype: 'combobox',
                            name: 'reason',
                            fieldLabel: 'Issue reason',
                            required: true,
                            store: 'Isu.store.IssueReason',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'Due date',
                            layout: 'hbox',
                            items: [
                                {
                                    itemId: 'dueDateTrigger',
                                    xtype: 'radiogroup',
                                    name: 'dueDateTrigger',
                                    formBind: false,
                                    columns: 1,
                                    vertical: true,
                                    width: 100,
                                    defaults: {
                                        name: 'dueDate',
                                        formBind: false,
                                        submitValue: false
                                    },
                                    items: [
                                        {
                                            itemId: 'noDueDate',
                                            boxLabel: 'No due date',
                                            inputValue: false
                                        },
                                        {
                                            itemId: 'dueIn',
                                            boxLabel: 'Due in',
                                            inputValue: true
                                        }
                                    ],
                                    listeners: {
                                        change: {
                                            fn: function (radioGroup, newValue, oldValue) {
                                                this.up('issues-creation-rules-edit').dueDateTrigger(radioGroup, newValue, oldValue);
                                            }
                                        }
                                    }
                                },
                                {
                                    itemId: 'dueDateValues',
                                    xtype: 'container',
                                    name: 'dueDateValues',
                                    margin: '30 0 10 0',
                                    layout: {
                                        type: 'hbox'
                                    },
                                    items: [
                                        {
                                            itemId: 'dueIn.number',
                                            xtype: 'numberfield',
                                            name: 'dueIn.number',
                                            minValue: 1,
                                            width: 60,
                                            margin: '0 10 0 0',
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('issues-creation-rules-edit [boxLabel=Due in]')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            }
                                        },
                                        {
                                            itemId: 'dueIn.type',
                                            xtype: 'combobox',
                                            name: 'dueIn.type',
                                            store: 'Isu.store.DueinType',
                                            queryMode: 'local',
                                            displayField: 'displayValue',
                                            valueField: 'name',
                                            editable: false,
                                            width: 100,
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('issues-creation-rules-edit [boxLabel=Due in]')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            itemId: 'comment',
                            xtype: 'textareafield',
                            name: 'comment',
                            fieldLabel: 'Comment',
                            emptyText: 'Provide a comment (optionally)',
                            height: 100
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'Actions',
                            width: 900,
                            items: [
                                {
                                    xtype: 'button',
                                    itemId: 'addAction',
                                    text: 'Add action',
                                    action: 'addAction',
                                    ui: 'action',
                                    margin: '0 0 10 0'
                                },
                                {
                                    xtype: 'issues-creation-rules-actions-list',
                                    hidden: true
                                },
                                {
                                    name: 'noactions',
                                    html: 'There are no actions added yet to this rule',
                                    hidden: true
                                }
                            ]
                        },
                        {
                            xtype: 'fieldcontainer',
                            ui: 'actions',
                            fieldLabel: '&nbsp',
                            defaultType: 'button',
                            items: [
                                {
                                    itemId: 'ruleAction',
                                    name: 'ruleAction',
                                    ui: 'action',
                                    action: 'save'
                                },
                                {
                                    itemId: 'cancel',
                                    text: 'Cancel',
                                    ui: 'link',
                                    name: 'cancel',
                                    href: '#/administration/creationrules'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    dueDateTrigger: function (radioGroup, newValue) {
        var dueDateValues = this.down('form [name=dueDateValues]'),
            dueInNumberField = this.down('form [name=dueIn.number]'),
            dueInTypeField = this.down('form [name=dueIn.type]');

        if (!newValue.dueDate) {
            dueInNumberField.reset();
            dueInTypeField.setValue(dueInTypeField.getStore().getAt(0).get('name'));
        }
    }
});

Ext.define('Isu.model.IssueType', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uid',
            type: 'text'
        },
        {
            name: 'name',
            type: 'text'
        }
    ],

    idProperty: 'uid',

    proxy: {
        type: 'rest',
        url: '/api/isu/issuetypes',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.IssueType', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueType',
    pageSize: 10,
    autoLoad: false
});

Ext.define('Isu.model.CreationRuleTemplate', {
    extend: 'Ext.data.Model',
    belongsTo: 'Isu.model.CreationRule',
    fields: [
        {
            name: 'uid',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            name: 'parameters',
            type: 'auto'
        }
    ],

    idProperty: 'uid',

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/templates',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.CreationRuleTemplate', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRuleTemplate',
    autoLoad: false,

    listeners: {
        beforeload: function () {
            this.getProxy().setExtraParam('issueType', 'datacollection');
        }
    }
});

Ext.define('Isu.model.DueinType', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'displayValue',
            type: 'string'
        }
    ]
});

Ext.define('Isu.store.DueinType', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.DueinType',

    data: [
        {name: "days", displayValue: 'day(s)'},
        {name: "weeks", displayValue: 'week(s)'},
        {name: "months", displayValue: 'month(s)'}
    ]
});

Ext.define('Isu.store.Clipboard', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'value',
            type: 'auto'
        }
    ],

    set: function (name, obj) {
        var model = this.getById(name);

        if (model) {
            model.set('value', obj)
        } else {
            this.add({
                id: name,
                value: obj
            });
        }
    },

    get: function (name) {
        var model = this.getById(name);

        if (model) {
            return model.get('value');
        } else {
            return model;
        }
    },

    clear: function (name) {
        var model = this.getById(name);

        this.remove(model);
    }
});

Ext.define('Isu.model.CreationRuleActionPhases', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uuid',
            type: 'string'
        },
        {
            name: 'title',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        }
    ],

    idProperty: 'uuid'
});

Ext.define('Isu.store.CreationRuleActionPhases', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRuleActionPhases',
    pageSize: 50,
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/actions/phases',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.controller.IssueCreationRulesEdit', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.CreationRule',
        'Isu.store.IssueType',
        'Isu.store.CreationRuleTemplate',
        'Isu.store.DueinType',
        'Isu.store.Clipboard',
        'Isu.store.CreationRuleActionPhases'
    ],
    views: [
        'Isu.view.administration.datacollection.issuecreationrules.Edit',
        'Isu.view.workspace.issues.MessagePanel'
    ],

    models: [
        'Isu.model.CreationRuleAction'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issues-creation-rules-edit'
        },
        {
            ref: 'pageTitle',
            selector: 'issues-creation-rules-edit [name=pageTitle]'
        },
        {
            ref: 'ruleForm',
            selector: 'issues-creation-rules-edit form'
        },
        {
            ref: 'ruleActionBtn',
            selector: 'issues-creation-rules-edit button[name=ruleAction]'
        },
        {
            ref: 'templateDetails',
            selector: 'issues-creation-rules-edit form [name=templateDetails]'
        },
        {
            ref: 'actionsGrid',
            selector: 'issues-creation-rules-edit issues-creation-rules-actions-list'
        }
    ],

    mixins: [
        'Isu.util.IsuGrid',
        'Isu.util.CreatingControl'
    ],

    init: function () {
        this.control({
            'issues-creation-rules-edit form [name=issueType]': {
                change: this.setRuleTemplateCombobox
            },
            'issues-creation-rules-edit form [name=template]': {
                change: this.setRuleTemplate,
                resize: this.comboTemplateResize
            },
            'issues-creation-rules-edit': {
                beforedestroy: this.removeTemplateDescription
            },
            'issues-creation-rules-edit button[action=save]': {
                click: this.ruleSave
            },
            'issues-creation-rules-edit button[action=addAction]': {
                click: this.addAction
            },
            'issues-creation-rules-edit issues-creation-rules-actions-list uni-actioncolumn': {
                menuclick: this.chooseActionOperation
            }
        });

        this.on('templateloaded', this.checkDependencies, this);
    },

    showCreate: function (id) {
        var widget = Ext.widget('issues-creation-rules-edit');

        this.setPage(id, 'create', widget);
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showEdit: function (id) {
        var widget = Ext.widget('issues-creation-rules-edit');

        this.setPage(id, 'edit', widget);
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    clearActionsStore: function (widget) {
        var actionsGrid = widget ? widget.down('issues-creation-rules-actions-list') : this.getActionsGrid(),
            actionsStore = actionsGrid.getStore();

        actionsStore.removeAll();
    },

    setPage: function (id, action, widget) {
        var me = this,
            ruleActionBtn = me.getRuleActionBtn(),
            clipboard = this.getStore('Isu.store.Clipboard'),
            savedData = clipboard.get('issuesCreationRuleState'),
            title,
            btnTxt;

        this.clearActionsStore(widget);

        switch (action) {
            case 'edit':
                title = Uni.I18n.translate('administration.issueCreationRules.title.editIssueCreationRule', 'ISU', 'Edit issue creation rule');
                btnTxt = Uni.I18n.translate('general.save', 'ISU', 'Save');
                if (savedData) {
                    me.ruleModel = savedData;
                    clipboard.clear('issuesCreationRuleState');
                    widget.on('afterrender', function () {
                        me.modelToForm(me.ruleModel);
                    }, me, {single: true});
                } else {
                    me.getModel('Isu.model.CreationRule').load(id, {
                        success: function (record) {
                            me.ruleModel = record;
                            delete me.ruleModel.data.creationDate;
                            delete me.ruleModel.data.modificationDate;
                            if (widget.isVisible()) {
                                me.modelToForm(record);
                            } else {
                                widget.on('afterrender', function () {
                                    me.modelToForm(record);
                                }, me, {single: true});
                            }
                        }
                    });
                }
                break;
            case 'create':
                title = Uni.I18n.translate('administration.issueCreationRules.title.addIssueCreationRule', 'ISU', 'Add issue creation rule');
                btnTxt = Uni.I18n.translate('general.add', 'ISU', 'Add');
                if (savedData) {
                    me.ruleModel = savedData;
                    clipboard.clear('issuesCreationRuleState');
                } else {
                    me.ruleModel = Isu.model.CreationRule.create();
                    delete me.ruleModel.data.id;
                    me.ruleModel.data.actions = [];
                }
                widget.on('afterrender', function () {
                    me.modelToForm(me.ruleModel);
                }, me, {single: true});
                break;
        }

        me.getPageTitle().title = title;
        ruleActionBtn.setText(btnTxt);
    },

    setDataToModel: function (data, model) {
        for (var field in data) {
            model.set(field, data[field]);
        }
    },

    modelToForm: function (record) {
        var me = this,
            form = me.getRuleForm(),
            data = record.getData(),
            nameField = form.down('[name=name]'),
            issueTypeField = form.down('[name=issueType]'),
            templateField = form.down('[name=template]'),
            templateDetails = this.getTemplateDetails(),
            reasonField = form.down('[name=reason]'),
            dueDateTrigger = form.down('[name=dueDateTrigger]'),
            dueInNumberField = form.down('[name=dueIn.number]'),
            dueInTypeField = form.down('[name=dueIn.type]'),
            commentField = form.down('[name=comment]'),
            page = me.getPage();

        if (record.get('template') && record.get('template').uid) {
            page.setLoading(true);
            me.on('templateloaded', function () {
                var formField,
                    name,
                    value;

                if (data.parameters) {
                    for (name in data.parameters) {
                        formField = templateDetails.down('[name=' + name + '][isFormField=true]');
                        value = data.parameters[name];

                        formField && formField.setValue(value);
                    }
                }

                page.setLoading(false);
            }, me, {single: true});
        }

        nameField.setValue(data.name);
        issueTypeField.getStore().load(function () {
            issueTypeField.setValue(data.issueType.uid || issueTypeField.getStore().getAt(0).get('uid'));
            templateField.getStore().on('load', function () {
                templateField.setValue(data.template.uid);
            }, me, {single: true});
        });
        reasonField.getStore().load(function () {
            reasonField.setValue(data.reason.id);
        });
        if (data.dueIn.number) {
            dueDateTrigger.setValue({dueDate: true});
            dueInNumberField.setValue(data.dueIn.number);
            dueInTypeField.setValue(data.dueIn.type || dueInTypeField.getStore().getAt(0).get('name'));
        } else {
            dueDateTrigger.setValue({dueDate: false});
        }
        commentField.setValue(data.comment);

        me.loadActionsToForm(record.actions().getRange());
    },

    formToModel: function (model) {
        var form = this.getRuleForm(),
            ruleModel = model || Isu.model.CreationRule.create(),
            nameField = form.down('[name=name]'),
            issueTypeField = form.down('[name=issueType]'),
            templateField = form.down('[name=template]'),
            reasonField = form.down('[name=reason]'),
            dueInNumberField = form.down('[name=dueIn.number]'),
            dueInTypeField = form.down('[name=dueIn.type]'),
            commentField = form.down('[name=comment]'),
            templateDetails = this.getTemplateDetails(),
            parameters = {};

        ruleModel.set('name', nameField.getValue());
        ruleModel.set('issueType', {
            uid: issueTypeField.getValue()
        });
        ruleModel.set('template', {
            uid: templateField.getValue()
        });
        ruleModel.set('reason', {
            id: reasonField.getValue()
        });
        ruleModel.set('dueIn', {
            number: dueInNumberField.getValue(),
            type: dueInTypeField.getValue()
        });
        ruleModel.set('comment', commentField.getValue());

        Ext.Array.each(templateDetails.query(), function (formItem) {
            if (formItem.isFormField && formItem.submitValue) {
                parameters[formItem.name] = formItem.getValue();
            }
        });

        ruleModel.set('parameters', parameters);
        this.loadActionsToModel(ruleModel);

        return ruleModel;
    },

    setRuleTemplateCombobox: function (combo, newValue) {
        var form = this.getRuleForm(),
            templateField = form.down('[name=template]'),
            templateStore = templateField.getStore(),
            templateStoreProxy = templateStore.getProxy();

        if (newValue) {
            templateStoreProxy.setExtraParam('issuetype', newValue);
            templateField.reset();
            templateStore.load();
        }
    },

    setRuleTemplate: function (combo, newValue) {
        var me = this,
            templateDetails = me.getTemplateDetails(),
            templateModel = combo.getStore().model,
            formItem;

        templateDetails.removeAll();

        if (newValue) {
            templateModel.load(newValue, {
                success: function (template) {
                    var description = template.get('description'),
                        parameters = template.get('parameters');

                    me.addTemplateDescription(combo, description);

                    Ext.Array.each(parameters, function (obj) {
                        formItem = me.createControl(obj);
                        formItem && templateDetails.add(formItem);
                    });
                    me.fireEvent('templateloaded', template);
                }
            });
        }
    },

    addTemplateDescription: function (combo, descriptionText) {
        var form = this.getRuleForm();

        this.removeTemplateDescription();

        if (descriptionText) {
            combo.templateDescriptionIcon = Ext.widget('button', {
                tooltip: Uni.I18n.translate('administration.issueCreationRules.templateInfo', 'ISU', 'Template info'),
                iconCls: 'icon-info-small',
                ui: 'blank',
                itemId: 'creationRuleTplHelp',
                floating: true,
                renderTo: form.getEl(),
                shadow: false,
                width: 16,
                handler: function () {
                    combo.templateDescriptionWindow = Ext.Msg.show({
                        title: Uni.I18n.translate('administration.issueCreationRules.templateDescription', 'ISU', 'Template description'),
                        msg: descriptionText,
                        buttons: Ext.MessageBox.CANCEL,
                        buttonText: {cancel: Uni.I18n.translate('general.close', 'ISU', 'Close')},
                        modal: true,
                        animateTarget: combo.templateDescriptionIcon
                    })
                }
            });
            this.comboTemplateResize(combo);
        }
    },

    comboTemplateResize: function (combo) {
        var comboEl = combo.getEl(),
            icon = combo.templateDescriptionIcon;

        icon && icon.setXY([
            comboEl.getX() + comboEl.getWidth(false) + 5,
            comboEl.getY() + (comboEl.getHeight(false) - icon.getEl().getHeight(false)) / 2
        ]);
    },

    removeTemplateDescription: function () {
        var combo = Ext.ComponentQuery.query('issues-creation-rules-edit form [name=template]')[0];

        if (combo && combo.templateDescriptionIcon) {
            combo.templateDescriptionIcon.clearListeners();
            combo.templateDescriptionIcon.destroy();
            delete combo.templateDescriptionIcon;
        }
    },

    ruleSave: function (button) {
        var me = this,
            form = me.getRuleForm().getForm(),
            rule = me.formToModel(me.ruleModel),
            formErrorsPanel = me.getRuleForm().down('[name=form-errors]'),
            store = me.getStore('Isu.store.CreationRule'),
            templateCombo = me.getRuleForm().down('combobox[name=template]'),
            router = this.getController('Uni.controller.history.Router'),
            page = me.getPage();

        if (form.isValid()) {
            page.setLoading('Saving...');
            button.setDisabled(true);
            formErrorsPanel.hide();
            rule.save({
                callback: function (model, operation, success) {
                    var messageText,
                        json;

                    page.setLoading(false);
                    button.setDisabled(false);

                    if (success) {
                        switch (operation.action) {
                            case 'create':
                                messageText = Uni.I18n.translate('administration.issueCreationRules.createSuccess.msg', 'ISU', 'Issue creation rule added');
                                break;
                            case 'update':
                                messageText = Uni.I18n.translate('administration.issueCreationRules.updateSuccess.msg', 'ISU', 'Issue creation rule updated');
                                break;
                        }
                        me.getApplication().fireEvent('acknowledge', messageText);
                        router.getRoute('administration/creationrules').forward();
                    } else {
                        json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {
                            form.markInvalid(json.errors);
                            formErrorsPanel.show();
                            me.comboTemplateResize(templateCombo);
                        }
                    }
                }
            });
        } else {
            formErrorsPanel.show();
            me.comboTemplateResize(templateCombo);
        }
    },

    addAction: function () {
        var router = this.getController('Uni.controller.history.Router'),
            rule = this.formToModel(this.ruleModel);

        this.getStore('Isu.store.Clipboard').set('issuesCreationRuleState', rule);

        router.getRoute('administration/creationrules/add/addaction').forward();
    },

    loadActionsToForm: function (actions) {
        var actionsGrid = this.getActionsGrid(),
            actionsStore = actionsGrid.getStore(),
            noActionsText = this.getPage().down('[name=noactions]'),
            phasesStore = this.getStore('Isu.store.CreationRuleActionPhases');

        if (actions.length) {
            phasesStore.load(function () {
                actionsStore.loadData(actions, false);
                actionsGrid.show();
                noActionsText.hide();
            });
        } else {
            actionsGrid.hide();
            noActionsText.show();
        }
    },

    loadActionsToModel: function (model) {
        var me = this,
            actionsGrid = me.getActionsGrid(),
            actionsStore = actionsGrid.getStore(),
            actions = actionsStore.getRange();

        model.actions().loadData(actions, false);
    },

    chooseActionOperation: function (menu, item) {
        var operation = item.action,
            actionId = menu.record.getId();

        switch (operation) {
            case 'delete':
                this.deleteAction(actionId);
                break;
        }
    },

    deleteAction: function (id) {
        var actionsGrid = this.getActionsGrid(),
            actionsStore = actionsGrid.getStore(),
            action = actionsStore.getById(id),
            noActionsText = this.getPage().down('[name=noactions]');

        actionsStore.remove(action);

        if (!actionsStore.getCount()) {
            actionsGrid.hide();
            noActionsText.show();
        }
    },

    checkDependencies: function (template) {
        var me = this,
            templateId = template ? template.getId() : me.getRuleForm().down('#ruleTemplate').getValue(),
            templateDetails = me.getTemplateDetails(),
            parametersFields = templateDetails.query('[isFormField=true]');

        Ext.Array.each(parametersFields, function (field) {
            var linkedFields = [];

            if (field.dependOn) {
                linkedFields.push(field);
                Ext.Array.each(field.dependOn, function (dependOnName) {
                    var dependOnField = templateDetails.down('[name=' + dependOnName + ']');
                    linkedFields.push(dependOnField);
                    dependOnField && dependOnField.on('blur', function () {
                        var data = {};
                        Ext.Array.each(linkedFields, function (linkedField) {
                            data[linkedField.name] = linkedField.getValue();
                        });
                        Ext.Ajax.request({
                            url: ' /api/isu/rules/templates/' + templateId + '/parameters/' + field.name,
                            method: 'PUT',
                            jsonData: Ext.encode(data),
                            success: function (response) {
                                var responseTextObj = Ext.decode(response.responseText, true),
                                    newControl = me.createControl(responseTextObj.data),
                                    oldControl = templateDetails.down('[name=' + newControl.name + ']'),
                                    index = templateDetails.query().indexOf(oldControl);
                                oldControl.destroy();
                                templateDetails.insert(index, newControl);
                                me.checkDependencies();
                            }
                        });
                    }, me, {single: true});
                });
            }
        });
    }
});

Ext.define('Isu.view.administration.datacollection.issuecreationrules.EditAction', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [

    ],
    alias: 'widget.issues-creation-rules-edit-action',
    content: [
        {
            name: 'pageTitle',
            ui: 'large',
            items: [
                {
                    xtype: 'form',
                    width: '75%',
                    defaults: {
                        labelWidth: 150,
                        validateOnChange: false,
                        validateOnBlur: false,
                        width: 700
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            hidden: true
                        },
                        {
                            xtype: 'radiogroup',
                            itemId: 'phasesRadioGroup',
                            name: 'phasesRadioGroup',
                            fieldLabel: 'When to perform',
                            required: true,
                            columns: 1,
                            vertical: true
                        },
                        {
                            itemId: 'actionType',
                            xtype: 'combobox',
                            name: 'actionType',
                            fieldLabel: 'Action',
                            required: true,
                            store: 'Isu.store.Actions',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            itemId: 'actionTypeDetails',
                            xtype: 'container',
                            name: 'actionTypeDetails',
                            defaults: {
                                labelWidth: 150,
                                margin: '0 0 10 0',
                                validateOnChange: false,
                                validateOnBlur: false,
                                width: 700
                            }
                        }
                    ],
                    buttons: [
                        {
                            itemId: 'actionOperation',
                            name: 'actionOperation',
                            ui: 'action',
                            formBind: false,
                            action: 'actionOperation'
                        },
                        {
                            itemId: 'cancel',
                            text: 'Cancel',
                            action: 'cancel',
                            ui: 'link',
                            name: 'cancel'
                        }
                    ]
                }
            ]
        }
    ]
});

Ext.define('Isu.controller.IssueCreationRulesActionsEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.Actions',
        'Isu.store.CreationRuleActionPhases',
        'Isu.store.Clipboard'
    ],

    views: [
        'Isu.view.administration.datacollection.issuecreationrules.EditAction'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issues-creation-rules-edit-action'
        },
        {
            ref: 'pageTitle',
            selector: 'issues-creation-rules-edit-action [name=pageTitle]'
        },
        {
            ref: 'actionOperationBtn',
            selector: 'issues-creation-rules-edit-action button[name=actionOperation]'
        },
        {
            ref: 'actionForm',
            selector: 'issues-creation-rules-edit-action form'
        },
        {
            ref: 'phasesRadioGroup',
            selector: 'issues-creation-rules-edit-action form [name=phasesRadioGroup]'
        },
        {
            ref: 'actionTypeDetails',
            selector: 'issues-creation-rules-edit-action form [name=actionTypeDetails]'
        }
    ],

    models: [
        'Isu.model.CreationRuleAction'
    ],

    mixins: [
        'Isu.util.CreatingControl'
    ],

    init: function () {
        this.control({
            'issues-creation-rules-edit-action form [name=actionType]': {
                change: this.setActionTypeDetails
            },
            'issues-creation-rules-edit-action button[action=cancel]': {
                click: this.finishEdit
            },
            'issues-creation-rules-edit-action button[action=actionOperation]': {
                click: this.saveAction
            }
        });
    },

    showCreate: function (id) {
        var widget = Ext.widget('issues-creation-rules-edit-action');

        Ext.util.History.on('change', this.checkRoute, this);

        this.setPage(id, 'create');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    checkRoute: function (token) {
        var clipboard = this.getStore('Isu.store.Clipboard'),
            createRegexp = /administration\/creationrules\/add/,
            editRegexp = /administration\/creationrules\/\d+\/edit/;

        Ext.util.History.un('change', this.checkRoute, this);

        if (token.search(createRegexp) == -1 && token.search(editRegexp) == -1) {
            clipboard.clear('issuesCreationRuleState');
        }
    },

    setPage: function (id, action) {
        var self = this,
            actionTypesStore = self.getStore('Isu.store.Actions'),
            actionTypesPhases = self.getStore('Isu.store.CreationRuleActionPhases'),
            loadedStoresCount = 0,
            prefix,
            btnTxt;

        var checkLoadedStores = function () {
            loadedStoresCount++;

            if (loadedStoresCount == 2) {
                switch (action) {
                    case 'create':
                        prefix = btnTxt = 'Add ';
                        self.actionModel = Ext.create('Isu.model.CreationRuleAction');
                        break;
                }

                self.getPageTitle().setTitle(prefix + 'action');
                self.getActionOperationBtn().setText(btnTxt);
            }
        };

        actionTypesStore.load(checkLoadedStores);
        actionTypesPhases.load(function (records) {
            var phasesRadioGroup = self.getPhasesRadioGroup();

            Ext.Array.each(records, function (record, index) {
                phasesRadioGroup.add({
                    boxLabel: record.get('title'),
                    name: 'phase',
                    inputValue: record.get('uuid'),
                    afterSubTpl: '<span style="color: #686868; font-style: italic">' + record.get('description') + '</span>',
                    checked: !index
                });
            });
            checkLoadedStores();
        });
    },

    formToModel: function (model) {
        var form = this.getActionForm(),
            phaseField = form.down('[name=phasesRadioGroup]'),
            actionStore = this.getStore('Isu.store.Actions'),
            actionField = form.down('[name=actionType]'),
            action = actionStore.getById(actionField.getValue()),
            parameters = {};

        model.set('type', action.getData());
        delete model.get('type').parameters;
        model.set('phase', {
            uuid: phaseField.getValue().phase
        });
        Ext.Array.each(form.down('[name=actionTypeDetails]').query(), function (formItem) {
            if (formItem.isFormField) {
                parameters[formItem.name] = formItem.getValue();
            }
        });
        model.set('parameters', parameters);

        return model;
    },

    setActionTypeDetails: function (combo, newValue) {
        var self = this,
            actionTypesStore = self.getStore('Isu.store.Actions'),
            parameters = actionTypesStore.getById(newValue).get('parameters'),
            actionTypeDetails = self.getActionTypeDetails();

        actionTypeDetails.removeAll();

        Ext.Object.each(parameters, function(key, value) {
            var formItem = self.createControl(value);

            formItem && actionTypeDetails.add(formItem);
        });
    },

    saveAction: function () {
        var rule = this.getStore('Isu.store.Clipboard').get('issuesCreationRuleState'),
            form = this.getActionForm().getForm(),
            formErrorsPanel = this.getActionForm().down('[name=form-errors]'),
            newAction,
            actions;

        if (rule) {
            if (form.isValid()) {
                newAction = this.formToModel(this.actionModel);
                actions = rule.actions();
                formErrorsPanel.hide();
                actions.add(newAction);
                this.finishEdit();
            } else {
                formErrorsPanel.show();
            }
        } else {
            this.finishEdit();
        }

    },

    finishEdit: function () {
        var router = this.getController('Uni.controller.history.Router'),
            rule = this.getStore('Isu.store.Clipboard').get('issuesCreationRuleState');

        if (rule) {
            if (rule.getId()) {
                router.getRoute('administration/creationrules/edit').forward({id: rule.getId()});
            } else {
                router.getRoute('administration/creationrules/add').forward();
            }
        } else {
            router.getRoute('administration/creationrules').forward();
        }
    }
});

Ext.define('Isu.Application', {
    name: 'Isu',
    appProperty: 'Current',

    extend: 'Ext.app.Application',

    controllers: [
        'Isu.controller.Main',
        'Isu.controller.history.Workspace',
        'Isu.controller.Issues',
        'Isu.controller.AssignIssues',
        'Isu.controller.CloseIssues',
        'Isu.controller.BulkChangeIssues',
        'Isu.controller.MessageWindow',
        'Isu.controller.IssueAssignmentRules',
        'Isu.controller.IssueCreationRules',
        'Isu.controller.IssueCreationRulesEdit',
        'Isu.controller.IssueCreationRulesActionsEdit',
        'Isu.controller.history.Workspace',
        'Isu.controller.IssueDetail',
        'Isu.controller.history.Administration',
        'Isu.controller.AdministrationDataCollection',
        'Isu.controller.NotifySend'
    ],

    init: function () {
        // TODO App specific loading.
        this.callParent(arguments);
    },

    launch: function () {
        //this.fireEvent('changeapptitleevent', 'Jupiter issue application');
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});

Ext.define('Isu.view.workspace.datacollection.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.datacollection-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            title: 'overview',
            xtype: 'menu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('issue.workspace.datacollection', 'ISU', 'Data collection'),
            flex: 1
        }
    ]
});

Ext.define('Isu.controller.DataCollectionOverview', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'workspace.datacollection.Overview'
    ],

    init: function () {
    },

    showOverview: function () {
        var widget = Ext.widget('datacollection-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});

Ext.define('Isu.model.BulkIssues', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id',  type: 'string'},
        {name: 'version',  type: 'string'}
    ]
});

var getPath = Ext.Loader.getPath;

/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Isu.override.LoaderOverride', {
    override: 'Ext.Loader',

    basePath: '',

    setBasePath: function(path) {
        this.basePath = path;
    },

    getBasePath: function() {
        return this.basePath;
    },

    getPath: function(className) {
        var path = getPath(className);

        if (path[0] === "/" && this.getBasePath()) {
            path = this.getBasePath() + path;
        }
        return path;
    }
});

/**
 * Config loader
 */
Ext.define('Isu.util.Config', {
    urls: [
        'resources/config/application.json',
        'resources/config/application.local.json'
    ],
    requires: [
        'Ext.Ajax'
    ],

    load: function(config) {
        _.each(config, function(item, key) {
            var obj = Ext.decode(key);

            if (obj) {
                Ext.override(obj, item);
            }
        });
    },

    /**
     * todo: refactor this
     * @param callback
     */
    onReady: function(callback) {
        var me = this;

        Ext.Ajax.request({
            url: 'resources/config/application.local.json',
            success: function(response, opts) {
                if (response.responseText) {
                    var config = Ext.decode(response.responseText);
                    me.load(config);
                }
                callback();
            },

            failure: function(response, opts) {
                callback();
            }
        });
    }
});

Ext.define('Isu.util.IssueHydrator', {
    extend: 'Uni.util.Hydrator',

    extract: function () {
        var me = this,
            data = me.callParent(arguments),
            assignee;

        if (data.assignee) {
            assignee = data.assignee.split(':');
            data.assigneeId = assignee[0];
            data.assigneeType = assignee[1];
            delete data.assignee;
        }

        delete data.id;

        return data;
    },

    hydrate: function (data, object) {
        var me = this,
            assignee = {},
            assigneeId,
            assigneeType;

        assigneeId = Ext.Array.findBy(data, function (item) {
            return item.property === 'assigneeId';
        });
        assigneeType = Ext.Array.findBy(data, function (item) {
            return item.property === 'assigneeType';
        });
        if (assigneeId && assigneeType) {
            assignee.property = 'assignee';
            assignee.value = assigneeId.value + ':' + assigneeType.value;
            data.push(assignee);
            Ext.Array.remove(data, assigneeId);
            Ext.Array.remove(data, assigneeType);
        }

        me.callParent([data, object]);
        return object;
    }
});

Ext.define('Isu.view.Viewport', {
    extend: 'Uni.view.Viewport',
    overflowY: true
});

Ext.define('Isu.view.ext.button.ItemAction', {
    extend: 'Ext.button.Button',
    text: 'Actions',
    iconCls: 'x-uni-action-iconD',

    alias: 'widget.item-action',
    menuAlign: 'tr-br?',
    handler: function(){
        this.showMenu()
    }
});

Ext.define('Isu.view.workspace.issues.Filter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.menu.Separator',
        'Isu.view.workspace.issues.FilteringToolbar',
        'Isu.view.workspace.issues.SortingToolbar',
        'Isu.view.workspace.issues.GroupingToolbar',
        'Isu.view.workspace.issues.IssueGroup'
    ],
    alias: "widget.issues-filter",
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'filtering-toolbar'
        },
        {
            itemId: 'menuseparator',
            xtype: 'menuseparator'
        },
        {
            xtype: 'grouping-toolbar'
        },
        {
            itemId: 'menuseparator',
            xtype: 'menuseparator'
        },
        {
            xtype: 'sorting-toolbar'
        },
        {
            xtype: 'issue-group'
        }
    ]
});

Ext.define('Isu.view.workspace.issues.IssueNoGroup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issue-no-group',
    itemId: 'IssueNoGroup',
    hidden: true,

    items: [
        {
            itemId : 'NoGroup_text',
            html: '<h3>No group selected</h3><p>Select a group of issues.</p>',
            bodyPadding: 10,
            border: false
        }
    ]
});

Ext.define('Isu.view.workspace.issues.FormWithFilters', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issue-form-with-filters',
    layout: 'column',
    itemId: 'issue-detailed-form',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    ui: 'medium',

    items: [
        {
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    itemId: '_reason',
                    fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                    name: 'reason_name_f'
                },
                {
                    itemId: '_customer',
                    fieldLabel: Uni.I18n.translate('general.title.customer', 'ISU', 'Customer'),
                    name: 'customer'
                },
                {
                    itemId: '_location',
                    fieldLabel: Uni.I18n.translate('general.title.location', 'ISU', 'Location'),
                    name: 'service_location'
                },
                {
                    itemId: '_usagepoint',
                    fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'ISU', 'Usage point'),
                    name: 'usage_point'
                },
                {
                    itemId: '_devicename',
                    fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
                    name: 'device_f'
                }
            ]
        },
        {
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    itemId: '_status',
                    fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                    name: 'status_name_f'
                },
                {
                    itemId: '_dueDate',
                    fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                    name: 'dueDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y')
                },
                {
                    itemId: '_assignee',
                    fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                    name: 'assignee_name_f'
                },
                {
                    itemId: '_creationDate',
                    fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISU', 'Creation date'),
                    name: 'creationDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                },
                {
                    itemId: '_serviceCat',
                    fieldLabel: Uni.I18n.translate('general.title.serviceCategory', 'ISU', 'Service category'),
                    name: 'service_category'
                }
            ]
        }
    ]
});

Ext.define('Isu.view.workspace.issues.Item', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-item',
    itemId: 'issues-item',
    requires: [
        'Isu.view.workspace.issues.ActionMenu',
        'Isu.view.workspace.issues.FormWithFilters'
    ],
    title: '',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'issue-action-menu'
            }
        }
    ],
    items: {
        itemId: 'issue-form-with-filters',
        xtype: 'issue-form-with-filters',
        bbar: {
            layout: {
                type: 'vbox',
                align: 'right'
            },
            items: {
                text: Uni.I18n.translate('general.title.viewDetails', 'ISU', 'View details'),
                itemId: 'viewDetails',
                ui: 'link',
                action: 'view',
                listeners: {
                    click: function () {
                        window.location.href = "#/workspace/datacollection/issues/" + this.up('form').getRecord().get('id')
                    }
                }
            }
        }
    }
});

Ext.define('Isu.view.workspace.issues.Browse', {
    itemId: 'Panel',
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-browse',
    ui: 'large',
    title: 'Issues',

    items: [
        {   itemId: 'issues-filter',
            xtype: 'issues-filter'
        },
        {   itemId: 'issues-no-group',
            xtype: 'issue-no-group'
        },
        {   itemId: 'issues-list',
            xtype: 'issues-list'
        },
        {   itemId: 'noIssues',
            name: 'noIssues',
            hidden: true
        },
        {   itemId: 'issues-item',
            xtype: 'issues-item'
        }
    ]
});

Ext.define('Isu.view.workspace.issues.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-overview',
    itemId: 'issuesOverview',

    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.workspace.issues.Filter',
        'Isu.view.workspace.issues.List',
        'Isu.view.workspace.issues.Item',
        'Isu.view.workspace.issues.SideFilter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    side: {
        itemId: 'navigation',
        xtype: 'panel',
        ui: 'medium',
        title: 'Navigation',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'overview',
                xtype: 'menu',
                title: 'Overview',
                ui: 'side-menu',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                floating: false,
                plain: true,
                items: [
                    {
                        text: 'Issues',
                        cls: 'current'
                    }
                ]
            },
            {   itemId: 'issues-side-filter',
                xtype: 'issues-side-filter'
            }
        ]
    },

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues'),
            items: [
                {   itemId: 'issues-filter',
                    xtype: 'issues-filter'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'issues-list'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('workspace.issues.empty.title', 'ISU', 'No issues found'),
                        reasons: [
                            Uni.I18n.translate('workspace.issues.empty.list.item1', 'ISU', 'No issues have been defined yet.'),
                            Uni.I18n.translate('workspace.issues.empty.list.item2', 'ISU', 'No issues comply to the filter.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'issues-item'
                    }
                }
            ]
        }
    ]
});

