-- This is a help and reminder document to inform the developer how and what security related translation
-- keys should be inserted in the nlsdatabase.

-- The database is located on labo.eict.local:1521/eiserver with username nls
-- The password is for security reasons NOT stored here

-- Each new EncryptionDeviceAccessLevel#getTranslationKey that is added should have a corresponding translationkey,
-- something like 'DlmsSecuritySupport.encryptionlevel.3' (keys are case sensitive)

-- Each new AuthenticationDeviceAccessLevel#getTranslationKey that is added should have a corresponding translationkey,
-- something like 'DlmsSecuritySupport.authenticationlevel.3' (keys are case sensitive)

-- Each new value for SecurityPropertySpecName should have a corresponding translationkey,
-- something like 'Password' (keys are case sensitive)

-- Use the following template to insert your keys:
-- insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('your key', 'english translation', 'L', 'Y', 'N', 'MDW', sysdate);

