Ext.define('Uni.view.search.field.internal.CriteriaButton', {
    extend: 'Ext.button.Button',
    xtype: 'uni-search-internal-button',
    requires: [
        'Ext.util.Filter',
        'Uni.model.search.Value'
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

        Ext.suspendLayouts();
        me.menu.removeAll();
        var widget = me.menu.add(me.service.createWidgetForProperty(me.property));
        var filter = me.service.filters.get(me.dataIndex);
        me.menu.setWidth(widget.minWidth);

        // restore value
        widget.setValue(filter && filter.value
            ? filter.value.map(function(rawValue) { return Ext.create('Uni.model.search.Value', rawValue)})
            : null);

        Ext.resumeLayouts(true);
    },

    initComponent: function () {
        var me = this,
            criteria = me.property,
            constraints = criteria.get('constraints');

        Ext.apply(me, {
            emptyText: me.text,
            disabled: criteria.get('disabled'),

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
                toggle: me.onToggle,
                click: me.onClickEvent
            }
        });

        if (constraints
            && constraints.length
            && criteria.get('disabled')
        ) {
            constraints = criteria.get('constraints').map(function(c) {
                return me.service.criteria.getByKey(c).get('displayValue')
            });

            Ext.apply(me, {
                tooltip: {
                    title: Uni.I18n.translate('search.criteriaselector.disabled.title', 'UNI', 'Enable {0}', [criteria.get('displayValue')]),
                    text: Uni.I18n.translate('search.criteriaselector.disabled.body', 'UNI',
                        '{0} property becomes available as soon as a value has been specified for the search criterion {1}',
                        [criteria.get('displayValue'), constraints.join(', ')]),
                    maxWidth: 150
                }
            });
        }

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
        var me = this;
        if (me.pressed) {
            if (!me.property.isCached) {
                me.property.refresh(function(){
                    me.reconfigure();
                    me.showMenu();
                });
            } else {
                if (!me.menu.items.getRange().length) {
                    me.reconfigure();
                    me.showMenu();
                }
            }
        }
    },

    onClickEvent: function () {
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
            this.reconfigure();
        }
    },

    setDisabled: function (disabled) {
        this.setTooltip(disabled ? this.tooltip : null);
        this.callParent(arguments);
    }
});