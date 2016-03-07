Ext.define('Scs.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.servicecalls-preview-form',
    router: null,
    layout: {
        type: 'vbox'
    },
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.topLevelServiceCall', 'SCS', 'Top level service call'),
                name: 'topLevelParent',
                id: 'topLevelParentField',
                hidden: true,
                router: me.router,
                renderer: function(value) {
                    if(value.name) {
                        return '<a href="' + this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id}) + '">' + value.name + '</a>';
                    }
                    return "-"
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.parentServiceCall', 'SCS', 'Parent service call'),
                name: 'parent',
                id: 'parentField',
                hidden: true,
                router: me.router,
                renderer: function(value) {
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
                renderer: function(value) {
                    return value === "" ? "-" : value;
                }
            },
            {
                xtype: 'container',
                id: 'serviceCallChildContainer',
                layout: {
                    type: 'vbox'
                }
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
        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(record);
        childrenContainer.removeAll();
        if(record.get('numberOfChildren')) {
            me.addChildrenInfo(record, childrenContainer);
        }

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
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
    }

});
