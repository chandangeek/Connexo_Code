Ext.define('Uni.view.search.field.TimeOfDay', {
    extend: 'Uni.view.search.field.DateTime',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine',
        'Uni.view.search.field.internal.TimeOfDayRange',
        'Uni.view.search.field.internal.TimeOfDayField'
    ],
    alias: 'widget.uni-search-criteria-timeofday',
    text: Uni.I18n.translate('search.field.timeOfDay.text', 'UNI', 'Time of day'),

    createCriteriaLine: function(config) {
        var me = this;

        return Ext.apply({
            xtype: 'uni-search-internal-criterialine',
            width: '455',
            operator: '==',
            removable: false,
            onRemove: function() {
                me.menu.remove(this);
                me.onInputChange();
            },
            operatorMap: {
                '==': 'uni-search-internal-timeOfDayField',
                //'!=': 'uni-search-internal-timeOfDayField',
                //'>': 'uni-search-internal-timeOfDayField',
                //'>=': 'uni-search-internal-timeOfDayField',
                //'<': 'uni-search-internal-timeOfDayField',
                //'<=': 'uni-search-internal-timeOfDayField',
                'BETWEEN': 'uni-search-internal-timeOfDayRange'
            },
            listeners: {
                change: {
                    fn: me.onInputChange,
                    scope: me
                }
            }
        }, config)
    }
});