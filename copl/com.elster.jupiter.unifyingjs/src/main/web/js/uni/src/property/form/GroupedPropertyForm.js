Ext.define('Uni.property.form.GroupedPropertyForm', {
    extend: 'Uni.property.form.Property',
    alias: 'widget.grouped-property-form',
    addEditPage: false,

    initProperties: function (properties) {
        var me = this;
        var registry = Uni.property.controller.Registry,
            groups = {};
        me.removeAll();
        properties.each(function (property) {
            if (!(property instanceof Uni.property.model.Property)) {
                throw '!(entry instanceof Uni.property.model.Property)';
            }

            me.inheritedValues
                ? property.initInheritedValues()
                : property.initValues();

            properties.commitChanges();

            var type = property.getType();
            var fieldType = registry.getProperty(type);

            if (fieldType) {
                var partitions = property.get('key').split('.');
                partitions.pop();
                var control = Ext.create(fieldType, {
                        property: property,
                        isEdit: me.isEdit,
                        translationKey: 'DES',
                        labelWidth: 250,
                        width: 235,
                        allowBlank: !property.get('required'),
                        name: property.get('key'),
                        itemId: property.get('key'),
                        boxLabel: Uni.I18n.translate(property.get('key'), null, property.get('key'))

                    }),
                    groupName = partitions.join('.');
                if (type === 'BOOLEAN') {
                    control.fieldLabel = '';
                }

                if (groups[groupName]) {
                    groups[groupName].push(control);
                } else {
                    groups[groupName] = [control];
                }
            }
        });
        Ext.suspendLayouts();
        Ext.iterate(groups, function (groupName, groupItems) {
            var namesArray = groupName.split('.'),
                fieldContainer = {
                    xtype: 'fieldcontainer',
                    itemId: 'group-fieldcontainer'
                };
            if (namesArray.length > 1) {
                fieldContainer.fieldLabel = Uni.I18n.translate(namesArray[1], null, namesArray[1]);
                fieldContainer.items = groupItems;
                Ext.Array.each(groupItems, function (groupItem) {
                    groupItem.setWidth(600);
                    groupItem.labelAlign = 'left';
                })
            } else {
                if (!me.addEditPage) {
                    me.add({
                        xtype: 'displayfield',
                        renderer: function () {
                            return '<b>' + Uni.I18n.translate(namesArray[0], null, namesArray[0]) + '</b>'
                        }
                    });
                } else {
                    me.add({
                        title: Uni.I18n.translate(namesArray[0], null, namesArray[0]),
                        ui: 'medium'
                    });
                }
            }
            me.add(fieldContainer.items ? fieldContainer : groupItems);
        });
        Ext.resumeLayouts();
        this.initialised = true;
    },

    updateRecord: function() {
        var me = this,
            raw = me.getFieldValues(),
            values = {},
            key,
            field;
        _.each(raw.properties || [], function(firstValue, firstKey){
            if (typeof firstValue == 'object') {
                _.each(firstValue, function(secondValue, secondKey){
                    if (typeof secondValue == 'object') {
                        _.each(secondValue, function(thirdValue, thirdKey){
                            key = firstKey + '.' + secondKey + '.' + thirdKey;
                            field = me.getPropertyField(key);
                            values[key] = field.getValue(thirdValue);
                        });
                    } else {
                        key = firstKey + '.' + secondKey;
                        field = me.getPropertyField(key);
                        values[key] = field.getValue(secondValue);
                    }
                });
            } else {
                field = me.getPropertyField(firstKey);
                values[firstKey] = field.getValue(firstValue);
            }
        });
        this.getForm().hydrator.hydrate(values, me.getRecord());
    },

    getPropertyField: function(key) {
        var me = this;
        if (key.split('.').length === 3) {
            return me.down('#group-fieldcontainer').getComponent(key);
        } else {
            return me.getComponent(key);
        }
    }
});
