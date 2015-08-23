package checkers.inference.solver.LogiqlDebugSolver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.framework.type.QualifierHierarchy;

/**
 * LogiqlConstraintGenerator take QualifierHierarchy of current type system as
 * input, and generate the logiql encoding of all constraint, and write the
 * result in a .logic file.
 *
 * @author Jianchu Li
 *
 */

class LogiqlConstraintGenerator {

    Map<String, String> subtype = new HashMap<String, String>();
    Map<String, String> supertype = new HashMap<String, String>();
    Map<String, String> notComparable = new HashMap<String, String>();
    Map<AnnotationMirror, String> qualifierName = new HashMap<AnnotationMirror, String>();
    Set<? extends AnnotationMirror> allTypes;
    String top = "";
    String bottom = "";
    String path = "";
    final String isAnnotated;
    final String mayBeAnnotated;
    final String cannotBeAnnotated;
    QualifierHierarchy qualHierarchy;

    protected LogiqlConstraintGenerator(QualifierHierarchy qualHierarchy,
            String path) {
        this.qualHierarchy = qualHierarchy;
        this.path = path;
        isAnnotated = "isAnnotated";
        mayBeAnnotated = "mayBeAnnotated";
        cannotBeAnnotated = "cannotBeAnnotated";
    }

    protected void GenerateLogiqlEncoding() throws IOException {
        allTypes = qualHierarchy.getTypeQualifiers();
        Set<String> allTypesInString = new HashSet<String>();
        StringBuilder encodingForEqualityConModifier = new StringBuilder();
        StringBuilder encodingForInequalityConModifier = new StringBuilder();
        StringBuilder encodingForEqualityConstraint = new StringBuilder();
        StringBuilder encodingForInequalityConstraint = new StringBuilder();
        StringBuilder encodingForComparableConstraint = new StringBuilder();
        StringBuilder encodingForSubtypeConTopBottom = new StringBuilder();
        StringBuilder encodingForSubtypeConstraint = new StringBuilder();
        StringBuilder encodingForAdaptationConstraint =  new StringBuilder();
        StringBuilder basicEncoding = new StringBuilder();
        mapSimpleOriginalName();
        for (AnnotationMirror i : allTypes) {
            allTypesInString.add(qualifierName.get(i));
        }
        getTopBottomQualifier(qualHierarchy);
        getSubSupertype(qualHierarchy);
        getNotComparable();
        getBasicString(allTypesInString, basicEncoding);
        getEncodingForEqualityConModifier(allTypesInString,
                encodingForEqualityConModifier);
        getEncodingForInequalityConModifier(allTypesInString,
                encodingForInequalityConModifier);
        getEncodingForEqualityConstraint(allTypesInString,
                encodingForEqualityConstraint);
        getEncodingForInequalityConstraint(allTypesInString,
                encodingForInequalityConstraint);
        getEncodingForComparableConstraint(allTypesInString,
                encodingForComparableConstraint);
        getEncodingForSubtypeConTopBottom(allTypesInString,
                encodingForSubtypeConTopBottom);
        getEncodingForSubtypeConstraint(allTypesInString,
                encodingForSubtypeConstraint);
        encodingForAdaptationConstraint= getEncodingForAdaptationConstraint(encodingForAdaptationConstraint);
        writeFile(basicEncoding.append(encodingForEqualityConModifier)
                .append(encodingForInequalityConModifier)
                .append(encodingForEqualityConstraint)
                .append(encodingForInequalityConstraint)
                .append(encodingForComparableConstraint)
                .append(encodingForSubtypeConTopBottom)
                .append(encodingForSubtypeConstraint)
                .append(encodingForAdaptationConstraint));

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
        for (AnnotationMirror modifier : qualHierarchy.getTypeQualifiers()) {
            qualifierName.put(modifier,
                    modifier.toString().replaceAll("[.@]", "_"));
        }
    }

    /**
     * get the top and bottom modifier of current type system.
     * 
     * @param hierarchy
     */
    private void getTopBottomQualifier(QualifierHierarchy hierarchy) {
        for (AnnotationMirror i : hierarchy.getTopAnnotations()) {
            top = qualifierName.get(i);
        }
        for (AnnotationMirror j : hierarchy.getBottomAnnotations()) {
            bottom = qualifierName.get(j);
        }
    }

