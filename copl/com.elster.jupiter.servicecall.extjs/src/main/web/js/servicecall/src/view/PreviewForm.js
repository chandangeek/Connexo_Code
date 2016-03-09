Ext.define('Scs.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.servicecalls-preview-form',
    router: null,
    requires: [
        'Uni.property.form.Property'
    ],
    layout: {
        type: 'column'
    },
    defaults: {
        //labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'container',
                columnWidth: 0.5,
                itemId: 'serviceCallPreviewColumnOne',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.topLevelServiceCall', 'SCS', 'Top level service call'),
                        name: 'topLevelParent',
                        itemId: 'topLevelParentField',
                        hidden: true,
                        router: me.router,
                        renderer: function (value) {
                            if (value.name) {
                                return '<a href="' + this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id}) + '">' + value.name + '</a>';
                            }
                            return "-"
                        }
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.parentServiceCall', 'SCS', 'Parent service call'),
                        name: 'parent',
                        itemId: 'parentField',
                        hidden: true,
                        router: me.router,
                        renderer: function (value) {
                            if (value.name) {
                                return '<a href="' + this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id}) + '">' + value.name + '</a>';
                            }
                            return "-"
                        }
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.origin', 'SCS', 'Origin'),
                        name: 'origin',
                        renderer: function (value) {
                            return value === "" ? "-" : value;
                        }
                    },
                    {
                        xtype: 'container',
                        itemId: 'serviceCallChildContainer'
                    },
                    {
                        xtype: 'container',
                        itemId: 'serviceCallPreviewCASColumnOne'
                    }
                ],
            },
            {
                xtype: 'container',
                columnWidth: 0.5,
                itemId: 'serviceCallPreviewColumnTwo',
            }

        ];
        me.callParent(arguments);
    },

    updatePreview: function (record) {
        var me = this,
            childrenContainer = me.down('#serviceCallChildContainer');
        if (!Ext.isDefined(record)) {
            return;
        }
        Ext.suspendLayouts();

        me.loadRecord(record);
        childrenContainer.removeAll();
        if(record.get('numberOfChildren')) {
            me.addChildrenInfo(record, childrenContainer);
        }
        me.loadCustomPropertySets(record);
        Ext.resumeLayouts(true);
        me.updateLayout();
        me.doLayout();
    },

    addChildrenInfo: function(record, childrenContainer) {
        var me = this,
            container;
        if(record.get('childrenInfo')) {
            container = Ext.create('Ext.form.FieldContainer', {
                layout: {
                    type: 'vbox'
                },
                labelAlign: 'top',
                fieldLabel: Uni.I18n.translate('general.summaryOfChildren', 'SCS', 'Summary of children')
            });

            container.add({
                xtype: 'displayfield',
                value: record.get('numberOfChildren'),
                router: me.router,
                record: record,
                labelWidth: 250,
                fieldLabel: Uni.I18n.translate('general.children', 'SCS', 'Children'),
                renderer: function(value) {
                    return '<a href="' + this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: this.record.get('id')}) + '">' + value + '</a>'
                }
            });
            Ext.each(record.get('childrenInfo'), function (info) {
                var statusFilter = {
                    status: info.state,
                };
                container.add({
                    xtype: 'displayfield',
                    value: info.percentage + '%',
                    router: me.router,
                    record: record,
                    labelWidth: 250,
                    fieldLabel: info.stateDisplayName,
                    renderer: function(value) {
                        return '<a href="' + this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: this.record.get('id')}, statusFilter) + '">' + value + '</a>'
                    }
                });
            });

            childrenContainer.add(container)
        }
    },

    loadCustomPropertySets: function(record) {
        var me = this,
            container,
            casContainer,
            i = 0,
            propertyForm;

        me.down('#serviceCallPreviewColumnTwo').removeAll();
        me.down('#serviceCallPreviewCASColumnOne').removeAll();
        for(i; i < record.customPropertySetInfos().getCount(); i++) {
            container = i%2 === 0 ? me.down('#serviceCallPreviewColumnTwo') : me.down('#serviceCallPreviewCASColumnOne');
            casContainer = Ext.create('Ext.form.FieldContainer', {
                labelAlign: 'top',
                fieldLabel: record.customPropertySetInfos().getAt(i).get('name')
            });
            propertyForm = Ext.create('Uni.property.form.Property', {
                isEdit: false,
                defaults: {
                    xtype: 'container',
                    resetButtonHidden: true,
                    labelWidth: 250
                }
            });
            propertyForm.loadRecord(record.customPropertySetInfos().getAt(i));

            casContainer.add(propertyForm)
            container.add(casContainer);
        };
    }

});
