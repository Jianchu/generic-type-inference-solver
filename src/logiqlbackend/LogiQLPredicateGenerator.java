package logiqlbackend;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import constraintsolver.Lattice;

/**
 * LogiqlConstraintGenerator take QualifierHierarchy of current type system as
 * input, and generate the logiql encoding of all constraint, and write the
 * result in a .logic file.
 *
 * @author Jianchu Li
 *
 */
public class LogiQLPredicateGenerator {

    // Map<String, String> subtype = new HashMap<String, String>();
    // Map<String, String> supertype = new HashMap<String, String>();
    // Map<String, String> notComparable = new HashMap<String, String>();
    Map<AnnotationMirror, String> qualifierName = new HashMap<AnnotationMirror, String>();
    // Set<? extends AnnotationMirror> allTypes;
    // String top = "";
    // String bottom = "";
    private final String path;
    private final String ISANNOTATED = "isAnnotated";
    private final String MAYBEANNOTATED = "mayBeAnnotated";
    private final String CANNOTBEANNOTATED = "cannotBeAnnotated";

    // QualifierHierarchy qualHierarchy;

    public LogiQLPredicateGenerator(String path) {
        this.path = path;
    }


    public void GenerateLogiqlEncoding() {
        Set<String> allTypesInString = new HashSet<String>();
        StringBuilder encodingForEqualityConModifier = new StringBuilder();
        StringBuilder encodingForInequalityConModifier = new StringBuilder();
        StringBuilder encodingForEqualityConstraint = new StringBuilder();
        StringBuilder encodingForInequalityConstraint = new StringBuilder();
        StringBuilder encodingForComparableConstraint = new StringBuilder();
        StringBuilder encodingForSubtypeConTopBottom = new StringBuilder();
        StringBuilder encodingForSubtypeConstraint = new StringBuilder();
        StringBuilder basicEncoding = new StringBuilder();
        mapSimpleOriginalName();
        for (AnnotationMirror i : Lattice.allTypes) {
            allTypesInString.add(qualifierName.get(i));
        }
        getBasicString(allTypesInString, basicEncoding);
        getEncodingForEqualityConModifier(allTypesInString, encodingForEqualityConModifier);
        getEncodingForInequalityConModifier(allTypesInString, encodingForInequalityConModifier);
        getEncodingForEqualityConstraint(allTypesInString, encodingForEqualityConstraint);
        getEncodingForInequalityConstraint(allTypesInString, encodingForInequalityConstraint);
        getEncodingForComparableConstraint(allTypesInString, encodingForComparableConstraint);
        getEncodingForSubtypeConTopBottom(allTypesInString, encodingForSubtypeConTopBottom);
        getEncodingForSubtypeConstraint(allTypesInString, encodingForSubtypeConstraint);

        writeFile(basicEncoding.append(encodingForEqualityConModifier)
                .append(encodingForInequalityConModifier)
                .append(encodingForEqualityConstraint)
                .append(encodingForInequalityConstraint)
                .append(encodingForComparableConstraint)
                .append(encodingForSubtypeConTopBottom).append(encodingForSubtypeConstraint).toString());

        // System.out.println(basicEncoding);
        // System.out.println(encodingForEqualityConModifier);
        // System.out.println(encodingForInequalityConModifier);
        // System.out.println(encodingForEqualityConstraint);
        // System.out.println(encodingForInequalityConstraint);
        // System.out.println(encodingForComparableConstraint);
        // System.out.println(encodingForSubtypeConTopBottom);
        // System.out.println(encodingForSubtypeConstraint);
        // System.out.println(encodingForAdaptationConstraint);
    }

    private void mapSimpleOriginalName() {
        for (AnnotationMirror modifier : Lattice.allTypes) {
            qualifierName.put(modifier, modifier.toString().replaceAll("[.@]", "_"));
        }
    }

