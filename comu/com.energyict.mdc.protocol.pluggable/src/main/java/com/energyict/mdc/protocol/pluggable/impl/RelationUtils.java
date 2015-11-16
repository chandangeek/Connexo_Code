package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;

/**
 * Provides functionality related to {@link Relation}s
 * and their {@link RelationAttributeType attributes}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/10/12
 * Time: 10:54
 */
public final class RelationUtils {

    private static final int RELATION_TYPE_NAME_MAX_LENGTH = 21;    // Actual is 24 but all relation type names will be prefixed with "Sys"
    private static final int RELATION_ATTRIBUTE_TYPE_NAME_MAX_LENGTH = 30;

    /**
     * RelationType names are limited to a maximum set of characters.
     * The given name will be converted to the appropriate maxLength based on the hashCode of the name.
     * The result will be:
     * <pre>
     * <code> name.substring(0, maxLength - lengthOfHashCode).concat(String.valueOf(name.hashCode()))</code>
     * </pre>
     *
     * @param name The name which need to be converted
     * @return the converted name if the length exceeds the given maxLength, or the original if the length is <= maxLength
     */
    public static String createConformRelationTypeName (String name) {
        return createConformRelationTypeName(name, RELATION_TYPE_NAME_MAX_LENGTH);
    }

    private static String createConformRelationTypeName (String name, int maxLength) {
        String hash = String.valueOf(Math.abs(name.hashCode()));
        if (hash.length() > maxLength) { // try to avoid this!
            return hash.substring(0, maxLength);
        }
        String hashedName = createHashedNameBasedOnLength("Sys" + name, maxLength, hash);
        System.out.println("Calculated hash relation type name for " + name + " as " + hashedName);
        return hashedName;
    }

    /**
     * The attributes of RelationTypes are limited to a maximum set of characters.
     * The given name will be converted to the appropriate maxLength based on the hashCode of the name.
     * The result will be:
     * <pre>
     * <code> name.substring(0, maxLength - lengthOfHashCode).concat(String.valueOf(name.hashCode()))</code>
     * </pre>
     *
     * @param name The name which need to be converted to a proper length
     * @return the converted name if the length exceeds the given maxLength, or the original if the length is <= maxLength
     */
    public static String createConformRelationAttributeName (String name) {
        return createConformRelationAttributeName(name, RELATION_ATTRIBUTE_TYPE_NAME_MAX_LENGTH);
    }

    private static String createConformRelationAttributeName (String name, int maxLength) {
        if (name.length() > maxLength) {
            String hash = String.valueOf(Math.abs(name.hashCode()));
            if (hash.length() > maxLength) { // try to avoid this!
                return hash.substring(0, maxLength);
            }
            return createHashedNameBasedOnLength(name, maxLength, hash);
        } else {
            return name;
        }
    }

    /**
     * RelationType names must be unique, in some cases (migration) the name is chosen by an certain pattern,
     * which may lead to duplicate relationTypeNames.
     * The given name will converted to and returned according to the following logic:
     * <pre>
     * The name will be appended by its HashCode.
     * If the name is longer then the allowed number or characters, it will be truncated,
     * but the HashCode will still be the complete trailing part.
     * </pre>
     *
     * @param javaClassname the javaClassName which need to be converted to a proper relationTypeName
     * @return the converted name
     */
    public static String createOriginalAndConformRelationNameBasedOnJavaClassname(String javaClassname) {
        return createOriginalAndConformRelationNameBasedOnJavaClassname(javaClassname, RELATION_TYPE_NAME_MAX_LENGTH);
    }

    public static String createOriginalAndConformRelationNameBasedOnJavaClassname(String javaClassname, int maxLength) {
        String hash = String.valueOf(Math.abs(javaClassname.hashCode()));
        String simpleClassname = getSimpleClassName(javaClassname);
        if (hash.length() > maxLength) { // try to avoid this!
            return hash.substring(0, maxLength);
        } else if (simpleClassname.length() + hash.length() <= maxLength) {
            return simpleClassname + hash;
        } else {
            return createHashedNameBasedOnLength(simpleClassname, maxLength, hash);
        }
    }

    private static String getSimpleClassName(String javaClassname) {
        if (javaClassname.contains(".")) {
            return javaClassname.substring(javaClassname.lastIndexOf('.') + 1);
        } else {
            return javaClassname;
        }
    }

    private static String createHashedNameBasedOnLength(String name, int maxLength, String hash) {
        return name.substring(0, Math.min(maxLength - hash.length(), name.length())) + hash;
    }

    // Hide utility class constructor
    private RelationUtils() {
    }

}
