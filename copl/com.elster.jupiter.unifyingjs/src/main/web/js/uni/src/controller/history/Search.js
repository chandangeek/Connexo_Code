Ext.define('Uni.controller.history.Search', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'search',

    routeConfig: {
        search: {
            title: Uni.I18n.translate('title.search', 'UNI', 'Search'),
            route: 'search',
            controller: 'Uni.controller.Search',
            action: 'showOverview'
        }
    }
});
