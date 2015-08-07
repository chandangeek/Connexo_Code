/**
 * @class Uni.util.QueryString
 */
Ext.define('Uni.util.QueryString', {
    singleton: true,

    mixins: {
        observable: 'Ext.util.Observable'
    },

    buildQueryString: function (config, recursive) {
        var me = this,
            queryString = me.getQueryString(),
            queryObject;

        recursive = typeof recursive === 'undefined' ? true : recursive;
        queryObject = Ext.Object.fromQueryString(queryString, recursive);

        Ext.apply(queryObject, config || {});

        queryObject = me.cleanQueryObject(queryObject);
        return Ext.Object.toQueryString(queryObject, recursive);
    },

    /**
     * Cleans up a query object by removing undefined parameters.
     *
     * @param queryObject
     * @returns {Object} Cleaned up query object
     */
    cleanQueryObject: function (queryObject) {
        var queryObjectCopy = Ext.clone(queryObject || {});

        for (var key in queryObject) {
            if (queryObject.hasOwnProperty(key) && (!Ext.isDefined(queryObject[key]) || Ext.isEmpty(queryObject[key]))) {
                delete queryObjectCopy[key];
            }
        }

        return queryObjectCopy;
    },

    buildHrefWithQueryString: function (config, recursive) {
        var me = this,
            url = location.href.split('?')[0],
            queryString = me.buildQueryString(config, recursive);

        if (Ext.isEmpty(queryString)) {
            return url;
        }

        return url + '?' + queryString;
    },

    getQueryString: function () {
        var token = Ext.util.History.getToken() || document.location.href.split('?')[1],
            queryStringIndex = token.indexOf('?');
        return queryStringIndex < 0 ? '' : token.substring(queryStringIndex + 1);
    },

    getQueryStringValues: function (recursive) {
        recursive = typeof recursive === 'undefined' ? true : recursive;

        return Ext.Object.fromQueryString(this.getQueryString(), recursive);
    },

    changed: function(queryString){
        this.fireEvent('querystringchanged', queryString);
    },

    constructor: function(){
        this.mixins.observable.constructor.call(this);
    }
});