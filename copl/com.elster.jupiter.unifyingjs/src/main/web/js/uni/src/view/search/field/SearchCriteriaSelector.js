Ext.define('Uni.view.search.field.SearchCriteriaSelector', {
    extend: 'Ext.button.Button',
    xtype: 'search-criteria-selector',
    style: {
        'background-color': '#71adc7'
    },
    mixins: [
        'Ext.util.Bindable'
    ],

    text: Uni.I18n.translate('search.overview.addCriteria.emptyText', 'UNI', 'Add criteria'),
    arrowAlign: 'right',
    menuAlign: 'tr-br',
    store: 'Uni.store.search.Properties',
    config: {
        service: null
    },

    setChecked: function(property, value, suppressEvents) {
        var item, me = this;
        var base = property.get('groupId')
            ? me.menu.items.findBy(function(i){return i.value === property.get('groupId');}).menu
            : me.menu;

        item = base.items.findBy(function(i){return i.criteria === property;});

        if (item) {
            item.setChecked(value, suppressEvents);
        }
    },

    initComponent: function () {
        var me = this;

        me.menu = {
            plain: true,
            defaults: {
                xtype: 'menucheckitem'
            }
        };

        var service = this.getService();
        var listeners = [];

        //service.on('reset', this.onReset, this);
        listeners.push(service.on('add', this.onCriteriaAdd, this, {
            destroyable: true
        }));
        listeners.push(service.on('remove', this.onCriteriaRemove, this, {
            destroyable: true
        }));

        this.callParent(arguments);
        me.bindStore(me.store, true);

        listeners.push(me.store.on('load', me.onStoreLoad, me, {
            destroyable: true
        }));

        me.on('destroy', function () {
            listeners.map(function (i) {
                i.destroy()
            });
        });
    },

    //onReset: function(filters, filter, property) {
    //    if (!property.get('sticky')) {
    //        this.setChecked(property, true);
    //    }
    //},

    onCriteriaAdd: function(filters, filter, property) {
        if (!property.get('sticky')) {
            this.setChecked(property, true);
        }
    },

    onCriteriaRemove: function(filters, filter, property) {
        if (!property.get('sticky')) {
            this.setChecked(property, false);
        }
    },

    createMenuItem: function (criteria) {
        var menuitem = {
            xtype: 'menucheckitem',
            text: criteria.get('displayValue'),
            value: criteria.get('name'),
            criteria: criteria
        };

        if (criteria.get('constraints') && criteria.get('constraints').length) {
            Ext.apply(menuitem, {
                disabled: true
                //todo: fix tooltip
                //tooltip: {
                //    title: 'Enable connection properties',
                //    text: 'Connection properties become available as soon as a search value has been specified for Device type, Device configuration and Connection properties.',
                //    maxWidth: 150
                //}
            })
        }
        return menuitem
    },

    onStoreLoad: function (store) {
        var me = this;
        me.setDisabled(!store.count());
        Ext.suspendLayouts();
        me.menu.removeAll();
        store.group('groupId');

        if (store.count()) {
            store.each(function (item) {
                if (!item.get('groupId') && item.get('sticky') == false) {
                    me.menu.add(me.createMenuItem(item));
                }
            });

            store.getGroups().map(function(group) {
                var items = [];
                group.children.map(function(item) {
                    items.push(me.createMenuItem(item));
                });

                if (items.length && !Ext.isEmpty(group.name)) {
                    me.menu.add({
                        xtype: 'menuitem',
                        text: items[0].criteria.get('group').displayValue,
                        value: group.name,
                        menu: {
                            items: items
                        }
                    })
                }
            });
        }
        Ext.resumeLayouts(true);
    },

    setValue: function(value) {
        var item = this.menu.items.findBy(function(item){return item.value == value});
        if (item) {
            item.setActive();
            this.setText(item.text);
            this.fireEvent('change', this);
        }
    }
});

