Ext.define('Imt.usagepointlifecycle.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagepoint-life-cycles-preview-form',
    xtype: 'usagepoint-life-cycles-preview-form',
    isOverview: false,
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'cycle-name',
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                name: 'name'
            },
            {
                itemId: 'cycle-default',
                fieldLabel: Uni.I18n.translate('general.default', 'IMT', 'Default'),
                name: 'isDefault',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No')
                }
            },
            {
                itemId: 'number-of-transitions',
                hidden: !me.isOverview,
                fieldLabel: Uni.I18n.translate('general.transitions', 'IMT', 'Transitions'),
                name: 'transitionsCount'
            },
            {
                xtype: 'fieldcontainer',
                name: 'states',
                itemId: 'states-field-container',
                fieldLabel: Uni.I18n.translate('general.states', 'IMT', 'States'),
                items: [
                    {
                        xtype: 'container',
                        itemId: 'states-container',
                        items: []
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;

        if (me.isOverview && record.get('obsolete')) {
            me.insert(0, {
                xtype: 'container',
                layout: 'column',
                items: {
                    xtype: 'uni-form-empty-message',
                    itemId: 'empty-message',
                    text: Uni.I18n.translate('usagePointLifeCycles.noLongerBeUsed', 'IMT', 'The usage point life cycle was archived and can no longer be used')
                }
            });
        }

        me.callParent(arguments);
    }
});
