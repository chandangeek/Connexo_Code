Ext.define('Uni.override.panel.Header', {
    override: 'Ext.panel.Header',

    htmlEncode: true,
    titleIsShrinked: false,

    initComponent: function() {
        var me = this;

        me.headingTpl = [
            // unselectable="on" is required for Opera, other browsers inherit unselectability from the header
            '<span id="{id}-textEl" class="{headerCls}-text {cls}-text {cls}-text-{ui}" unselectable="on"',
            '<tpl if="headerRole">',
            ' role="{headerRole}"',
            '</tpl>',
            me.htmlEncode ? '>{title:htmlEncode}</span>' : '>{title}</span>',
            '<span id="{id}-subTextEl" class="{headerCls}-sub-text {cls}-sub-text {cls}-sub-text-{ui}" unselectable="on"',
            me.htmlEncode ? '>{subtitle:htmlEncode}</span>' : '>{subtitle}</span>'
        ];

        this.callParent(arguments);
        if (me.titleIsShrinked) {
            me.titleCmp.flex = undefined;
            me.titleCmp.style += ' margin-right: 10px'
        }
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