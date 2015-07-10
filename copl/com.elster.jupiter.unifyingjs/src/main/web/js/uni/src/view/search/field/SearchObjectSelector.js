Ext.define('Uni.view.search.field.SearchObjectSelector', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-view-search-field-search-object-selector',
    xtype: 'uni-view-search-field-search-object-selector',
    layout: 'hbox',

    initComponent: function () {
        var me = this;
        var store = Ext.getStore('Uni.store.search.Domains' || 'ext-empty-store');
        this.items = [
            {
                xtype: 'button',
                style: {
                    'background-color': '#71adc7'
                },
                itemId: 'domain',
                text: Uni.I18n.translate('search.overview.searchDomains.emptyText', 'UNI', 'Search domains'),
                arrowAlign: 'right',
                menuAlign: 'tl-bl',
                menu: {
                    enableScrolling: true,
                    maxHeight: 350,
                    itemId: 'mnu-domain',
                    listeners: {
                        click: function (cmp, item) {

                            //me.router.getRoute().forward(null, {searchDomain: item.value});
                            //debugger;
                           // me.scope.filter.set('domain', item.value);
                           // me.scope.filter.save();
                        }
                    }
                },
                setValue: function(value) {
                    var item = this.menu.items.findBy(function(item){return item.value == value});
                    if (item) {
                        item.setActive();
                        this.setText(item.text);
                        this.fireEvent('change', this);
                        }

                }
            }
        ];

        this.callParent(arguments);

        var button = me.down('#domain');
        Ext.suspendLayouts();

        store.load(function () {
            var menu = button.menu;
            menu.removeAll();

            store.each(function (item) {
                menu.add({
                    text: item.get('displayValue'),
                    value: item.get('id')
                })
            });
            //button.setValue(me.scope.filter.get('domain'));
        });

        Ext.resumeLayouts(true);
    }
});

