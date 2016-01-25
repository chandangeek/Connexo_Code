Ext.define('Uni.view.search.field.internal.CriteriaButton', {
    extend: 'Ext.button.Button',
    xtype: 'uni-search-internal-button',
    requires: [
        'Ext.util.Filter'
    ],
    menuConfig: null,
    enableToggle: true,
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

        Ext.apply(me, {
            emptyText: me.text,
            disabled: me.property.get('disabled'),
            menu: Ext.apply({
                plain: true,
                maxHeight: 600,
                bodyStyle: {
                    background: '#fff'
                },
                padding: 0,
                minWidth: 70,
                items: [],
                onMouseOver: Ext.emptyFn,
                enableKeyNav: false
            }, me.menuConfig),
            listeners: {
                toggle:     me.onToggle,
                menuhide:   me.onMenuHideEvent
            }
        });

        me.callParent(arguments);

        var serviceListeners = me.service.on({
            change:         me.onFilterChange,
            criteriaChange: me.onCriteriaChange,
            scope: me,
            destroyable: true
        });

        me.on('destroy', function () {
            serviceListeners.destroy();
        });
    },

    onToggle: function() {
        if (this.pressed) {
            this.reconfigure();
        }
    },

    onMenuHideEvent: function() {
        this.menu.removeAll();
        this.toggle(false);
    },

    onFilterChange: function(filters, filter) {
        var me = this;

        if (me.dataIndex == filter.id) {
            if (me.property.get('selectionMode') == 'multiple') {
                me.updateButtonText(filter.value ? _.reduce(filter.value, function (acc, v) {
                    return acc.concat(v.criteria)
                }, []) : null);
            } else {
                me.updateButtonText(filter.value);
            }

        }
    },

    onCriteriaChange: function(criterias, criteria) {
        if (this.dataIndex == criteria.getId() && this.rendered) {
            this.setDisabled(criteria.get('disabled'));
        }
    }
});