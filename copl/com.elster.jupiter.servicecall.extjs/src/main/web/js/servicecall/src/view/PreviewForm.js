/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.servicecalls-preview-form',
    router: null,
    detailed: false,
    serviceCallId: null,
    requires: [
        'Uni.property.form.Property'
    ],
    layout: {
        type: 'column'
    },
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
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
                            return '-'
                        }
                    },
                    {
                        xtype: 'displayfield',
                        name: 'targetObject',
                        hidden: !me.detailed,
                        router: me.router,
                        fieldLabel: Uni.I18n.translate('servicecalls.affectedObject', 'SCS', 'Affected object'),
                        renderer: function (value) {
                            if(value.type === 'com.energyict.mdc.device.Device') {
                                if(Uni.util.Application.getAppName() === 'MultiSense') {
                                    return '<a href="' + this.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(value.key)}) + '">' + Ext.String.htmlEncode(value.key) + '</a>';
                                } else {
                                    return Ext.String.htmlEncode(value.key);
                                }
                            } else if (value.type === 'com.elster.jupiter.metering.UsagePoint') {
                                if(Uni.util.Application.getAppName() === 'MdmApp') {
                                    return '<a href="' + this.router.getRoute('usagepoints/view').buildUrl({usagePointId:  encodeURIComponent(value.key)}) + '">' + Ext.String.htmlEncode(value.key) + '</a>';
                                } else {
                                    return Ext.String.htmlEncode(value.key);
                                }
                            } else {
                                this.hide();
                                return '-';
                            }
                        }
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.serviceCall', 'SCS', 'Service call'),
                        name: 'name',
                        hidden: !me.detailed
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.externalReference', 'SCS', 'External reference'),
                        name: 'externalReference',
                        hidden: !me.detailed,
                        renderer: function (value) {
                            if (value) {
                                return Ext.String.htmlEncode(value);
                            }
                            return "-"
                        }
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.type', 'SCS', 'Type'),
                        name: 'type',
                        hidden: !me.detailed
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.origin', 'SCS', 'Origin'),
                        name: 'origin',
                        renderer: function (value) {
                            return value === "" ? "-" : Ext.String.htmlEncode(value);
                        }
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.status', 'SCS', 'Status'),
                        name: 'state',
                        hidden: !me.detailed,
                        renderer: function(value) {
                            if(value.displayValue) {
                                return value.displayValue;
                            } else {
                                return '-';
                            }
                        }
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.receivedDate', 'SCS', 'Received date'),
                        name: 'creationTimeDisplayLong',
                        hidden: !me.detailed
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.modificationDate', 'SCS', 'Modification date'),
                        name: 'lastModificationTimeDisplayLong',
                        hidden: !me.detailed
                    },
                    {
                        xtype: 'container',
                        itemId: 'serviceCallChildContainer'
                    },
                    {
                        xtype: 'container',
                        itemId: 'serviceCallPreviewCASColumnOne'
                    }
                ]
            },
            {
                xtype: 'form',
                columnWidth: 0.5,
                itemId: 'serviceCallPreviewColumnTwo'
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
        record.get('topLevelParent') === "" ? me.down("#topLevelParentField").hide() : me.down("#topLevelParentField").show();
        record.get('parent') === "" ? me.down("#parentField").hide() : me.down("#parentField").show();

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
        if(record.get('children')) {
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
                    return '<a href="' + this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: this.record.get('id')}) + '?' + '">' + value + '</a>'
                }
            });
            Ext.each(record.get('children'), function (info) {
                var statusFilter = {
                    status: info.state
                };
                container.add({
                    xtype: 'displayfield',
                    value: info.percentage + '% (' + info.count + ')',
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
        for(i; i < record.customPropertySets().getCount(); i++) {
            container = i%2 === 0 ? me.down('#serviceCallPreviewColumnTwo') : me.down('#serviceCallPreviewCASColumnOne');
            casContainer = Ext.create('Ext.form.FieldContainer', {
                labelAlign: 'top',
                fieldLabel: record.customPropertySets().getAt(i).get('name'),
                style: {
                    overflow: 'visible'
                },
                labelWidth: 250
            });
            propertyForm = Ext.create('Uni.property.form.Property', {
                isEdit: false,
                defaults: {
                    xtype: 'container',
                    resetButtonHidden: true,
                    labelWidth: 250
                }
            });
            propertyForm.loadRecord(record.customPropertySets().getAt(i));

            casContainer.add(propertyForm)
            container.add(casContainer);
        };
    }

});
