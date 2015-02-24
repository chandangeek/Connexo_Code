Ext.define('Uni.override.grid.plugin.BufferedRenderer', {
    override: 'Ext.grid.plugin.BufferedRenderer',
    rowHeight: 29, // comes from skyline theme

    init: function (grid) {
        this.callParent(arguments);
        grid.view.cls = 'uni-infinite-scrolling-grid-view';
        // grid height calculated before the toolbar is on layouts, it causes the bug: JP-3817
        grid.on('boxready', function () {
            grid.view.refresh();
        });
    },

    bindStore: function (store) {
        var me = this;

        // TODO Check below!
        // TEMPORARY Disabling some things to get the workaround retested!

        //me.trailingBufferZone = 0; // No idea why this needs to be 0, without it some grids don't work.
        //me.leadingBufferZone = store.pageSize;

        me.callParent(arguments);
    }

    //,
    //
    //onViewScroll: function (e, t) {
    //    var me = this,
    //        store = me.store,
    //        totalCount = (store.buffered ? store.getTotalCount() : store.getCount()),
    //        vscrollDistance,
    //        scrollDirection,
    //        scrollTop = me.scrollTop = me.view.el.dom.scrollTop,
    //        scrollHandled = false;
    //
    //    if (me.ignoreNextScrollEvent) {
    //        me.ignoreNextScrollEvent = false;
    //        return;
    //    }
    //
    //    if (!(me.disabled || totalCount < me.viewSize)) {
    //        vscrollDistance = scrollTop - me.position;
    //        scrollDirection = vscrollDistance >= 0 ? 1 : -1;
    //        if (Math.abs(vscrollDistance) >= 20 || (scrollDirection !== me.lastScrollDirection)) {
    //            me.lastScrollDirection = scrollDirection;
    //            me.handleViewScroll(me.lastScrollDirection);
    //            scrollHandled = true;
    //        }
    //    }
    //
    //    if (!scrollHandled) {
    //        if (me.lockingPartner && me.lockingPartner.scrollTop !== scrollTop) {
    //            me.lockingPartner.view.el.dom.scrollTop = scrollTop;
    //        }
    //    }
    //}
});