/**
 * @class Uni.util.QueryString
 */
Ext.define('Uni.util.QueryString', {
    singleton: true,

    buildQueryString: function (config) {
        var me = this,
            queryString = me.getQueryString(),
            queryObject = Ext.Object.fromQueryString(queryString);

        Ext.apply(queryObject, config || {});
        return Ext.Object.toQueryString(queryObject);
    },

    buildHrefWithQueryString: function (config) {
        var me = this,
            url = location.href.split('?')[0],
            queryString = me.buildQueryString(config);
        return url + '?' + queryString;
    },

    getQueryString: function () {
        var token = Ext.util.History.getToken(),
            queryStringIndex = token.indexOf('?');
        return queryStringIndex < 0 ? '' : token.substring(queryStringIndex + 1);
    },

    getQueryStringValues: function () {
        return Ext.Object.fromQueryString(this.getQueryString());
    }
});