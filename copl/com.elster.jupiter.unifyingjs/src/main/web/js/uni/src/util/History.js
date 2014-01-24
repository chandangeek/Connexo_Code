/**
 * @class Uni.util.History
 */
Ext.define('Uni.util.History', {
    singleton: true,

    suspendEventsForNextCall: function () {
        var currentHref = location.href;

        Ext.util.History.suspendEvents();

        Ext.TaskManager.start({
            run: function () {
                if (location.href !== currentHref) {
                    Ext.util.History.resumeEvents();
                    this.stopped = true;
                }
            },
            interval: 100
        });
    }
});