    /**
     * generate the encoding of EqualityConstraint for the case that both
     * modifiers of two slots are unknown.
     *
     * @param allTypesInString
     *            is a set contains all modifiers of current type system in
     *            string.
     * @param encodingForEqualityConstraint
     *            is the return value.
     * @returns encodingForEqualityConstraint, which is the logiql encoding of
     *          equality constraint for current type system.
     */
    private void getEncodingForEqualityConstraint(Set<String> allTypesInString, StringBuilder encodingForEqualityConstraint) {
        for (String s : allTypesInString) {
            encodingForEqualityConstraint.append(ISANNOTATED + s + "(v1) <- equalityConstraint(v1,v2), " + ISANNOTATED + s + "(v2).\n");
            encodingForEqualityConstraint.append(ISANNOTATED + s + "(v2)<- equalityConstraint(v1,v2), " + ISANNOTATED + s + "(v1).\n");
        }
    }
    
    /**
     * generate the encoding of equality constraint for the case that if one
     * slot's modifier is known.
     *
     * @param allTypesInString
     *            is a set contains all modifiers of current type system in
     *            string.
     * @param encodingForEqualityConModifier
     *            is the return value.
     * @returns encodingForEqualityConModifier, which is the logiql encoding of
     *          equality constraint for current type system.
     */
    private void getEncodingForEqualityConModifier(
            Set<String> allTypesInString,
            StringBuilder encodingForEqualityConModifier) {
        for (String s : allTypesInString) {
            encodingForEqualityConModifier.append(ISANNOTATED + s + "(v2) <- equalityConstraintContainsModifier(v1,v2), v1 = \""  + s + "\".\n");
        }
    }

    /**
     * generate the encoding of subtype constraint.
     *
     * @param allTypesInString
     *            is a set contains all modifiers of current type system in
     *            string.
     * @param encodingForSubtypeConstraint
     *            is the return value.
     * @returns encodingForSubtypeConstraint, which is the logiql encoding of
     *          subtype constraint for current type system.
     */
    private void getEncodingForSubtypeConstraint(Set<String> allTypesInString, StringBuilder encodingForSubtypeConstraint) {
        String[] subtypeFors;
        String[] supertypeFors;
        for (String subkey : allTypesInString) {
            subtypeFors = subtype.get(subkey).split(" ");
            supertypeFors = supertype.get(subkey).split(" ");
            for (int i = 1; i < subtypeFors.length; i++) {
                if (!subtypeFors[i].equals(subkey)
                        && !subtypeFors[i].equals(" ")) {
                    encodingForSubtypeConstraint.append(CANNOTBEANNOTATED + subkey
                            + "(v1) <- subtypeConstraint(v1,v2), " + ISANNOTATED + subtypeFors[i]
                            + "(v2).\n");
                }
                if (!subtypeFors[i].equals(" ")
                        && !(subkey.equals(top) && subtypeFors[i].equals(top))) {
                    encodingForSubtypeConstraint.append(MAYBEANNOTATED + subkey
                            + "(v2) <- subtypeConstraint(v1,v2), " + ISANNOTATED + subtypeFors[i]
                            + "(v1), " + "!" + CANNOTBEANNOTATED + subkey + "(v2).\n");
                }
            }

            for (int j = 1; j < supertypeFors.length; j++) {
                if (!supertypeFors[j].equals(subkey)
                        && !supertypeFors[j].equals(" ")) {
                    encodingForSubtypeConstraint.append(CANNOTBEANNOTATED
 + subkey
                            + "(v2) <- subtypeConstraint(v1,v2), " + ISANNOTATED + supertypeFors[j]
                            + "(v1).\n");
                }
                if (!supertypeFors[j].equals(" ")
                        && !(subkey.equals(bottom) && supertypeFors[j]
                                .equals(bottom))) {
                    encodingForSubtypeConstraint.append(MAYBEANNOTATED + subkey
                            + "(v1) <- subtypeConstraint(v1,v2), "
 + ISANNOTATED + supertypeFors[j]
                            + "(v2), " + "!" + CANNOTBEANNOTATED + subkey + "(v1).\n");
                }

            }
        }
    }

