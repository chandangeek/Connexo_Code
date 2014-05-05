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

    setDefaults: function () {
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
            queryString = me.setDefaults();
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
            hydrator.hydrate(filterValues, filterModel, function () {
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