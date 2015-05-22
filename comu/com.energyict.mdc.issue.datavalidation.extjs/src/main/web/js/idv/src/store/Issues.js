Ext.define('Idv.store.Issues', {
    extend: 'Uni.data.store.Filterable',
    require: [
        'Uni.component.sort.model.Sort'
    ],
    model: 'Idv.model.Issue',
    pageSize: 10,
    autoLoad: false,

    setFilterModel: function (model) {
        var proxy = this.getProxy(),
            sorting = [];

        proxy.extraParams = {};
        Ext.iterate(model.getData(), function (key, value) {
            if (value) {
                switch (key) {
                    case 'assignee':
                        proxy.setExtraParam('assigneeId', value.split(':')[0]);
                        proxy.setExtraParam('assigneeType', value.split(':')[1]);
                        break;
                    case 'grouping':
                        if (value && value.value && !model.get(value.type)) {
                            proxy.setExtraParam(value.type, value.value);
                        }
                        break;
                    case 'sorting':
                        if (Ext.isArray(value)) {
                            Ext.Array.each(value, function (sort) {
                                var direction = sort.value === Uni.component.sort.model.Sort.DESC ? '-' : '';

                                sorting.push(direction + sort.type);
                            });
                            if (sorting.length) {
                                proxy.setExtraParam('sort', sorting);
                            }
                        }
                        break;
                    default:
                        proxy.setExtraParam(key, value);
                }
            }
        });
    }
});