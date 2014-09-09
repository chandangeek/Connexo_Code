/**
 * The router component is responsible for gathering and handling route configuration
 *
 * @class Uni.controller.history.Router
 *
 * Route confipuration parameters:
 *
 * title: string or function
 * route: the part of route, srr crossroads for example.
 * controller: specify the controller which will handle the route
 * action: (optional) action of the controller which will be fired on route match. If not specified defaultAction will be used.
 * disabled: (optional) if true the route will not be registered with crossroads
 * items: (optional) the set of child routes
 * params: (optional) the set of optional parameters which will be passed to the action
 * redirect: string|object specify string route for redirect or an object in following format:
 * {
 *      route: 'route/to/redirect/{paramId}',
 *      params: {paramId: 42}
 * }
 *
 * For creating URL in the application use the following example:
 *
 * router.getRoute('/app/item/view').getTitle() # for title
 * router.getRoute('app/item/view').buildUrl({id: 1}) # example output: /application/item/1
 *
 * Router will fire event 'routematch' with the Router component as parameter when the route is activated.
 * Example of route configuration:
 *
 * router.addConfig({
 *      administration : {
 *          title: 'Administration',
 *          route: 'administration',
 *          disabled: true,
 *          items: {
 *              issue: {
 *                  title: 'Issue',
 *                  route: 'issue',
 *                  items: {
 *                      assignmentrules: {
 *                          title: 'Assignment Rules',
 *                          route: 'assignmentrules',
 *                          controller: 'Isu.controller.IssueAssignmentRules'
 *                      },
 *                      creationrules: {
 *                          title: 'Creation Rules',
 *                          route: 'creationrules',
 *                          controller: 'Isu.controller.IssueCreationRules',
 *                          items: {
 *                              create: {
 *                                  title: 'Create',
 *                                  route: '/create',
 *                                  controller: 'Isu.controller.IssueCreationRulesEdit'
 *                              },
 *                              edit: {
 *                                  title: 'Edit',
 *                                  route: '{id}/edit',
 *                                  controller: 'Isu.controller.IssueCreationRulesEdit'
 *                              }
 *                          }
 *                      }
 *                  }
 *              },
 *              communicationtasks: {
 *                  title: 'Communication Tasks',
 *                  route: 'communicationtasks',
 *                  controller: 'Isu.controller.CommunicationTasksView',
 *                  items: {
 *                      create: {
 *                          title: 'Create',
 *                          route: '/create',
 *                          controller: 'Isu.controller.CommunicationTasksEdit'
 *                      },
 *                      edit: {
 *                          title: 'Edit',
 *                          route: '{id}',
 *                          controller: 'Isu.controller.CommunicationTasksEdit'
 *                      }
 *                  }
 *              }
 *          }
 *      }
 *  })
 *
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
     * List of route arguments
     */
    arguments: {},

    /**
     * List of query params
     */
    queryParams: {},

    /**
     * Add router configuration
     * @param config
     */
    addConfig: function (config) {
        _.extend(this.config, config);

        var me = this;
        _.each(config, function (item, key) {
            me.initRoute(key, item);
        });
    },

    getQueryString: function () {
        var token = Ext.util.History.getToken(),
            queryStringIndex = token.indexOf('?');
        return queryStringIndex < 0 ? '' : token.substring(queryStringIndex + 1);
    },

    queryParamsToString: function (obj) {
        return Ext.urlEncode(_.object(_.keys(obj), _.map(obj, function (i) {
            return _.isString(i) ? i : Ext.JSON.encodeValue(i)
        })))
    },

    /**
     * @private
     * @param key string
     * @param config Object
     * @param prefix string|null
     */
    initRoute: function (key, config, prefix) {
        var me = this;
        prefix = typeof prefix !== 'undefined' ? prefix : '';
        var route = prefix + '/' + config.route;
        var action = typeof config.action !== 'undefined' ? config.action : me.defaultAction;
        var params = typeof config.params !== 'undefined' ? config.params : {};

        // register route within controller
        // todo: move route class to external entity.
        me.routes[key] = _.extend(config, {
            path: route,

            /**
             * Return title of the route
             * @returns string
             */
            getTitle: function () {
                var route = this;
                return _.isFunction(this.title)
                    ? this.title.apply(me, [route])
                    : this.title;
            },

            setTitle: function (title) {
                this.title = title;
                me.fireEvent('routechange', this);
            },

            /**
             * returns URL builded with provided arguments and query string parameters
             *
             * @param arguments
             * @param queryParams
             *
             * @returns {string}
             */
            buildUrl: function (arguments, queryParams) {
                arguments = Ext.applyIf(arguments || {}, me.arguments);
                queryParams = Ext.applyIf(queryParams || {}, me.queryParams);
                var url = this.crossroad ?
                    '#' + this.crossroad.interpolate(arguments) :
                    '#' + this.path;
                return _.isEmpty(queryParams) ? url : url + '?' + me.queryParamsToString(queryParams);
            },

            /**
             * @param arguments
             * @param queryParams
             */
            forward: function (arguments, queryParams) {
                window.location.href = this.buildUrl(arguments, queryParams);
            }
        });

        if (me.routes[key].callback) {
            me.routes[key].callback.apply(me, [me.routes[key]])
        }

        // register route with crossroads if not disabled
        if (!config.disabled) {
            me.routes[key].crossroad = crossroads.addRoute(route, function () {
                me.currentRoute = key;

                // todo: this will not work with optional params
                me.queryParams = Ext.Object.fromQueryString(me.getQueryString());
                me.arguments = _.object(
                    me.routes[key].crossroad._paramsIds,
                    arguments
                );

                var arguments = _.values(_.extend(me.arguments, params));
                if (Ext.isDefined(config.redirect)) {
                    // perform redirect on route match
                    if (Ext.isObject(config.redirect)) {
                        var redirectParams = _.extend(me.arguments, config.redirect.params);
                        me.getRoute(config.redirect.route).forward(redirectParams);
                    } else if (Ext.isString(config.redirect)) {
                        me.getRoute(config.redirect).forward(me.arguments);
                    } else {
                        throw 'config redirect must be a string or an object';
                    }
                } else {
                    // fire the controller action with this route params as arguments
                    var controller = me.getController(config.controller);
                    controller[action].apply(controller, arguments);
                }

                me.fireEvent('routematch', me);
            });
        }

        // handle child items
        if (config.items) {
            _.each(config.items, function (item, itemKey) {
                if (typeof item.authorized === 'undefined' || item.authorized) {
                    var path = key + '/' + itemKey;
                    me.initRoute(path, item, route);
                }
            });
        }
    },

    /**
     * Builds breadcrumbs data based on path
     * @param path
     * @returns [Route]
     */
    buildBreadcrumbs: function (path) {
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
     * Route object have following api:
     * getTitle() - returns the title of the route
     * buildUrl(arguments) - builds URl with provided arguments
     * @param path
     * @returns Route
     */
    getRoute: function (path) {
        var me = this;

        if (!Ext.isDefined(path)) {
            path = me.currentRoute;
        }

        return me.routes[path];
    },

    getRouteConfig: function (path) {
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