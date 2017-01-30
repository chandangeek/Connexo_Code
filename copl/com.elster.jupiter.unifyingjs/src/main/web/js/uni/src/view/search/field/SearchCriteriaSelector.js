Ext.define('Uni.view.search.field.SearchCriteriaSelector', {
    extend: 'Ext.button.Button',
    xtype: 'search-criteria-selector',
    mixins: [
        'Ext.util.Bindable'
    ],

    text: Uni.I18n.translate('search.overview.addCriteria.emptyText', 'UNI', 'Add criteria'),
    arrowAlign: 'right',
    menuAlign: 'tr-br?',
    store: 'Uni.store.search.Properties',
    config: {
        service: null,
        searchContainer: null
    },
    menuItems: [],

    setChecked: function(property, value, suppressEvents) {
        var item,
            me = this,
            base = property.get('groupId')
                ? me.menu.items.findBy(function(i){return i.value === property.get('groupId');})
                : me;

        if (base) {
            item = base.menu.items.findBy(function(i){return i.criteria.getId() === property.getId();});
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
            floating: true,
            constrain: true,
            constraintInsets: '10 10 10 10',
            plain: true,
            defaults: {
                xtype: 'menucheckitem'
            }
        };

        listeners.push(service.on({
            add:  me.onCriteriaAdd,
            remove: me.onCriteriaRemove,
            change: me.onCriteriaChange,
            reset: me.onReset,
            scope: me,
            destroyable: true
        }));

        me.callParent(arguments);
        me.bindStore(me.store, true);

        listeners.push(me.store.on('beforeload', function() {
            me.setDisabled(true);
            me.menu.setLoading(true);
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

    onReset: function() {
        this.onStoreLoad(this.store);
    },

    onCriteriaAdd: function(filters, property) {
        if (!property.get('sticky')) {
            this.setChecked(property, true, true);
        }
    },

    onCriteriaRemove: function(filters, property) {
        if (!property.get('sticky')) {
            this.setChecked(property, false, true);
        }
    },

    /**
     * performs constraint update
     *
     * @param filters
     * @param filter
     */
    onCriteriaChange: function (filters, filter) {
        var me = this,
            deps = _.filter(me.menuItems, function (item) {
                return !!(item.criteria.get('constraints')
                && item.criteria.get('constraints').length
                && item.criteria.get('constraints').indexOf(filter.id) >= 0);
            });

        deps.map(me.checkConstraints());
    },

    checkConstraints: function() {
        var me = this;

        return function(item) {item.setDisabled(me.service.checkConstraints(item.criteria))}
    },

    createMenuItem: function (criteria) {
        var me = this,
            menuitem = {
                xtype: 'menucheckitem',
                text: criteria.get('displayValue'),
                value: criteria.get('name'),
                criteria: criteria,
                checked: this.service.filters.get(criteria.get('name'))
            };

        if (    criteria.get('constraints')
            &&  criteria.get('constraints').length
            &&  me.service.checkConstraints(criteria)
        ) {
            var  constraints = criteria.get('constraints').map(function(c) {
                return me.getStore().getById(c).get('displayValue')
            });

            Ext.apply(menuitem, {
                disabled: true,
                tooltip: {
                    title: Uni.I18n.translate('search.criteriaselector.disabled.title', 'UNI', 'Enable {0}', [criteria.get('displayValue')]),
                    text: Uni.I18n.translate('search.criteriaselector.disabled.body', 'UNI',
                        '{0} property becomes available as soon as a value has been specified for the search criterion {1}',
                        [criteria.get('displayValue'), constraints.join(', ')]),
                    maxWidth: 150
                }
            })
        }

        return menuitem;
    },

    onStoreLoad: function (store) {
        var me = this,
            groups;

        Ext.suspendLayouts();
        me.setDisabled(!store.count());
        me.menu.removeAll();
        me.menuItems = [];
        store.group('groupId');

        if (store.count()) {
            store.each(function (item) {
                if (!item.get('groupId') && item.get('sticky') == false) {
                    me.menuItems.push(me.menu.add(me.createMenuItem(item)));
                }
            });

            groups = _.sortBy(store.getGroups(), function (item) {
                var group = item.children[0].get('group');

                return (group.displayValue || group).toLowerCase();
            });
            groups.map(function(group) {
                var items = [];

                _.sortBy(group.children, function (item) {
                    return item.get('displayValue').toLowerCase();
                }).map(function(item) {
                    items.push(me.createMenuItem(item));
                });

                if (items.length && !Ext.isEmpty(group.name)) {
                    me.menuItems = me.menuItems.concat(me.menu.add({
                        xtype: 'menuitem',
                        text: items[0].criteria.get('group').displayValue,
                        value: group.name,
                        menu: {
                            floating: true,
                            constrain: true,
                            maxHeight: me.getSearchContainer().getHeight(),
                            enableScrolling: true,
                            itemId: 'search-criteria-sub-menu',
                            items: items
                        }
                    }).menu.items.getRange());
                }
            });
        }

        me.menu.setLoading(false);
        Ext.resumeLayouts(true);
    },

    setValue: function(value, suspendEvent) {
        if (!Ext.isDefined(suspendEvent)) {
            suspendEvent = false;
        }

        var item = this.menu.items.findBy(function(item){return item.value == value});
        if (item) {
            item.setActive();
            this.setText(item.text);

            if (!suspendEvent) {
                this.fireEvent('change', this);
            }
        }
    }
});

