Ext.define('Isu.view.workspace.issues.CommentsList', {
    extend: 'Ext.container.Container',
    requires: [
        'Isu.view.ext.button.Action'
    ],
    alias: 'widget.issue-comments',
    title: 'Comments',
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    cls: 'isu-comments-list',

    initComponent: function () {
        var me = this;

        me.bindStore(me.store || 'ext-empty-store', true);

        this.callParent(arguments);

        me.store.on('add', me.onLoad, me);
    },

    beforeRender: function () {
        this.callParent(arguments);
        if (!this.store.isLoading()) {
            this.onLoad();
        }
    },

    onLoad: function () {
        var me = this;

        me.removeAll();

        if (!me.store.getCount()) {
            me.add({
                html: '<h3>There are no comments yet on this issue</h3>',
                border: false
            });
            return;
        }

        Ext.Array.forEach(me.store.getRange(), function (item, index) {
            me.addcomment(item.data);
        });
    },

    addcomment: function (data) {
        return this.add({
            xtype: 'container',
            cls: 'isu-comments-item',
            data: data,
            tpl: new Ext.XTemplate(
                '<p><span class="isu-icon-USER"></span><b>{author.name}</b> added a comment - {[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</p>',
                '<p>{comment}</p>',
                {
                    formatCreationDate: function (date) {
                        date = Ext.isDate(date) ? date : new Date(date);
                        return Ext.Date.format(date, 'M d, Y (H:i)');
                    }
                }
            )
        });
    },

    getStoreListeners: function () {
        return {
            load: this.onLoad
        };
    }
});