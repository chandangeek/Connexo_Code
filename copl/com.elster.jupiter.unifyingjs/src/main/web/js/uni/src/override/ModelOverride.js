/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.ModelOverride
 */
Ext.define('Uni.override.ModelOverride', {
    override: 'Ext.data.Model',

    getWriteData: function(includeAssociated,excludeNotPersisted){
        var me = this,
            fields = me.fields.items,
            fLen = fields.length,
            data = {},
            name, f, persistent;

        persistent = (typeof excludeNotPersisted === 'undefined')?false:excludeNotPersisted;

        for (f = 0; f < fLen; f++) {
            if (!persistent)
            {
                name = fields[f].name;
                data[name] = me.get(name);
            }
            else
            {
                if(fields[f].persist)
                {
                    name = fields[f].name;
                    data[name] = me.get(name);
                }
            }
        }

        if (includeAssociated === true) {
            Ext.apply(data, me.getAssociatedData(persistent));
        }
        return data;
    },

    getAssociatedData: function(persistedFields){
        return this.prepareAssociatedData({}, 1,persistedFields);
    },

    /**
     * @private
     * This complex-looking method takes a given Model instance and returns an object containing all data from
     * all of that Model's *loaded* associations. See {@link #getAssociatedData}
     * @param {Object} seenKeys A hash of all the associations we've already seen
     * @param {Number} depth The current depth
     * @return {Object} The nested data set for the Model's loaded associations
     */
    prepareAssociatedData: function(seenKeys, depth,persistedFields) {
        /**
         * In this method we use a breadth first strategy instead of depth
         * first. The reason for doing so is that it prevents messy & difficult
         * issues when figuring out which associations we've already processed
         * & at what depths.
         */
        var me = this,
            associations = me.associations.items,
            associationCount = associations.length,
            associationData = {},
// We keep 3 lists at the same index instead of using an array of objects.
// The reasoning behind this is that this method gets called a lot
// So we want to minimize the amount of objects we create for GC.
            toRead = [],
            toReadKey = [],
            toReadIndex = [],
            associatedStore, associatedRecords, associatedRecord, o, index, result, seenDepth,
            associationId, associatedRecordCount, association, i, j, type, name;

        for (i = 0; i < associationCount; i++) {
            association = associations[i];
            associationId = association.associationId;

            seenDepth = seenKeys[associationId];
            if (seenDepth && seenDepth !== depth) {
                continue;
            }
            seenKeys[associationId] = depth;

            type = association.type;
            name = association.name;
            if (type == 'hasMany') {
//this is the hasMany store filled with the associated data
                associatedStore = me[association.storeName];

//we will use this to contain each associated record's data
                associationData[name] = [];

//if it's loaded, put it into the association data
                if (associatedStore && associatedStore.getCount() > 0) {
                    associatedRecords = associatedStore.data.items;
                    associatedRecordCount = associatedRecords.length;

//now we're finally iterating over the records in the association. Get
// all the records so we can process them
                    for (j = 0; j < associatedRecordCount; j++) {
                        associatedRecord = associatedRecords[j];
                        associationData[name][j] = associatedRecord.getWriteData(false,persistedFields);
                        toRead.push(associatedRecord);
                        toReadKey.push(name);
                        toReadIndex.push(j);
                    }
                }
            } else if (type == 'belongsTo' || type == 'hasOne') {
                associatedRecord = me[association.instanceName];
// If we have a record, put it onto our list
                if (associatedRecord !== undefined) {
                    associationData[name] = associatedRecord.getWriteData(false,persistedFields);
                    toRead.push(associatedRecord);
                    toReadKey.push(name);
                    toReadIndex.push(-1);
                }
            }
        }

        for (i = 0, associatedRecordCount = toRead.length; i < associatedRecordCount; ++i) {
            associatedRecord = toRead[i];
            o = associationData[toReadKey[i]];
            index = toReadIndex[i];
            result = associatedRecord.prepareAssociatedData(seenKeys, depth + 1, persistedFields);
            if (index === -1) {
                Ext.apply(o, result);
            } else {
                Ext.apply(o[index], result);
            }
        }

        return associationData;
    }
});