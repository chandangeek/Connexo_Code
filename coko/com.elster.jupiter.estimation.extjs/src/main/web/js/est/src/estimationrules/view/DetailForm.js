Ext.define('Est.estimationrules.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.property.form.Property',
        'Est.estimationrules.view.ActionMenu'
    ],
    alias: 'widget.estimation-rules-detail-form',
    ui: 'large',
    title: Uni.I18n.translate('general.overview', 'EST', 'Overview'),
    layout: 'form',
    actionMenuItemId: null,
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    items: [
        {
            itemId: 'name-field',
            fieldLabel: Uni.I18n.translate('general.estimationRule', 'EST', 'Estimation rule'),
            name: 'name'
        },
        {
            itemId: 'estimator-field',
            fieldLabel: Uni.I18n.translate('estimationrules.estimator', 'EST', 'Estimator'),
            name: 'displayName'
        },
        {
            itemId: 'status-field',
            fieldLabel: Uni.I18n.translate('general.status', 'EST', 'Status'),
            name: 'active',
            renderer: function (value) {
                return value ? Uni.I18n.translate('general.active', 'EST', 'Active') : Uni.I18n.translate('general.inactive', 'EST', 'Inactive');
            }
        },
        {
            xtype: 'fieldcontainer',
            itemId: 'reading-types-field',
            fieldLabel: Uni.I18n.translate('general.readingTypes', 'EST', 'Reading types'),
            defaults: {
                xtype: 'reading-type-displayfield',
                fieldLabel: undefined
            }
        },
        {
            xtype: 'property-form',
            itemId: 'estimation-rule-properties',
            isEdit: false,
            defaults: {
                labelWidth: 250
            }
        }
    ],
    updateForm: function (record) {
        var me = this,
            readingTypesField = me.down('#reading-types-field');

        Ext.suspendLayouts();
        if (!me.staticTitle) {
            me.setTitle(Ext.String.htmlEncode(record.get('name')));
        }
        me.loadRecord(record);
        readingTypesField.removeAll();
        Ext.Array.each(record.get('readingTypes'), function (item) {
            readingTypesField.add({
                value: item
            });
        });
        me.down('property-form').loadRecord(record);
        Ext.resumeLayouts(true);
    },
    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'button',
                itemId: 'estimation-rules-detail-action-menu-button',
                text: Uni.I18n.translate('general.actions', 'EST', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                privileges: Est.privileges.EstimationConfiguration.administrate,
                menu: {
                    xtype: 'estimation-rules-action-menu',
                    itemId: me.actionMenuItemId
                }
            }
        ];

        me.callParent(arguments);
    }
});