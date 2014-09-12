/**
 * @class Uni.util.History
 */
Ext.define('Uni.util.History', {
    singleton: true,

    suspendEventsForNextCall: function () {
        var currentHref = location.href;

        Ext.util.History.suspendEvents();

        new Ext.util.DelayedTask(function () {
                if (location.href !== currentHref) {
                    Ext.util.History.resumeEvents();
                    this.stopped = true;
                }
            }).delay(100);
    }
});