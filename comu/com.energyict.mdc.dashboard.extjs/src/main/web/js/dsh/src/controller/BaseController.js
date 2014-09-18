Ext.define('Dsh.controller.BaseController', {
    extend: 'Ext.app.Controller',

    init: function () {
        this.control({
            'dsh-side-filter button[action=applyfilter]': {
                click: this.applyFilter
            },
            'filter-top-panel': {
                removeFilter: this.removeFilter,
                clearAllFilters: this.clearFilter
            },
            'dsh-side-filter nested-form side-filter-combo': {
                change: this.onFilterChange
            }
        });
        this.callParent(arguments);
    },

    initFilter: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getSideFilterForm().loadRecord(router.filter);

        var value = '';
        if (router.filter.startedBetween) {
            if (router.filter.startedBetween.get('from') && (router.filter.startedBetween.get('from') || router.filter.startedBetween.get('to'))) {
                value += ' from ' + Ext.util.Format.date(router.filter.startedBetween.get('from'), 'd/m/Y H:i');
            }
            if (router.filter.startedBetween.get('to')) {
                value += ' to ' + Ext.util.Format.date(router.filter.startedBetween.get('to'), 'd/m/Y H:i');
            }

            this.getFilterPanel().setFilter('startedBetween', 'Started between', value);
        }

        if (router.filter.finishedBetween && (router.filter.finishedBetween.get('from') || router.filter.finishedBetween.get('to'))) {
            value = '';
            if (router.filter.finishedBetween.get('from')) {
                value += ' from ' + Ext.util.Format.date(router.filter.finishedBetween.get('from'), 'd/m/Y H:i');
            }
            if (router.filter.finishedBetween.get('to')) {
                value += ' to ' + Ext.util.Format.date(router.filter.finishedBetween.get('to'), 'd/m/Y H:i');
            }
            this.getFilterPanel().setFilter('finishedBetween', 'Finished between', value);
        }
    },

    onFilterChange: function (combo) {
        this.getFilterPanel().setFilter(combo.getName(), combo.getFieldLabel(), combo.getRawValue());
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

        switch (key) {
            case 'startedBetween':
                delete record.startedBetween;
                break;
            case 'finishedBetween':
                delete record.finishedBetween;
                break;
            default:
                record.set(key, null);
        }

        record.save();
    }
});