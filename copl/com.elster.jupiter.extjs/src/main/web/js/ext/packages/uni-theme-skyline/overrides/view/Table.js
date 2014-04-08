Ext.define('Skyline.view.Table', {
    override: 'Ext.view.Table',
    scroll: true,
    scrollbarTpl: '<div class="scrollbar"><div class="up"><span class="arrow-up"></span></div><div class="down"><span class="arrow-down"></span></div><div class="track"><div class="thumb"></div></div></div>',

    // todo: refactor this
    listeners: {
        refresh: function () {
            var body = this.getEl().parent('.x-grid-body');
            body.update(body.getHTML() + this.scrollbarTpl);
            if (body.down('.x-grid-view')) {
                body.down('.x-grid-view').addCls('viewport');

                this.getEl().down('.x-grid-table').addCls('overview');

                var $scrollbar = body.dom,
                    scrollbar  = tinyscrollbar($scrollbar, {
                        trackSize: body.down('.track').dom.offsetHeight
                    });
            }

        }
    }
});
