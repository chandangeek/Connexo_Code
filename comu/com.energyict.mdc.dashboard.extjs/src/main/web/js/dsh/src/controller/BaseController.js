Ext.define('Dsh.controller.BaseController', {
    extend: 'Ext.app.Controller',
    prefix: '',

    init: function () {
        if (this.prefix) {
            var control = {};

            control[this.prefix + ' filter-top-panel'] = {
                removeFilter: this.removeFilter,
                clearAllFilters: this.clearFilter
            };
            control[this.prefix + ' #filter-form side-filter-combo'] = {
                change: this.onFilterChange,
                updateTopFilterPanelTagButtons: this.onFilterChange
            };
            control[this.prefix + ' button[action=applyfilter]'] = {
                click: this.applyFilter
            };
            control[this.prefix + ' button[action=clearfilter]'] = {
                click: this.clearFilter
            };
            this.control(control);
        }

        this.callParent(arguments);
    },

    initFilter: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getSideFilterForm().loadRecord(router.filter);

        // todo: refactor this
        this.setFilterTimeInterval(router.filter.startedBetween, 'started', 'startedBetween');
        this.setFilterTimeInterval(router.filter.finishedBetween, 'finished', 'finishedBetween');

        this.rerenderFilterPanel();
    },

    // todo: refactor this also
    setFilterTimeInterval: function (interval, prefix, propName) {
        var value = '',
            intervalFrom = interval ? interval.get('from') : undefined,
            intervalTo = interval ? interval.get('to') : undefined,
            defaultLabel = prefix.charAt(0).toUpperCase() + prefix.slice(1),
            label = Uni.I18n.translate('connection.widget.' + prefix, 'DSH', defaultLabel),
            between = Uni.I18n.translate('connection.widget.between', 'DSH', 'between'),
            after = Uni.I18n.translate('connection.widget.after', 'DSH', 'after'),
            before = Uni.I18n.translate('connection.widget.before', 'DSH', 'before'),
            and = Uni.I18n.translate('connection.widget.and', 'DSH', 'and');

        if (interval && (intervalFrom || intervalTo)) {
            if (intervalFrom && intervalTo) {
                value += ' ' + between + ' ';
                value += Ext.util.Format.date(intervalFrom, 'd/m/Y H:i');
                value += ' ' + and + ' ';
                value += Ext.util.Format.date(intervalTo, 'd/m/Y H:i');
            }
            if (intervalFrom && !intervalTo) {
                value += ' after ' + Ext.util.Format.date(intervalFrom, 'd/m/Y H:i');
            }
            if (!intervalFrom && intervalTo) {
                value += ' before ' + Ext.util.Format.date(intervalTo, 'd/m/Y H:i');
            }
            this.getFilterPanel().setFilter(propName, label, value);
        }
    },

    onFilterChange: function (combo) {
        if (!_.isEmpty(combo.getRawValue())) {
            this.getFilterPanel().setFilter(combo.getName(), combo.getFieldLabel(), combo.getRawValue());
        }
    },

    rerenderFilterPanel: function () {
        var filterPanel = this.getFilterPanel();
        filterPanel.updateContainer(filterPanel.getContainer());
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