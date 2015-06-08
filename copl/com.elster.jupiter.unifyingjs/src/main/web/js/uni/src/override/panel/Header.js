Ext.define('Uni.override.panel.Header', {
    override: 'Ext.panel.Header',

    headingTpl: [
        // unselectable="on" is required for Opera, other browsers inherit unselectability from the header
        '<span id="{id}-textEl" class="{headerCls}-text {cls}-text {cls}-text-{ui}" unselectable="on"',
        '<tpl if="headerRole">',
        ' role="{headerRole}"',
        '</tpl>',
        '>{title:htmlEncode}</span>',
        '<span id="{id}-subTextEl" class="{headerCls}-sub-text {cls}-sub-text {cls}-sub-text-{ui}" unselectable="on"',
        '>{subtitle:htmlEncode}</span>'
    ],

    initComponent: function() {
        var me = this;

        this.callParent(arguments);
        me.titleCmp.childEls.push("subTextEl");
    },

    /**
     * Sets the subtitle of the header.
     * @param {String} subtitle The title to be set
     */
    setSubTitle: function(subtitle) {
        var me = this,
            titleCmp = me.titleCmp;
        me.subtitle = subtitle || ' ';

        if (titleCmp.rendered) {
            titleCmp.subTextEl.update(me.subtitle );
            titleCmp.updateLayout();
        } else {
            me.titleCmp.on({
                render: function() {
                    me.setSubTitle(subtitle);
                },
                single: true
            });
        }
    }
});