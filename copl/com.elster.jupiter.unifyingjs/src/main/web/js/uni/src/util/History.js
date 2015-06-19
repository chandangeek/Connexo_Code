/**
 * @class Uni.util.History
 */
Ext.define('Uni.util.History', {
    singleton: true,

    routerController: 'Uni.controller.history.Router',

    requires: [
        'Uni.util.Application'
    ],

    config: {
        suspended: false
    },

    suspendEventsForNextCall: function () {
        if (!this.isSuspended()) {
            this.setSuspended(true);
        }
        //var currentHref = location.href;
        //
        //Ext.util.History.suspendEvents();
        //
        //new Ext.util.DelayedTask(function () {
        //    if (location.href !== currentHref) {
        //        Ext.util.History.resumeEvents();
        //        this.stopped = true;
        //    }
        //}).delay(100);
    },

     isSuspended: function() {
        return this.suspended;
    },

    setSuspended: function(suspend) {
        this.suspended = suspend;
    },

    getRouterController: function () {
        var me = this,
            appPath = Ext.String.htmlEncode(Uni.util.Application.appPath),// Better safe than sorry, so encoding these.
            namespace = Ext.String.htmlEncode(Uni.util.Application.getAppNamespace()),
            evalCode = namespace + '.' + appPath + '.getController(\'' + me.routerController + '\')';

        if (typeof namespace !== 'undefined') {
            try {
                return eval(evalCode + ';');
            } catch (ex) {
                return evalCode;
            }
        }

        return Ext.create(me.routerController);
    }
});