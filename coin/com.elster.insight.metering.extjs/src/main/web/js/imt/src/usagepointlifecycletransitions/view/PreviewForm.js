Ext.define('Imt.usagepointlifecycletransitions.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagepoint-life-cycle-transitions-preview-form',
    requires: [
        'Uni.form.field.DisplayFieldWithInfoIcon'
    ],
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                name: 'name',
                itemId: 'name-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.from', 'IMT', 'From'),
                name: 'fromState_name',
                itemId: 'from-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.to', 'IMT', 'To'),
                name: 'toState_name',
                itemId: 'to-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.privileges', 'IMT', 'Privileges'),
                name: 'privileges',
                itemId: 'privileges-field',
                renderer: function (privileges) {
                    if (privileges) {
                        var str = '';
                        Ext.Array.each(privileges, function (privilege) {
                            if (privilege.privilege === privileges[privileges.length - 1].privilege) {
                                str += privilege.name;
                            } else {
                                str += privilege.name + ' - ';
                            }
                        });
                        return Ext.String.htmlEncode(str);
                    } else {
                        return '-';
                    }
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'pretransitionsContainer',
                fieldLabel: Uni.I18n.translate('usagePointLifeCycleTransitions.add.pretransitionChecks', 'IMT', 'Pretransition checks'),
                defaults: {
                    labelAlign: 'top'
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'autoActionsContainer',
                fieldLabel: Uni.I18n.translate('usagePointLifeCycleTransitions.add.autoActions', 'IMT', 'Auto actions'),
                defaults: {
                    labelAlign: 'top'
                }
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;

        me.callParent(arguments);

        Ext.suspendLayouts();
        me.addDynamicFields(record.get('microChecks'),
            me.down('#pretransitionsContainer'),
            Uni.I18n.translate('usagePointLifeCycleTransitions.pretransitionChecks.empty', 'IMT', 'No pretransition checks'),
            'microChecks');
        me.addDynamicFields(record.get('microActions'),
            me.down('#autoActionsContainer'),
            Uni.I18n.translate('usagePointLifeCycleTransitions.autoActions.empty', 'IMT', 'No auto actions'),
            'microActions');
        Ext.resumeLayouts(true);
    },

    addDynamicFields: function (properties, container, emptyMsg, typeChecksOrActions) {
        container.removeAll();
        if (properties && properties.length) {
            Ext.Object.each(_.groupBy(properties, function (property) {
                    return property.category.id
                }),
                function (categoryId, propertiesGroup) {
                    var fieldContainer;
                    fieldContainer = {
                        xtype: 'fieldcontainer',
                        fieldLabel: propertiesGroup[0].category.name,
                        defaults: {
                            labelAlign: 'top'
                        },
                        items: []
                    };
                    Ext.Array.each(propertiesGroup, function (property) {
                        fieldContainer.items.push({
                            xtype: 'displayfield-with-info-icon',
                            fieldLabel: undefined,
                            value: property.name,
                            infoTooltip: property.description,
                            style: 'margin-top: -10px'
                        });
                    });
                    container.add(fieldContainer);
                });
        } else {
            container.add({
                xtype: 'displayfield',
                value: emptyMsg
            });
        }
    }
});