    /**
     * Generate the encoding for a special case of subtype constraint: if the
     * modifier A is subtype of modifier in Bottom or the modifier is the
     * supertype of modifier in top.
     *
     * @param allTypesInString
     *            is a set contains all modifiers of current type system in
     *            string.
     * @param encodingForSubtypeConTopBottom
     *            is the return value.
     * @returns encodingForSubtypeConTopBottom, which is the logiql encoding of
     *          subtype constraint for current type system.
     */
    private void getEncodingForSubtypeConTopBottom(
            Set<String> allTypesInString,
            StringBuilder encodingForSubtypeConTopBottom) {
        String[] subtypeFors;
        for (String subkey : subtype.keySet()) {
            subtypeFors = subtype.get(subkey).split(" ");
            if (subtypeFors.length == allTypesInString.size() + 1) {
                encodingForSubtypeConTopBottom.append(ISANNOTATED + subkey
                        + "(v2) <- subtypeConstraint(v1,v2), " + ISANNOTATED
                        + subkey + "(v1).\n");
            }
        }
        for (String superkey : supertype.keySet()) {
            subtypeFors = supertype.get(superkey).split(" ");
            if (subtypeFors.length == allTypesInString.size() + 1) {
                encodingForSubtypeConTopBottom.append(ISANNOTATED + superkey
                        + "(v1) <- subtypeConstraint(v1,v2), " + ISANNOTATED
                        + superkey + "(v2).\n");
            }
        }
    }

    /**
     * Generate the encoding of comparable constraint.
     *
     * @param allTypesInString
     *            is a set contains all modifiers of current type system in
     *            string.
     * @param encodingForComparableConstraint
     *            is the return value.
     * @returns encodingForComparableConstraint, which is the logiql encoding of
     *          comparable constraint for current type system.
     */
    private void getEncodingForComparableConstraint(
            Set<String> allTypesInString,
            StringBuilder encodingForComparableConstraint) {
        StringBuilder variableMaybeAnnotated = new StringBuilder();
        if (notComparable.isEmpty() != true) {
            String[] notComparableForkey;
            for (String key : notComparable.keySet()) {
                notComparableForkey = notComparable.get(key).split(" ");
                for (String s : notComparableForkey) {
                    if (!s.equals("")) {
                        encodingForComparableConstraint
.append(CANNOTBEANNOTATED
                                        + key
                                        + "(v1)<- comparableConstraint(v1,v2), "
 + ISANNOTATED + s + "(v2).\n");
                        encodingForComparableConstraint
.append(CANNOTBEANNOTATED
                                        + key
                                        + "(v2)<- comparableConstraint(v1,v2), "
 + ISANNOTATED + s + "(v1).\n");
                        for (String ss : allTypesInString) {
                            if (!ss.equals(s)) {
                                variableMaybeAnnotated
.append(MAYBEANNOTATED
                                                + ss
                                                + "(v1) <- comparableConstraint(v1,v2), "
 + ISANNOTATED + key
                                        + "(v2), !" + CANNOTBEANNOTATED + ss
                                                + "(v1).\n");
                                variableMaybeAnnotated
.append(MAYBEANNOTATED
                                                + ss
                                                + "(v2) <- comparableConstraint(v1,v2), "
 + ISANNOTATED + key
                                        + "(v1), !" + CANNOTBEANNOTATED + ss
                                                + "(v2).\n");
                            }
                        }
                    }
                }
            }
        }
        encodingForComparableConstraint.append(variableMaybeAnnotated);
    }

