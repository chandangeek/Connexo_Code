Ext.define('Uni.override.panel.Panel', {
    override: 'Ext.panel.Panel',

    initComponent: function() {
        if (this.ui === 'large'){
            this.title = this.title || ' ';
        }
        this.callParent(arguments);
    },

    beforeRender: function () {
        var me = this;
        this.callParent(arguments);

        if (me.subtitle) {
            this.setSubTitle(me.subtitle);
        }
    },

    /**
     * Set a title for the panel's header. See {@link Ext.panel.Header#title}.
     * @param {String} subtitle
     */
    setSubTitle: function (subtitle) {
        var me = this,
            header = me.header;

        me.subtitle = subtitle;

        if (header) {
            if (header.isHeader) {
                header.setSubTitle(subtitle);
            } else {
                header.subtitle = subtitle;
            }
        } else if (me.rendered) {
            me.updateHeader();
        }
    }
});