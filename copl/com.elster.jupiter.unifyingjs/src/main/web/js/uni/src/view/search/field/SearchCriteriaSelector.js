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


    initComponent: function () {
        var me = this;

        me.menu = {
            plain: true,
            defaults: {
                xtype: 'menucheckitem'
            },
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

    onBindStore: function (store) {
        var me = this;
        me.setDisabled(!store.count());
        Ext.suspendLayouts();
        me.menu.removeAll();
        if (store.count()) {
            store.each(function (item) {
                me.menu.add({
                    text: item.get('displayValue'),
                    value: item.get('name'),
                    criteria: item
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
});