    /**
     * generate the encoding of inequality constraint for the case that both
     * modifiers of two slots are unknown.
     *
     * @param allTypesInString
     *            is a set contains all modifiers of current type system in
     *            string.
     * @param encodingForInequalityConstraint
     *            is the return value.
     * @returns encodingForInequalityConstraint, which is the logiql encoding of
     *          inequality constraint for current type system.
     */
    private void getEncodingForInequalityConstraint(
            Set<String> allTypesInString,
            StringBuilder encodingForInequalityConstraint) {
        StringBuilder variableMaybeAnnotated = new StringBuilder();
        for (String s : allTypesInString) {
            encodingForInequalityConstraint.append(CANNOTBEANNOTATED + s
                    + "(v1) <- inequalityConstraint(v1,v2), " + ISANNOTATED + s
                    + "(v2).\n");
            encodingForInequalityConstraint.append(CANNOTBEANNOTATED + s
                    + "(v2) <- inequalityConstraint(v1,v2), " + ISANNOTATED + s
                    + "(v1).\n");
            for (String ss : allTypesInString) {
                if (s != ss) {
                    variableMaybeAnnotated.append(MAYBEANNOTATED + ss
                            + "(v1) <- inequalityConstraint(v1,v2), "
 + ISANNOTATED + s + "(v2), !"
                            + CANNOTBEANNOTATED
                            + ss + "(v1).\n");
                    variableMaybeAnnotated.append(MAYBEANNOTATED + ss
                            + "(v2) <- inequalityConstraint(v1,v2), "
 + ISANNOTATED + s + "(v1), !"
                            + CANNOTBEANNOTATED
                            + ss + "(v2).\n");
                }
            }
        }
        encodingForInequalityConstraint.append(variableMaybeAnnotated);
    }

    /**
     * generate the encoding of inequality constraint for the case that if one
     * slot's modifier is known.
     *
     * @param allTypesInString
     *            is a set contains all modifiers of current type system in
     *            string.
     * @param encodingForInequalityConModifier
     *            is the return value.
     * @returns encodingForInequalityConModifier, which is the logiql encoding
     *          of inequality constraint for current type system.
     */
    private void getEncodingForInequalityConModifier(
            Set<String> allTypesInString,
            StringBuilder encodingForInequalityConModifier) {
        for (String s : allTypesInString) {
            encodingForInequalityConModifier
.append(CANNOTBEANNOTATED
                            + s
                            + "(v1) <- inequalityConstraintContainsModifier(v1,v2), v2 = \""
                            + s + "\".\n");
        }
    }



    /**
     * generate the basic predicate of encoding.
     *
     * @param allTypesInString
     *            is a set contains all modifiers of current type system in
     *            string.
     * @param basicEncoding
     *            is the return value.
     * @returns basicEncoding.
     */
    private void getBasicString(Set<String> allTypesInString,
            StringBuilder basicEncoding) {
        basicEncoding
                .append("variable(v), hasvariableName(v:i) -> int(i)."
                        + "\nmodifier(m), hasmodifierName(m:i) -> string(i)."
                        + "\nvariableOrder(v) -> int(v)."
                        + "\nvariableOrder(v) <- variable(v)."
                        + "\norderVariable[o] = v -> int(o), int(v)."
                        + "\norderVariable[o] =v <- seq<<o=v>> variableOrder(v)."
                        + "\norderedAnnotationOf[v] = a -> int(v), string(a)."
                        + "\norderedAnnotationOf[v] = a <- AnnotationOf[v]=a, orderVariable[_]=v."
                        + "\nAnnotationOf[v] = a -> variable(v), string(a)."
                        + "\nadaptationConstraint(v1,v2,v3) -> variable(v1), variable(v2), variable(v3)."
                        + "\nequalityConstraint(v1,v2) -> variable(v1), variable(v2)."
                        + "\nequalityConstraintContainsModifier(v1,v2) -> modifier(v1), variable(v2)."
                        + "\ninequalityConstraint(v1,v2)-> variable(v1), variable(v2)."
                        + "\ninequalityConstraintContainsModifier(v1,v2) -> modifier(v2), variable(v1)."
                        + "\ncomparableConstraint(v1,v2) -> variable(v1), variable(v2)."
                        + "\nsubtypeConstraint(v1,v2) -> variable(v1), variable(v2).\n");
        for (String s : allTypesInString) {
            basicEncoding.append(ISANNOTATED + s + "(v) ->variable(v).\n" + MAYBEANNOTATED + s
                    + "(v) ->variable(v).\n" + CANNOTBEANNOTATED + s + "(v) ->variable(v).\n"
                    + "AnnotationOf[v] = \"" + s + "\" <-isAnnotated" + s
                    + "(v).\n");
        }
    }

    /**
     * write all encoding generated by this class to file LogiqlEncoding.logic.
     *
     *
     */
    private void writeFile(String output) {
        try {
            String writePath = path + "/LogiqlEncoding.logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(writePath);
            pw.write(output);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
