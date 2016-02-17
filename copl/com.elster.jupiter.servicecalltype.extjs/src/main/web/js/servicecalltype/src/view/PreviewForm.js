Ext.define('Sct.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.servicecalltypes-preview-form',
    defaults: {
        labelWidth: 250
    },
    layout: {
        type: 'column'
    },
    initComponent: function () {
        var me = this;
        me.items = [];
        me.callParent(arguments);
    },

    updatePreview: function (record, parent) {
        var me = this,
            cps,
            wrapContainer1,
            wrapContainer2,
            infoMessage,
            i;

        if (!Ext.isDefined(record)) {
            return;
        }
        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.removeAll();

        cps = record.get('customPropertySets');

        if(cps) {
            if(cps.length > 0) {
                wrapContainer1 = Ext.create('Ext.container.Container', {
                    xtype: 'container',
                    columnWidth: 0.5,
                    layout: {
                        type: 'vbox',
                    }
                });

                wrapContainer2 = Ext.create('Ext.container.Container', {
                    xtype: 'container',
                    columnWidth: 0.5,
                    layout: {
                        type: 'vbox',
                    }
                });

                me.add(wrapContainer1);
                me.add(wrapContainer2);

                for(i = 0; i < cps.length; i++) {
                    me.addInfoToPreview(cps[i], i % 2 === 0 ? wrapContainer1 : wrapContainer2);
                }
            } else {
                infoMessage = Ext.create('Uni.util.FormEmptyMessage');
                me.add(infoMessage);
                infoMessage.setText(Uni.I18n.translate('servicecalltype.noCASmessage', 'SCT', 'This service call type has no attributes that need to be defined.'));
            }
        }

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    addInfoToPreview: function (customPropertySetInfo, wrapContainer) {
        var container,
            attributesContainer;

        container = Ext.create('Ext.form.FieldContainer', {
            labelAlign: 'top',
            fieldLabel: customPropertySetInfo.name
        });

        attributesContainer = Ext.create('Ext.form.FieldContainer', {
            fieldLabel: Uni.I18n.translate('general.attributes', 'SCT', 'Attributes'),
            layout: {
                type: 'vbox'
            },
            labelWidth: 250
        });

        customPropertySetInfo.attributes.forEach(function(attribute) {
            attributesContainer.add(
                Ext.create('Ext.form.field.Display', {
                value: attribute.name,
                labelWidth: 250
            }))
        });
        container.add(attributesContainer);
        wrapContainer.add(container);
    }
});
