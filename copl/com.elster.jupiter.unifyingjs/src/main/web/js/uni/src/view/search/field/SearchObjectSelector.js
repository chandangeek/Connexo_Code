Ext.define('Uni.view.search.field.SearchObjectSelector', {
    extend: 'Ext.button.Button',
    xtype: 'search-object-selector',
    style: {
        'background-color': '#71adc7'
    },
    mixins: [
        'Ext.util.Bindable'
    ],
    text: Uni.I18n.translate('search.overview.searchDomains.emptyText', 'UNI', 'Search domains'),
    arrowAlign: 'right',
    menuAlign: 'tl-bl',

    setValue: function(value, suspendEvent) {
        this.value = value;
        if (!Ext.isDefined(suspendEvent)) {
            suspendEvent = false;
        }

        this.menu.items.each(function(item) {
            item.setVisible(true);
        });
        var item = this.menu.items.findBy(function(item){return item.value == value});

        if (item) {
            item.setVisible(false);
            this.setText(item.text);

            if (!suspendEvent) {
                this.fireEvent('change', this, item.value);
            }
        }
    },

    initComponent: function () {
        var me = this;
        me.menu = {
            plain: true,
                enableScrolling: true,
                maxHeight: 350,
                itemId: 'search-object-menu',
                listeners: {
                click: function(cmp, item) {
                    me.setValue(item.value);
                }
            }
        };

        me.callParent(arguments);
        me.bindStore('Uni.store.search.Domains' || 'ext-empty-store', true);
        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    getStoreListeners: function () {
        var me = this;

        return {
            load: me.onStoreLoad
        };
    },

    onStoreLoad: function() {
        var me = this,
            menu = me.menu;

        Ext.suspendLayouts();
        menu.removeAll();
        me.getStore().each(function (item) {
            menu.add({
                text: item.get('displayValue'),
                value: item.get('id')
            })
        });

        if (me.value) {
            me.setValue(me.value);
        }
        Ext.resumeLayouts(true);
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    }
});

