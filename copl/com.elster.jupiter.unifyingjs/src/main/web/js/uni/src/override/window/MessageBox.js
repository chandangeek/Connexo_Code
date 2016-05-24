Ext.define('Uni.override.window.MessageBox', {
    override: 'Ext.window.MessageBox',
    shadow: false,

    title: ' ',
    htmlEncode: true,

    reconfigure: function (cfg) {
        if (((typeof cfg) != 'undefined') && cfg.ui) {
            this.ui = cfg.ui;
        }
        this.callParent(arguments);
    },

    initComponent: function () {
        var me = this,
            title = me.title,
            baseId = me.id,
            i, button;

        me.title = ' ';

        me.topContainer = new Ext.container.Container({
            layout: 'hbox',
            padding: 10,
            style: {
                overflow: 'hidden'
            },
            items: [
                me.iconComponent = new Ext.Component({
                    width: me.iconWidth,
                    height: me.iconHeight
                }),
                me.promptContainer = new Ext.container.Container({
                    flex: 1,
                    layout: 'anchor',
                    items: [
                        me.msg = new Ext.form.field.Display({
                            id: baseId + '-displayfield',
                            cls: me.baseCls + '-text',
                            htmlEncode: me.htmlEncode
                        }),
                        me.textField = new Ext.form.field.Text({
                            id: baseId + '-textfield',
                            anchor: '100%',
                            enableKeyEvents: true,
                            listeners: {
                                keydown: me.onPromptKey,
                                scope: me
                            }
                        }),
                        me.textArea = new Ext.form.field.TextArea({
                            id: baseId + '-textarea',
                            anchor: '100%',
                            height: 75
                        })
                    ]
                })
            ]
        });
        me.progressBar = new Ext.ProgressBar({
            id: baseId + '-progressbar',
            margins: '0 10 10 10'
        });

        me.items = [me.topContainer, me.progressBar];

        // Create the buttons based upon passed bitwise config
        me.msgButtons = [];
        for (i = 0; i < 4; i++) {
            button = me.makeButton(i);
            me.msgButtons[button.itemId] = button;
            me.msgButtons.push(button);
        }
        me.bottomTb = new Ext.toolbar.Toolbar({
            id: baseId + '-toolbar',
            ui: 'footer',
            dock: 'bottom',
            layout: {
                pack: 'center'
            },
            items: [
                me.msgButtons[0],
                me.msgButtons[1],
                me.msgButtons[2],
                me.msgButtons[3]
            ]
        });
        me.dockedItems = [me.bottomTb];
        me.on('close', me.onClose, me);
        me.callSuper();

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
