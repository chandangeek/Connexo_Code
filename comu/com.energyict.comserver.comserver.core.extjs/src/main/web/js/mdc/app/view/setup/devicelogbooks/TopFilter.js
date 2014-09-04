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
            eventOrAction = filterModel.get('eventOrAction'),
            eventDateText;

        container.removeAll();

        if (intervalStart || intervalEnd) {
            eventDateText = Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date') + ': ';
            if (intervalStart) {
                eventDateText += Uni.I18n.translate('general.from', 'MDC', 'From') + ' '
                    + Uni.I18n.formatDate('devicelogbooks.topFilter.tagButton.dateFormat', intervalStart, 'MDC', 'd/m/Y') + ' ';
            }
            if (intervalEnd) {
                eventDateText += (intervalStart ? Uni.I18n.translate('general.to', 'MDC', 'to').toLowerCase() : Uni.I18n.translate('general.to', 'MDC', 'To')) + ' '
                    + Uni.I18n.formatDate('devicelogbooks.topFilter.tagButton.dateFormat', intervalEnd, 'MDC', 'd/m/Y');
            }
            container.add(Ext.create('Skyline.button.TagButton', {
                itemId: 'filter-by-eventDate',
                text: eventDateText,
                target: ['intervalStart', 'intervalEnd']
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