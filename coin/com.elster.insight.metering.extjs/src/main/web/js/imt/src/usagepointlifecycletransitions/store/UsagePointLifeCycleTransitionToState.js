Ext.define('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionToState', {
    extend: 'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionFromState',
    constructor: function () {
        var me = this,
            parent;
        me.callParent(arguments);
        parent = Ext.getStore('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionFromState');
        parent.on('load', function (store, records) {
            me.loadData(records);
        }, me);
    }
});
