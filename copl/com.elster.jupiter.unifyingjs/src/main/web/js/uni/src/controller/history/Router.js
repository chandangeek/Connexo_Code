/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
 *      locationReplace: true    <<<< [Optional] To indicate you want a window.location.replace() instead of a route.forward()
 *                                               This way, the original route is NOT stored in browser history
 *                                               and hence doesn't introduce browser back-button problems
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
    requires: ['Ext.util.Observable'],

    config: {},

    /**
     * @private
     */
    routes: {},

    defaultAction: 'showOverview',
    currentRoute: null,
    previousRoute: null,

    /**
     * List of route arguments
     */
    arguments: {},

    /**
     * List of query params
     */
    queryParams: {},

    /**
     * Filter instance
     */
    filter: null,

    state: {returnLink: location.href.split('#/')[1] ? location.href.split('#/')[1].split('/')[0] : ''},

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
        var token = Ext.util.History.getToken() || document.location.href.split('?')[1],
            queryStringIndex = token ? token.indexOf('?') : -1;
        return queryStringIndex < 0 ? '' : token.substring(queryStringIndex + 1);
    },

    getQueryStringValues: function () {
        var queryString = this.getQueryString();
        if (typeof queryString !== 'undefined') {
            return Ext.Object.fromQueryString(this.getQueryString());
        }
        return {};
    },

    queryParamsToString: function (obj) {
        return Ext.Object.toQueryString(obj, false);
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
            key: key,
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
                var url = this.buildUrl(arguments, queryParams);
                if (url === window.location.hash) {
                    Ext.util.History.fireEvent('change', Ext.util.History.getToken());
                } else {
                    window.location.assign(url);
                }
            },

            forwardInNewTab: function(arguments, queryParams) {
                var url = this.buildUrl(arguments, queryParams),
                    newWindow = window.open(url, '_blank');
                newWindow.focus();
            }
        });

        if (me.routes[key].callback) {
            me.routes[key].callback.apply(me, [me.routes[key]])
        }

        // register route with crossroads if not disabled
        if (!config.disabled) {
            me.routes[key].crossroad = crossroads.addRoute(route, function () {
                me.fireEvent('routeChangeStart', this);
                me.previousRoute = me.currentRoute;
                me.currentRoute = key;
                // todo: this will not work with optional params
                me.queryParams = Ext.Object.fromQueryString(me.getQueryString());
                me.arguments = _.object(
                    me.routes[key].crossroad._paramsIds,
                    arguments
                );

                var routeArguments = _.values(_.extend(me.arguments, params));

                Object.keys(routeArguments).forEach(function (key) {
                    if (typeof routeArguments[key] === 'string') {
                        try {
                            routeArguments[key] = decodeURIComponent(routeArguments[key]);
                        } catch (e) {
                        }
                    }
                });

                if (Ext.isDefined(config.redirect)) {
                    // perform redirect on route match
                    if (Ext.isObject(config.redirect)) {
                        var redirectParams = _.extend(me.arguments, config.redirect.params);
                        // a case for redirection to different application
                        if (config.redirect.app && Uni.store.Apps.findRecord('name', config.redirect.app)) {
                            var url = Uni.store.Apps.findRecord('name', config.redirect.app).get('url'),
                                route = me.getRoute(config.redirect.route || me.route);
                            url = url + route.buildUrl(redirectParams).substring(1);
                            window.location.replace(url);
                        } else if (!Ext.isEmpty(config.redirect.locationReplace) && config.redirect.locationReplace) {
                            var route = me.getRoute(config.redirect.route || me.route),
                                url = route.buildUrl(redirectParams);
                            window.location.replace(url);
                        } else {
                            me.getRoute(config.redirect.route).forward(redirectParams);
                        }
                    } else if (Ext.isString(config.redirect)) {
                        me.getRoute(config.redirect).forward(me.arguments);
                    } else {
                        throw 'config redirect must be a string or an object';
                    }
                } else {
                    // fire the controller action with this route params as arguments
                    var controller = me.getController(config.controller);

                    var applyAction = function () {
                        me.fireEvent('routematch', me);
                        controller[action].apply(controller, routeArguments);
                    };

                    var dispatch = function () {
                        if (config.haveDependenciesLoaded && !config.haveDependenciesLoaded.call(me)) {
                            me.on('dependenciesLoaded', function () {
                                dispatch();
                            }, me);
                        }
                        else if (!Uni.Auth.checkPrivileges(config.privileges)) {
                            crossroads.parse("/error/notvisible");
                        } else if(me.checkForDynamicPrivileges(config, applyAction)) {
                            //do nothing
                        } else {
                            applyAction();
                        }
                    };

                    // load filter
                    if (config.filter) {
                        Ext.ModelManager.getModel(config.filter).load(null, {
                            callback: function (record) {
                                me.filter = record || Ext.create(config.filter);
                                dispatch();
                            }
                        });
                    } else {
                        dispatch();
                    }
                }
            });
        }

        // handle child items
        if (config.items) {
            _.each(config.items, function (item, itemKey) {

                var path = key + '/' + itemKey;
                me.initRoute(path, item, route);
            });
        }
    },

    checkForDynamicPrivileges: function (config, applyAction) {
        var me = this,
            path = config.key,
            route,
            index;

        while (path.lastIndexOf('/') > -1) {
            route = me.getRoute(path);
            if(route !== me.routes["notfound"]) {
                if (route.dynamicPrivilegeStores) {
                    Uni.DynamicPrivileges.loadPage(route.dynamicPrivilegeStores, config.dynamicPrivilege, applyAction, me);
                    return true;
                }
            }
            index = path.lastIndexOf('/');
            if (index !== -1) {
                path = path.substring(0, index);
            }
        }
        return false;
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
    getRoute: function (path, state) {
        var me = this;
        if (state) {
            this.state = state;
        }
        if (!Ext.isDefined(path)) {
            path = me.currentRoute;
        }

        return me.routes[path] || me.routes["notfound"]; // add fallback route, to prevent crashes when calling buildUrl method.
    },

    getRouteConfig: function (path) {
        var me = this,
            route = me.config;
        path = path.split('/');

        do {
            var item = path.shift();
            route = route[item];
            if (item !== 'items' && path.length) path.splice(0, 0, 'items');
        } while (path.length);

        return route;
    },

    setState: function (state) {
        this.state = state;
    },

    getState: function (state) {
        return this.state;
    }
});