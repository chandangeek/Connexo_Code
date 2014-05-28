/**
 * The router component is responsible for gathering and handling route configuration
 *
 * @class Uni.controller.history.Router
 *
 * todo: example
 */
Ext.define('Uni.controller.history.Router', {
    extend: 'Ext.app.Controller',

    config: {},

    /**
     * @private
     */
    routes: {},

    defaultAction: 'showOverview',
    currentRoute: null,

    /**
     * Add router configutarion
     * @param config
     */
    addConfig: function(config) {
        _.extend(this.config, config);

        var me = this;
        _.each(this.config, function(item, key) {
            me.initRoute(key, item);
        });
    },

    /**
     * @private
     * @param key string
     * @param config Object
     * @param prefix string|null
     */
    initRoute: function(key, config, prefix) {
        var me = this;
        prefix = typeof prefix !== 'undefined' ? prefix : '';
        var route = prefix + '/' + config.route;
        var action = typeof config.action !== 'undefined' ? config.action : me.defaultAction;

        // register route within controller
        // todo: move route class to external entity.
        me.routes[key] = _.extend(config, {
            path: route,

            /**
             * Return title of the route
             * @returns string
             */
            getTitle: function() {
                var me = this;
                return _.isFunction(this.title)
                    ? this.title.apply(me, this)
                    : this.title;
            },

            /**
             * returns URL builded with provided parameters
             * @param params
             * @returns {string}
             */
            buildUrl: function (params) {
                params = typeof params !== 'undefined' ? params : this.params;
                return this.crossroad ?
                    '#' + this.crossroad.interpolate(params) :
                    '#' + this.path;
            }
        });

        // register route with crossroads if not disabled
        if (!config.disabled) {
            me.routes[key].crossroad = crossroads.addRoute(route, function() {
                me.currentRoute = key;

                // todo: this will not work with optional params
                me.routes[key].params = _.object(
                    me.routes[key].crossroad._paramsIds,
                    arguments
                );
                var controller = me.getController(config.controller);

                // fire the controller action with this route params as arguments
                controller[action].apply(controller, arguments);
                me.fireEvent('routematch', me);
            });
        }

        // handle child items
        if (config.items) {
            _.each(config.items, function(item, itemKey){
                var path = key + '/' + itemKey;
                me.initRoute(path, item, route);
            });
        }
    },

    /**
     * Bulds breadcrums data based on path
     * @param path
     * @returns {Array}
     */
    buildBreadcrumbs: function(path) {
        var me = this;
        path = typeof path === 'undefined'
            ? me.currentRoute.split('/')
            : path.split('/');

        var items = [];
        do {
            var route = me.getRoute(path.join('/'));
            items.push(route);
            path.pop();
        } while (path.length);

        return items;
    },

    /**
     * return the route via alias
     * @param path
     * @returns {*}
     */
    getRoute: function(path) {
        var me = this;
        return me.routes[path];
    },

    getRouteConfig: function(path) {
        var route = me.routeConfig;
        path = path.split('/');

        do {
            var item = path.shift();
            route = route[item];
            if (item !== 'items' && path.length) path.splice(0, 0, 'items');
        } while (path.length);

        return route;
    }
});