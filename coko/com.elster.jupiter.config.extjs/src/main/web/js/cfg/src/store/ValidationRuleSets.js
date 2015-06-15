Ext.define('Cfg.store.ValidationRuleSets', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.ValidationRuleSet',
    pageSize: 10,

    storeId: 'ValidationRuleSets',

    listeners: {
        'beforeLoad': function () {
            var extraParams = this.proxy.extraParams;
            // replace filter extra params with new ones
            if (this.proxyFilter) {
                extraParams = _.omit(extraParams, this.proxyFilter.getFields());
                Ext.merge(extraParams, this.getFilterParams());
            }

            this.proxy.extraParams = extraParams;
        }
    },

    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        reader: {
            type: 'json',
            root: 'ruleSets',
            totalProperty: 'total'
        },
		buildUrl: function (request) {
            var me = this,
                format = me.format,
                url = me.getUrl(request),
                ruleSetId = request.params.ruleSetId;
            
			if (ruleSetId){
				if (!url.match(/\/$/)) {
					url += '/';
				}

				url += ruleSetId;
				
				if (format) {
					if (!url.match(/\.$/)) {
						url += '.';
					}

					url += format;
				}
			}
			
			if (me.noCache) {
				url = Ext.urlAppend(url, Ext.String.format("{0}={1}", me.cacheString, Ext.Date.now()));
			}

			request.url = url;
			
            return url;
        }

    }
});