    /**
     * put a key-value pair in HashMap subtype if value is subtype of key, or in
     * HashMap supertype if value is super type of key. For value, different
     * modifier's name is divided by space.
     */
    private void getSubSupertype(QualifierHierarchy hierarchy) {
        for (AnnotationMirror i : allTypes) {
            String subtypeFori = "";
            String supertypeFori = "";
            for (AnnotationMirror j : allTypes) {
                if (hierarchy.isSubtype(j, i)) {
                    subtypeFori = subtypeFori + " " + qualifierName.get(j);
                }
                if (hierarchy.isSubtype(i, j)) {
                    supertypeFori = supertypeFori + " " + qualifierName.get(j);
                }
            }
            supertype.put(qualifierName.get(i), supertypeFori);
            subtype.put(qualifierName.get(i), subtypeFori);
        }
    }

    /**
     * get the information of which two modifiers are not comparable. And put
     * this result to HashMap "notComparable"
     */
    private void getNotComparable() {
        for (AnnotationMirror i : allTypes) {
            String notComparableFori = "";
            for (AnnotationMirror j : allTypes) {
                if (!subtype.get(qualifierName.get(i)).contains(
                        qualifierName.get(j))
                        && !subtype.get(qualifierName.get(j)).contains(
                                qualifierName.get(i))) {
                    notComparableFori = notComparableFori + " "
                            + qualifierName.get(j);
                }
            }
            if (!notComparableFori.equals("")) {
                notComparable.put(qualifierName.get(i), notComparableFori);
            }
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
    private void getEncodingForEqualityConstraint(Set<String> allTypesInString,
            StringBuilder encodingForEqualityConstraint) {
        for (String s : allTypesInString) {
            encodingForEqualityConstraint.append(isAnnotated + s
                    + "(v1) <- equalityConstraint(v1,v2), " + isAnnotated + s
                    + "(v2).\n");
            encodingForEqualityConstraint.append(isAnnotated + s
                    + "(v2)<- equalityConstraint(v1,v2), " + isAnnotated + s
                    + "(v1).\n");
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
    private void getEncodingForSubtypeConstraint(Set<String> allTypesInString,
            StringBuilder encodingForSubtypeConstraint) {
        String[] subtypeFors;
        String[] supertypeFors;
        for (String subkey : allTypesInString) {
            subtypeFors = subtype.get(subkey).split(" ");
            supertypeFors = supertype.get(subkey).split(" ");
            for (int i = 1; i < subtypeFors.length; i++) {
                if (!subtypeFors[i].equals(subkey)
                        && !subtypeFors[i].equals(" ")) {
                    encodingForSubtypeConstraint.append(cannotBeAnnotated
                            + subkey + "(v1) <- subtypeConstraint(v1,v2), "
                            + isAnnotated + subtypeFors[i] + "(v2).\n");
                }
                if (!subtypeFors[i].equals(" ")
                        && !(subkey.equals(top) && subtypeFors[i].equals(top))) {
                    encodingForSubtypeConstraint.append(mayBeAnnotated + subkey
                            + "(v2) <- subtypeConstraint(v1,v2), "
                            + isAnnotated + subtypeFors[i] + "(v1), " + "!"
                            + cannotBeAnnotated + subkey + "(v2).\n");
                }
            }

            for (int j = 1; j < supertypeFors.length; j++) {
                if (!supertypeFors[j].equals(subkey)
                        && !supertypeFors[j].equals(" ")) {
                    encodingForSubtypeConstraint.append(cannotBeAnnotated
                            + subkey + "(v2) <- subtypeConstraint(v1,v2), "
                            + isAnnotated + supertypeFors[j] + "(v1).\n");
                }
                if (!supertypeFors[j].equals(" ")
                        && !(subkey.equals(bottom) && supertypeFors[j]
                                .equals(bottom))) {
                    encodingForSubtypeConstraint.append(mayBeAnnotated + subkey
                            + "(v1) <- subtypeConstraint(v1,v2), "
                            + isAnnotated + supertypeFors[j] + "(v2), " + "!"
                            + cannotBeAnnotated + subkey + "(v1).\n");
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
                encodingForSubtypeConTopBottom.append(isAnnotated + subkey
                        + "(v2) <- subtypeConstraint(v1,v2), " + isAnnotated
                        + subkey + "(v1).\n");
            }
        }
        for (String superkey : supertype.keySet()) {
            subtypeFors = supertype.get(superkey).split(" ");
            if (subtypeFors.length == allTypesInString.size() + 1) {
                encodingForSubtypeConTopBottom.append(isAnnotated + superkey
                        + "(v1) <- subtypeConstraint(v1,v2), " + isAnnotated
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
                                .append(cannotBeAnnotated
                                        + key
                                        + "(v1)<- comparableConstraint(v1,v2), "
                                        + isAnnotated + s + "(v2).\n");
                        encodingForComparableConstraint
                                .append(cannotBeAnnotated
                                        + key
                                        + "(v2)<- comparableConstraint(v1,v2), "
                                        + isAnnotated + s + "(v1).\n");
                        for (String ss : allTypesInString) {
                            if (!ss.equals(s)) {
                                variableMaybeAnnotated
                                        .append(mayBeAnnotated
                                                + ss
                                                + "(v1) <- comparableConstraint(v1,v2), "
                                                + isAnnotated + key + "(v2), !"
                                                + cannotBeAnnotated + ss
                                                + "(v1).\n");
                                variableMaybeAnnotated
                                        .append(mayBeAnnotated
                                                + ss
                                                + "(v2) <- comparableConstraint(v1,v2), "
                                                + isAnnotated + key + "(v1), !"
                                                + cannotBeAnnotated + ss
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
            encodingForInequalityConstraint.append(cannotBeAnnotated + s
                    + "(v1) <- inequalityConstraint(v1,v2), " + isAnnotated + s
                    + "(v2).\n");
            encodingForInequalityConstraint.append(cannotBeAnnotated + s
                    + "(v2) <- inequalityConstraint(v1,v2), " + isAnnotated + s
                    + "(v1).\n");
            for (String ss : allTypesInString) {
                if (s != ss) {
                    variableMaybeAnnotated.append(mayBeAnnotated + ss
                            + "(v1) <- inequalityConstraint(v1,v2), "
                            + isAnnotated + s + "(v2), !" + cannotBeAnnotated
                            + ss + "(v1).\n");
                    variableMaybeAnnotated.append(mayBeAnnotated + ss
                            + "(v2) <- inequalityConstraint(v1,v2), "
                            + isAnnotated + s + "(v1), !" + cannotBeAnnotated
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
                    .append(cannotBeAnnotated
                            + s
                            + "(v1) <- inequalityConstraintContainsModifier(v1,v2), v2 = \""
                            + s + "\".\n");
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
            encodingForEqualityConModifier
                    .append(isAnnotated
                            + s
                            + "(v2) <- equalityConstraintContainsModifier(v1,v2), v1 = \""
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
            basicEncoding.append(isAnnotated + s + "(v) ->variable(v).\n"
                    + mayBeAnnotated + s + "(v) ->variable(v).\n"
                    + cannotBeAnnotated + s + "(v) ->variable(v).\n"
                    + "AnnotationOf[v] = \"" + s + "\" <-isAnnotated" + s
                    + "(v).\n");
        }
    }

    /**
     * generate the encoding of adaptation constraint of universe type system.
     * 
     * @param encodingForAdaptationConstraint
     *            is the return value.
     * @returns encodingForAdaptationConstraint is the encoding of adaptation
     *          constraint for universe type system.
     */
    private StringBuilder getEncodingForAdaptationConstraint(
            StringBuilder encodingForAdaptationConstraint) {
        InferenceChecker IC = new GUTIChecker();
        GUTIChecker GC = (GUTIChecker) IC;
        if (IC instanceof AdaptationInference) {
            encodingForAdaptationConstraint = GC.viewpointEncodingFor();
        }
        return encodingForAdaptationConstraint;
    }

    /**
     * write all encoding generated by this class to file LogiqlEncoding.logic.
     * 
     * @throws FileNotFoundException
     * 
     */
    private void writeFile(StringBuilder output) throws FileNotFoundException {
        String writePath = path + "/LogiqlEncoding.logic";
        File f = new File(writePath);
        PrintWriter pw = new PrintWriter(f);
        pw.write(output.toString());
        pw.close();
    }
}
