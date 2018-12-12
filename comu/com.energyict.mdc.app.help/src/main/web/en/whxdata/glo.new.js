/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

(function () {
    var glossary = {
        "type": "glossary",
        "chunkinfos": [{"type": "chunkinfo", "first": "authentication", "last": "register configuration", "num": "41", "node": "gdata1"}, {
            "type": "chunkinfo",
            "first": "register types",
            "last": "validator",
            "num": "10",
            "node": "gdata2"
        }]
    };
    window.rh.model.publish(rh.consts('KEY_TEMP_DATA'), glossary, {sync: true});
})();