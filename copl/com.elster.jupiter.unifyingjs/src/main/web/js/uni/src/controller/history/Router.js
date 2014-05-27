/**
 * @class Uni.controller.history.Router
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

    refs: [
        {
            ref: 'breadcrumbs',
            selector: 'contentcontainer breadcrumbTrail'
        }
    ],

    addConfig: function(config) {
        _.extend(this.config, config);

        var me = this;
        _.each(this.config, function(item, key) {
            me.initRoute(key, item);
        });
    },

    initRoute: function(key, config, prefix) {
        var me = this;
        prefix = typeof prefix !== 'undefined' ? prefix : '';
        var route = prefix + '/' + config.route;
        var action = typeof config.action !== 'undefined' ? config.action : me.defaultAction;

        me.routes[key] = _.extend(config, {
            path: route,
            getTitle: function() {
                var me = this;
                return _.isFunction(this.title)
                    ? this.title.apply(me, this)
                    : this.title;
            },
            buildUrl: function (params) {
                params = typeof params !== 'undefined' ? params : this.params;
                return this.crossroad ?
                    this.crossroad.interpolate(params) :
                    this.path;
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
                controller[action].apply(controller, arguments);
                me.renderBreadcrumbs();
            });
        }

        if (config.items) {
            _.each(config.items, function(item, itemKey){
                var path = key + '/' + itemKey;
                me.initRoute(path, item, route);
            });
        }
    },

    buildBreadcrumbs: function(path) {
        var me = this;
        path = typeof path === 'undefined'
            ? me.currentRoute.split('/')
            : path.split('/');

        var items = [];
        do {
            var route = me.getRoute(path.join('/'));
            var item = {
                title: route.getTitle(),
                href: '#' + route.buildUrl()
            };
            items.push(item);
            path.pop();
        } while (path.length);

        return items;
    },

    /**
     * todo: this function is out of responsibility of router and should be moved
     */
    renderBreadcrumbs: function() {
        var me = this;
        var breadcrumbs = this.getBreadcrumbs();
        var child;

        breadcrumbs.removeAll();
        _.map(me.buildBreadcrumbs(), function(item) {
            var breadcrumb = Ext.create('Uni.model.BreadcrumbItem', {
                text: item.title,
                href: child ? item.href.replace(child.href, '') : item.href
            });
            if (child) {
                breadcrumb.setChild(child);
            }
            breadcrumbs.setBreadcrumbItem(breadcrumb);
            child = breadcrumb;
        });
    },

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