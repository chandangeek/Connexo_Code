Ext.define('Uni.override.window.MessageBox', {
    override: 'Ext.window.MessageBox',
    shadow: false,

    title: ' ',

    reconfigure: function (cfg) {
        if (((typeof cfg) != 'undefined') && cfg.ui) {
            this.ui = cfg.ui;
        }
        this.callParent(arguments);
    },

    initComponent: function () {
        var me = this,
            title = me.title;

        me.title = ' ';

        this.callParent(arguments);
        this.topContainer.padding = 0;

        me.titleComponent = new Ext.panel.Header({
            title: title
        });
        me.promptContainer.insert(0, me.titleComponent);
    },

    /**
     * Set a title for the panel's header. See {@link Ext.panel.Header#title}.
     * @param {String} newTitle
     */
    setTitle: function (newTitle) {
        var me = this,
            header = me.titleComponent;

        if (header) {
            var oldTitle = header.title;
        }

        if (header) {
            if (header.isHeader) {
                header.setTitle(newTitle);
            } else {
                header.title = newTitle;
            }
        }
        else if (me.rendered) {
            me.updateHeader();
        }

        me.fireEvent('titlechange', me, newTitle, oldTitle);
    }
}, function () {
    /**
     * @class Ext.MessageBox
     * @alternateClassName Ext.Msg
     * @extends Ext.window.MessageBox
     * @singleton
     * Singleton instance of {@link Ext.window.MessageBox}.
     */
    Ext.MessageBox = Ext.Msg = new this();
});
