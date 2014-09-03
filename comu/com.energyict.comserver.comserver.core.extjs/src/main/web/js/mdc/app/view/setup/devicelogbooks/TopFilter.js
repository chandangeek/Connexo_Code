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
            intervalEnd = filterModel.get('intervalEnd'),
            domain = filterModel.get('domain'),
            subDomain = filterModel.get('subDomain'),
            eventOrAction = filterModel.get('eventOrAction');

        container.removeAll();

        if (intervalStart) {
            container.add(Ext.create('Skyline.button.TagButton', {
                itemId: 'filter-by-intervalStart',
                text: Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date')
                    + ' ' + Uni.I18n.translate('general.start', 'MDC', 'start').toLowerCase() + ': '
                    + Uni.I18n.formatDate('devicelogbooks.topFilter.tagButton.dateFormat', intervalStart, 'MDC', 'd/m/Y'),
                target: 'intervalStart'
            }));
        }

        if (intervalEnd) {
            container.add(Ext.create('Skyline.button.TagButton', {
                itemId: 'filter-by-intervalEnd',
                text: Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date')
                    + ' ' + Uni.I18n.translate('general.end', 'MDC', 'end').toLowerCase() + ': '
                    + Uni.I18n.formatDate('devicelogbooks.topFilter.tagButton.dateFormat', intervalEnd, 'MDC', 'd/m/Y'),
                target: 'intervalEnd'
            }));
        }

        if (domain) {
            container.add(Ext.create('Skyline.button.TagButton', {
                itemId: 'filter-by-domain',
                text: Uni.I18n.translate('devicelogbooks.domain', 'MDC', 'Domain') + ': ' + domain.get('localizedValue'),
                target: 'domain'
            }));
        }

        if (subDomain) {
            container.add(Ext.create('Skyline.button.TagButton', {
                itemId: 'filter-by-subDomain',
                text: Uni.I18n.translate('devicelogbooks.subDomain', 'MDC', 'Subdomain') + ': ' + subDomain.get('localizedValue'),
                target: 'subDomain'
            }));
        }

        if (eventOrAction) {
            container.add(Ext.create('Skyline.button.TagButton', {
                itemId: 'filter-by-eventOrAction',
                text: Uni.I18n.translate('devicelogbooks.eventOrAction', 'MDC', 'Event or action') + ': ' + eventOrAction.get('localizedValue'),
                target: 'eventOrAction'
            }));
        }
    }
});