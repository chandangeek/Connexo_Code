Ext.define('Uni.store.Translations', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.Translation',
    storeId: 'translations',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,
    remoteFilter: false,

    config: {
        baseComponents: ['UNI'],
        components: []
    },

    proxy: {
        type: 'ajax',
        url: '/api/nls/thesaurus',
        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,

        reader: {
            type: 'json',
            root: 'translations'
        },

        buildUrl: function (request) {
            var baseComponents = Uni.store.Translations.getBaseComponents(),
                components = Uni.store.Translations.getComponents();

            request.params.cmp = _.union(baseComponents, components);

            var me = this,
                format = me.format,
                url = me.getUrl(request),
                id = request.params.id;

            if (!url.match(/\/$/)) {
                url += '/';
            }

            if (typeof id !== 'undefined') {
                url += id;
            }

            if (format) {
                if (!url.match(/\.$/)) {
                    url += '.';
                }

                url += format;
            }

            if (me.noCache) {
                url = Ext.urlAppend(url, Ext.String.format("{0}={1}", me.cacheString, Ext.Date.now()));
            }

            request.url = url;

            return url;
        }
    }
});