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
    menuItems: [],

    setChecked: function(property, value, suppressEvents) {
        var item,
            me = this,
            base = property.get('groupId')
                ? me.menu.items.findBy(function(i){return i.value === property.get('groupId');})
                : me;

        if (base) {
            item = base.menu.items.findBy(function(i){return i.criteria === property;});
            if (item) {
                item.setChecked(value, suppressEvents);
            }
        }
    },

    initComponent: function () {
        var me = this,
            service = this.getService(),
            listeners = [];

        me.menu = {
            plain: true,
            defaults: {
                xtype: 'menucheckitem'
            }
        };

        listeners.push(service.on({
            add:  me.onCriteriaAdd,
            remove: me.onCriteriaRemove,
            change: me.onCriteriaChange,
            scope: me,
            destroyable: true
        }));

        me.callParent(arguments);
        me.bindStore(me.store, true);

        listeners.push(me.store.on('beforeload', function() {
            me.setLoading(true);
        }, me, {
            destroyable: true
        }));

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

    /**
     * performs constraint update
     *
     * @param widget
     * @param value
     */
    onCriteriaChange: function (widget, value) {
        var me = this,
            deps = _.filter(me.menuItems, function (item) {
                return !!(item.criteria.get('constraints')
                && item.criteria.get('constraints').length
                && item.criteria.get('constraints').indexOf(widget.property.get('name')) >= 0);
            });

        deps.map(function (item) {
            item.setDisabled(!value);
        });
    },

    createMenuItem: function (criteria) {
        var menuitem = {
            xtype: 'menucheckitem',
            text: criteria.get('displayValue'),
            value: criteria.get('name'),
            criteria: criteria,
            checked: this.service.filters.get(criteria.get('name'))
        };

        if (    criteria.get('constraints')
            &&  criteria.get('constraints').length
            &&  this.service.checkConstraints(criteria)
        ) {
            Ext.apply(menuitem, {
                disabled: true,
                tooltip: {
                    title: Uni.I18n.translate('search.criteriaselector.disabled.title', 'UNI', 'Enable {0}', [criteria.get('displayValue')]),
                    text: Uni.I18n.translate('search.criteriaselector.disabled.title', 'UNI',
                        '{0} become available as soon as a search value has been specified for {1}',
                        [criteria.get('displayValue'), criteria.get('constraints').join(', ')]),
                    maxWidth: 150
                }
            })
        }

        return menuitem;
    },

    onStoreLoad: function (store) {
        var me = this,
            groups;
        me.setDisabled(!store.count());
        Ext.suspendLayouts();
        me.menu.removeAll();
        me.menuItems = [];
        store.group('groupId');

        if (store.count()) {
            store.each(function (item) {
                if (!item.get('groupId') && item.get('sticky') == false) {
                    me.menuItems.push(me.menu.add(me.createMenuItem(item)));
                }
            });

            groups = store.getGroups();
            _.sortBy(groups, function (item) {
                var group = item.children[0].get('group');

                return group.displayValue || group;
            });
            groups.map(function(group) {
                var items = [];

                group.children.map(function(item) {
                    items.push(me.createMenuItem(item));
                });

                if (items.length && !Ext.isEmpty(group.name)) {
                    me.menuItems = me.menuItems.concat(me.menu.add({
                        xtype: 'menuitem',
                        text: items[0].criteria.get('group').displayValue,
                        value: group.name,
                        menu: {
                            itemId: 'search-criteria-sub-menu',
                            items: items
                        }
                    }).menu.items.getRange());
                }
            });
        }
        Ext.resumeLayouts(true);

        me.setLoading(false);
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

