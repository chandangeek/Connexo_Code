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

        this.callParent(arguments);
        me.bindStore('ext-empty-store', true);
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

    onBindStore: function (store) {
        var me = this;
        me.setDisabled(!store.count());
        Ext.suspendLayouts();
        me.menu.removeAll();
        store.group('groupId');

        if (store.count()) {
            store.each(function (item) {
                if (!item.get('groupId')) {
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

    //onUpdateRemovablesStore: function () {
    //    var me = this,
    //        emptyText = Uni.I18n.translate('search.overview.addCriteria.emptyText', 'UNI', 'Add criteria'),
    //        addCriteriaCombo = me.getAddCriteriaCombo(),
    //        criteriaStore = Ext.getStore('Uni.store.search.Removables');
    //
    //    if (addCriteriaCombo) {
    //        addCriteriaCombo = me.getAddCriteriaCombo().down('#addcriteria');
    //        if (criteriaStore.count() === 0) {
    //            emptyText = Uni.I18n.translate('search.overview.addCriteria.emptyText.none', 'UNI', 'No criteria to add');
    //        }
    //        if (addCriteriaCombo.text !== emptyText) {
    //            addCriteriaCombo.text = emptyText;
    //        }
    //    }
    //},
});

