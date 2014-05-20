Ext.define('Skyline.window.MessageBox', {
    override: 'Ext.window.MessageBox',

    iconHeight: 0,
    iconWidth: 0,

    initComponent: function () {
        this.callParent(arguments);
        this.topContainer.padding = 0;
    },

    /**
     * Set the icon for the panel's header. See {@link Ext.panel.Header#icon}. It will fire the
     * {@link #iconchange} event after completion.
     * @param {String} iconCls The new icon path
     */
    setIcon: function(iconCls) {
        var me = this,
            oldIcon = me.iconCls,
            header = me.header,
            placeholder = me.placeholder;

        me.iconCls = iconCls;
        if (header) {
            if (header.isHeader) {
                header.setIconCls(iconCls);
            } else {
                header.iconCls = iconCls;
            }
        } else {
            me.updateHeader();
        }

        if (placeholder && placeholder.setIconCls) {
            placeholder.setIconCls(iconCls);
        }

        me.fireEvent('iconclschange', me, iconCls, oldIcon);
    }
});
