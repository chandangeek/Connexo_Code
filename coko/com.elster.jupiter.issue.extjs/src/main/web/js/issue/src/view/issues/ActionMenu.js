Ext.define('Isu.view.issues.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issues-action-menu',
    store: 'Isu.store.IssueActions',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    minHeight: 60,
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    predefinedItems: [
        {
            text: Uni.I18n.translate('issues.actionMenu.addComment', 'ISU', 'Add comment'),
            isPredefined: true,
            action: 'addComment'
        }
    ],
    listeners: {
        show: {
          fn: function () {
              var me = this;

              me.removeAll();
              if (me.record) {
                  me.store.getProxy().url = me.record.getProxy().url + '/' + me.record.getId() + '/actions';
                  me.store.load();
                  setTimeout(function () {
                      me.setLoading(true);
                  },1)
              } else {
                  //<debug>
                  console.error('Record for \'' + me.xtype + '\' is not defined');
                  //</debug>
              }
          }
      }
    },
    initComponent: function () {
        var me = this;

        me.bindStore(me.store || 'ext-empty-store', true);

        this.callParent(arguments);
    },

    onLoad: function () {
        var me = this;

        me.removeAll();
        me.store.each(function (record) {
            me.add({
                text: record.get('name'),
                actionRecord: record
            });
        });
        if (me.predefinedItems && me.predefinedItems.length) {
            me.add(me.predefinedItems);
        }
        me.setLoading(false);
    },

    getStoreListeners: function () {
        return {
            load: this.onLoad
        };
    }
});