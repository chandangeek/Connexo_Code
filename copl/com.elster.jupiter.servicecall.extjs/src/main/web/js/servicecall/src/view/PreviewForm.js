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
            }
        ];
        me.callParent(arguments);
    },

    updatePreview: function (record) {
        var me = this;
        if (!Ext.isDefined(record)) {
            return;
        }
        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(record);
        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    }

});
