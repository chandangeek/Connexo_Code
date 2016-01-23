Ext.define('Uni.view.search.field.internal.CriteriaButton', {
    extend: 'Ext.button.Button',
    xtype: 'uni-search-internal-button',
    requires: [
        'Ext.util.Filter'
    ],
    menuConfig: null,
    items: [],

    reset: function() {
        this.setText(this.emptyText);
    },

    updateButtonText: function (value) {
        Ext.isEmpty(value)
            ? this.reset()
            : this.setText(this.emptyText + '&nbsp;(' + value.length + ')');
    },

    reconfigure: function() {
        var me = this;

        me.property.refresh(function() {
            Ext.suspendLayouts();
            me.menu.removeAll();
            var widget = me.menu.add(me.service.createWidgetForProperty(me.property));
            var filter = me.service.filters.get(me.dataIndex);
            me.menu.setWidth(widget.minWidth);

            // restore value
            widget.setValue(filter && filter.value
                ? filter.value.map(function(rawValue) { return Ext.create('Uni.model.search.Value', rawValue)})
                : null);

            me.showMenu();
            Ext.resumeLayouts(true);
        });
    },

    initComponent: function () {
        var me = this;

        me.emptyText = me.text;
        Ext.apply(me, {
            menu: Ext.apply({
                plain: true,
                //width: 70,
                maxHeight: 600,
                bodyStyle: {
                    background: '#fff'
                },
                padding: 0,
                //minWidth: 273,
                minWidth: 70,
                items: me.items,
                onMouseOver: Ext.emptyFn,
                enableKeyNav: false
            }, me.menuConfig)
        });

        me.callParent(arguments);

        me.on('click', function(){
            me.reconfigure();
        });
        me.on('menuhide', function(){
            me.menu.removeAll();
        });

        me.setDisabled(me.property.get('disabled'));

        me.service.on('change', function(filters, filter) {
            if (me.dataIndex == filter.id) {
                if (me.property.get('exhaustive')) {
                    me.updateButtonText(filter.value && filter.value[0] ? filter.value[0].criteria : null);
                } else {
                    me.updateButtonText(filter.value);
                }

            }
        });

        me.service.on('criteriaChange', function(criterias, criteria) {
            if (me.dataIndex == criteria.getId() && me.rendered) {
                me.setDisabled(criteria.get('disabled'));
            }
        });


        // todo: drop listeners on destroy
    }
});