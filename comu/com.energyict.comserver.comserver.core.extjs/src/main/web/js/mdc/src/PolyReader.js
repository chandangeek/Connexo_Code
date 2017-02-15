/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.PolyReader', {
    extend: 'Ext.data.reader.Json',
    alias : 'reader.polymorphic',

    extractData : function(root) {
        var me = this,
            records = [],
            Model   = me.model,
            length  = root.length,
            convertedValues, node, record, i, modelCls;

        if (!root.length && Ext.isObject(root)) {
            root = [root];
            length = 1;
        }

        for (i = 0; i < length; i++) {
            node = root[i];
            if (!node.isModel) {

                if ('__type__' in node || me.getTypeDiscriminator) {
                    var typeDiscriminator = (me.getTypeDiscriminator
                        ? me.getTypeDiscriminator(node)
                        : node['__type__']);
                    if (typeDiscriminator) {
                        if (!Ext.isString(typeDiscriminator)) {
                            Ext.Error.raise("Type discriminator must be a string.")
                        }
                        modelCls = Ext.ClassManager.get(typeDiscriminator);
                    }
                }

                if (!modelCls || !(modelCls.prototype instanceof me.model
                    || modelCls == me.model))
                {
                    continue;
                }

                var tmp = me.model;
                me.model = modelCls;
                me.buildExtractors(true);
                record = modelCls.create(undefined, me.getId(node), node, convertedValues = {});
                me.model = tmp;

                record.phantom = false;

                me.convertRecordData(convertedValues, node, record);

                records.push(record);

                if (me.implicitIncludes) {
                    me.readAssociated(record, node);
                }
            } else {
                records.push(node);
            }
        }

        return records;
    }

});