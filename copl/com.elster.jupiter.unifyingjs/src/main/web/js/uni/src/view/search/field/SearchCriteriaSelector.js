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
        var item = this.menu.items.findBy(function(i){return i.criteria === property;});
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
            //items: [
            //    {
            //        text: 'Batch',
            //        checked: false,
            //        //checkHandler: onItemCheck
            //    },
            //    {
            //        text: 'Has open data collection issues',
            //        checked: false
            //    },
            //    {
            //        text: 'Service category',
            //        checked: false
            //    },
            //    {
            //        text: 'Shared schedule name',
            //        checked: false
            //    },
            //    {
            //        text: 'Usage point',
            //        checked: false
            //    },
            //    {
            //        text: 'Year of certification',
            //        checked: false
            //    },
            //    {
            //        arrowAlign: 'left',
            //        text: 'Load profile',
            //        menu: {
            //            items: [
            //                {
            //                    text: 'Last reading',
            //                    checked: false,
            //                    //checkHandler: onItemCheck
            //                }, {
            //                    text: 'Interval',
            //                    checked: false,
            //                    //checkHandler: onItemCheck
            //                }, {
            //                    text: 'OBIS code',
            //                    checked: false,
            //                    //checkHandler: onItemCheck
            //                }, {
            //                    text: 'Name',
            //                    checked: false,
            //                    //checkHandler: onItemCheck
            //                }
            //            ]
            //        }
            //    },
            //    {
            //        text: 'Communication task',
            //        disabled: true,
            //        tooltip: {
            //            title: 'Enable connection properties',
            //            text: 'Connection properties become available as soon as a search value has been specified for Device type, Device configuration and Connection properties.',
            //            maxWidth: 150
            //        },
            //
            //        menu: {
            //            items: [
            //                {
            //                    text: 'Last reading',
            //                    checked: false,
            //                    //checkHandler: onItemCheck
            //                }, {
            //                    text: 'Interval',
            //                    checked: false,
            //                    disabled: true
            //                    //checkHandler: onItemCheck
            //                }, {
            //                    text: 'OBIS code',
            //                    checked: false,
            //                    //checkHandler: onItemCheck
            //                }, {
            //                    text: 'Name',
            //                    checked: false,
            //                    //checkHandler: onItemCheck
            //                }
            //            ]
            //        }
            //    }]
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
                disabled: true,
                tooltip: {
                    title: 'Enable connection properties',
                    text: 'Connection properties become available as soon as a search value has been specified for Device type, Device configuration and Connection properties.',
                    maxWidth: 150
                }
            })
        }
        return menuitem
    },

    onBindStore: function (store) {
        var me = this;
        me.setDisabled(!store.count());
        Ext.suspendLayouts();
        me.menu.removeAll();
        store.group('group');

        if (store.count()) {
            store.each(function (item) {
                if (!item.get('group')) {
                    me.menu.add(me.createMenuItem(item));
                }
            });

            store.getGroups().map(function(group) {
                var items = [];
                group.children.map(function(item) {
                    items.push(me.createMenuItem(item));
                });

                me.menu.add({
                    xtype: 'menuitem',
                    text: group.name,
                    value: group.name,
                    menu: {
                        items: items
                    }
                })
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

