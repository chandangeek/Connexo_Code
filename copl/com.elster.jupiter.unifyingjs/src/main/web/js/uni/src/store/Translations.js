Ext.define('Uni.store.Translations', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.Translation',
    storeId: 'translations',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,

    config: {
        locale: 'en-GB',
        component: 'all'
    },

    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    }

    // TODO Enable when the API for it is available.
//    proxy: {
//        type: 'ajax',
//        url: '/api/i18n', // TODO Use the correct URL when the API for it is available.
//        pageParam: undefined,
//        limitParam: undefined,
//        startParam: undefined,
//        reader: {
//            type: 'json',
//            root: 'items'
//        },
//        buildUrl: function (request) {
//            request.params.locale = Uni.store.Translations.getLocale();
//            request.params.component = Uni.store.Translations.getComponent();
//
//            var me = this,
//                format = me.format,
//                url = me.getUrl(request),
//                id = request.params.id;
//
//            if (!url.match(/\/$/)) {
//                url += '/';
//            }
//
//            url += id;
//
//            if (format) {
//                if (!url.match(/\.$/)) {
//                    url += '.';
//                }
//
//                url += format;
//            }
//
//            if (me.noCache) {
//                url = Ext.urlAppend(url, Ext.String.format("{0}={1}", me.cacheString, Ext.Date.now()));
//            }
//
//            request.url = url;
//
//            return url;
//        }
//    }
});