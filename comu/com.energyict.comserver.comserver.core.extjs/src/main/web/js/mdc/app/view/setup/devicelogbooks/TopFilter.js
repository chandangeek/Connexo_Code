Ext.define('Mdc.view.setup.devicelogbooks.TopFilter', {
    extend: 'Skyline.panel.FilterToolbar',
    requires: [
        'Skyline.button.TagButton'
    ],
    alias: 'widget.deviceLogbookDataTopFilter',
    itemId: 'deviceLogbookDataTopFilter',
    title: Uni.I18n.translate('general.filter', 'MDC', 'Filter'),
    emptyText: Uni.I18n.translate('general.none', 'MDC', 'None'),

    addButtons: function (filterModel) {
        var me = this,
            container = me.getContainer(),
            intervalStart = filterModel.get('intervalStart'),
            intervalEnd = filterModel.get('intervalEnd');

        container.removeAll();

        if (intervalStart) {
            container.add(Ext.create('Skyline.button.TagButton', {
                itemId: 'filter-by-intervalStart',
                text: Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date')
                    + ' ' + Uni.I18n.translate('general.start', 'MDC', 'start').toLowerCase() + ': '
                    + Uni.I18n.formatDate('devicelogbooks.topFilter.tagButton.dateFormat', intervalStart, 'MDC', 'm/d/Y'),
                target: 'intervalStart'
            }));
        }

        if (intervalEnd) {
            container.add(Ext.create('Skyline.button.TagButton', {
                itemId: 'filter-by-intervalEnd',
                text: Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date')
                    + ' ' + Uni.I18n.translate('general.end', 'MDC', 'end').toLowerCase() + ': '
                    + Uni.I18n.formatDate('devicelogbooks.topFilter.tagButton.dateFormat', intervalEnd, 'MDC', 'm/d/Y'),
                target: 'intervalEnd'
            }));
        }
    }
});