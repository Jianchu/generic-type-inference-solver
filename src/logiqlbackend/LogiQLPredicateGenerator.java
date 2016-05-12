package logiqlbackend;

import org.checkerframework.javacutil.AnnotationUtils;

import java.io.File;
import java.io.PrintWriter;

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

    private final String path;

    public LogiQLPredicateGenerator(String path) {
        this.path = path;
    }


    public void GenerateLogiqlEncoding() {
        final String basicEncoding = getBasicEncoding();
        final String equalityEncoding = getEqualityConstraintEncoding();
        final String inequalityEncoding = getInequalityConstraintEncoding();
        System.out.println(basicEncoding);
        System.out.println(equalityEncoding);
        System.out.println(inequalityEncoding);

        // writeFile(basicEncoding.append(encodingForEqualityConModifier)
        // .append(encodingForInequalityConModifier)
        // .append(encodingForEqualityConstraint)
        // .append(encodingForInequalityConstraint)
        // .append(encodingForComparableConstraint)
        // .append(encodingForSubtypeConTopBottom).append(encodingForSubtypeConstraint).toString());

    }


    private String getEqualityConstraintEncoding() {
        StringBuilder equalityEncoding = new StringBuilder();
        for (AnnotationMirror annoMirror : Lattice.allTypes) {
            String simpleName = getSimpleName(annoMirror);
            equalityEncoding.append("is" + simpleName + "[v2] = true <- equalityConstraint(v1, v2), is"
                    + simpleName + "[v1] = true.\n");
            equalityEncoding.append("is" + simpleName
                    + "[v2] = true <- equalityConstraintContainsConstant(v1, v2), hasconstantName(v1:\""
                    + simpleName + "\").\n");
        }
        return equalityEncoding.toString();
    }
    
    private String getInequalityConstraintEncoding() {
        StringBuilder inequalityEncoding = new StringBuilder();
        for (AnnotationMirror annoMirror : Lattice.allTypes) {
            String simpleName = getSimpleName(annoMirror);
            inequalityEncoding.append("is" + simpleName + "[v2] = false <- inequalityConstraint(v1, v2), is"
                    + simpleName + "[v1] = true.\n");
            inequalityEncoding.append("is" + simpleName
                    + "[v2] = false <- inequalityConstraintContainsConstant(v1, v2), hasconstantName(v1:\""
                    + simpleName + "\").\n");
        }
        return inequalityEncoding.toString();
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
    // private void getEncodingForSubtypeConstraint(Set<String>
    // allTypesInString, StringBuilder encodingForSubtypeConstraint) {
    // String[] subtypeFors;
    // String[] supertypeFors;
    // for (String subkey : allTypesInString) {
    // subtypeFors = subtype.get(subkey).split(" ");
    // supertypeFors = supertype.get(subkey).split(" ");
    // for (int i = 1; i < subtypeFors.length; i++) {
    // if (!subtypeFors[i].equals(subkey)
    // && !subtypeFors[i].equals(" ")) {
    // encodingForSubtypeConstraint.append(CANNOTBEANNOTATED + subkey
    // + "(v1) <- subtypeConstraint(v1,v2), " + ISANNOTATED + subtypeFors[i]
    // + "(v2).\n");
    // }
    // if (!subtypeFors[i].equals(" ")
    // && !(subkey.equals(top) && subtypeFors[i].equals(top))) {
    // encodingForSubtypeConstraint.append(MAYBEANNOTATED + subkey
    // + "(v2) <- subtypeConstraint(v1,v2), " + ISANNOTATED + subtypeFors[i]
    // + "(v1), " + "!" + CANNOTBEANNOTATED + subkey + "(v2).\n");
    // }
    // }
    //
    // for (int j = 1; j < supertypeFors.length; j++) {
    // if (!supertypeFors[j].equals(subkey)
    // && !supertypeFors[j].equals(" ")) {
    // encodingForSubtypeConstraint.append(CANNOTBEANNOTATED
    // + subkey
    // + "(v2) <- subtypeConstraint(v1,v2), " + ISANNOTATED + supertypeFors[j]
    // + "(v1).\n");
    // }
    // if (!supertypeFors[j].equals(" ")
    // && !(subkey.equals(bottom) && supertypeFors[j]
    // .equals(bottom))) {
    // encodingForSubtypeConstraint.append(MAYBEANNOTATED + subkey
    // + "(v1) <- subtypeConstraint(v1,v2), "
    // + ISANNOTATED + supertypeFors[j]
    // + "(v2), " + "!" + CANNOTBEANNOTATED + subkey + "(v1).\n");
    // }
    //
    // }
    // }
    // }

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
    // private void getEncodingForSubtypeConTopBottom(
    // Set<String> allTypesInString,
    // StringBuilder encodingForSubtypeConTopBottom) {
    // String[] subtypeFors;
    // for (String subkey : subtype.keySet()) {
    // subtypeFors = subtype.get(subkey).split(" ");
    // if (subtypeFors.length == allTypesInString.size() + 1) {
    // encodingForSubtypeConTopBottom.append(ISANNOTATED + subkey
    // + "(v2) <- subtypeConstraint(v1,v2), " + ISANNOTATED
    // + subkey + "(v1).\n");
    // }
    // }
    // for (String superkey : supertype.keySet()) {
    // subtypeFors = supertype.get(superkey).split(" ");
    // if (subtypeFors.length == allTypesInString.size() + 1) {
    // encodingForSubtypeConTopBottom.append(ISANNOTATED + superkey
    // + "(v1) <- subtypeConstraint(v1,v2), " + ISANNOTATED
    // + superkey + "(v2).\n");
    // }
    // }
    // }

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
    // private void getEncodingForComparableConstraint(
    // Set<String> allTypesInString,
    // StringBuilder encodingForComparableConstraint) {
    // StringBuilder variableMaybeAnnotated = new StringBuilder();
    // if (notComparable.isEmpty() != true) {
    // String[] notComparableForkey;
    // for (String key : notComparable.keySet()) {
    // notComparableForkey = notComparable.get(key).split(" ");
    // for (String s : notComparableForkey) {
    // if (!s.equals("")) {
    // encodingForComparableConstraint
    // .append(CANNOTBEANNOTATED
    // + key
    // + "(v1)<- comparableConstraint(v1,v2), "
    // + ISANNOTATED + s + "(v2).\n");
    // encodingForComparableConstraint
    // .append(CANNOTBEANNOTATED
    // + key
    // + "(v2)<- comparableConstraint(v1,v2), "
    // + ISANNOTATED + s + "(v1).\n");
    // for (String ss : allTypesInString) {
    // if (!ss.equals(s)) {
    // variableMaybeAnnotated
    // .append(MAYBEANNOTATED
    // + ss
    // + "(v1) <- comparableConstraint(v1,v2), "
    // + ISANNOTATED + key
    // + "(v2), !" + CANNOTBEANNOTATED + ss
    // + "(v1).\n");
    // variableMaybeAnnotated
    // .append(MAYBEANNOTATED
    // + ss
    // + "(v2) <- comparableConstraint(v1,v2), "
    // + ISANNOTATED + key
    // + "(v1), !" + CANNOTBEANNOTATED + ss
    // + "(v2).\n");
    // }
    // }
    // }
    // }
    // }
    // }
    // encodingForComparableConstraint.append(variableMaybeAnnotated);
    // }



    /**
     * generate the basic predicate of encoding.
     *
     * @param allTypesInString
     *            is a set contains all modifiers of current type system in
     *            string.
     * @param basicEncoding
     *            is the return value.
     * 
     * @returns basicEncoding.
     */
    private String getBasicEncoding() {
        StringBuilder basicEncoding = new StringBuilder();
        basicEncoding.append("variable(v), hasvariableName(v:i) -> int(i).\n");
        basicEncoding.append("constant(m), hasconstantName(m:i) -> string(i).\n");
        basicEncoding.append("AnnotationOf[v] = a -> variable(v), string(a).\n");
        //predicates for making output in order.
        basicEncoding.append("variableOrder(v) -> int(v).\n");
        basicEncoding.append("variableOrder(i) <- variable(v), hasvariableName(v:i).\n");
        basicEncoding.append("orderVariable[o] = v -> int(o), int(v).\n");
        basicEncoding.append("orderVariable[o] = v <- seq<<o=v>> variableOrder(v).\n");
        basicEncoding.append("orderedAnnotationOf[v] = a -> int(v), string(a).\n");
        basicEncoding.append("orderedAnnotationOf[v] = a <- variable(q), hasvariableName(q:v), AnnotationOf[q]=a, orderVariable[_]=v.\n");
        //predicates for constraints.
        //equality constraint
        basicEncoding.append("equalityConstraint(v1,v2) -> variable(v1), variable(v2).\n");
        basicEncoding.append("equalityConstraintContainsConstant(v1,v2) -> constant(v1), variable(v2).\n");
        //inequality constraint
        basicEncoding.append("inequalityConstraint(v1,v2) -> variable(v1), variable(v2).\n");
        basicEncoding.append("inequalityConstraintContainsConstant(v1,v2) -> constant(v1), variable(v2).\n");
        //subtype constraint
        basicEncoding.append("subtypeConstraint(v1,v2) -> variable(v1), variable(v2).\n");
        basicEncoding.append("subtypeConstraintLeftConstant(v1,v2) -> constant(v1), variable(v2).\n");
        basicEncoding.append("subtypeConstraintRightConstant(v1,v2) -> variable(v1), constant(v2).\n");
        //comparable constraint
        basicEncoding.append("comparableConstraint(v1,v2) -> variable(v1), variable(v2).\n");
        basicEncoding.append("comparableConstraintContainsConstant(v1,v2) -> constant(v1), variable(v2).\n");
        // each type
        for (AnnotationMirror annoMirror : Lattice.allTypes) {
            basicEncoding.append("is" + getSimpleName(annoMirror) + "[v] = i -> variable(v), boolean(i).\n");
        }
        for (AnnotationMirror annoMirror : Lattice.allTypes) {
            String simpleName = getSimpleName(annoMirror);
            basicEncoding.append("AnnotationOf[v] = \"" + simpleName + "\" <- " + "is" + simpleName + "[v] = true.\n");
        }
        for (AnnotationMirror rightAnnoMirror : Lattice.allTypes) {
            for (AnnotationMirror leftAnnoMirror : Lattice.allTypes) {
                String leftAnnoName = getSimpleName(leftAnnoMirror);
                String rightAnnoName = getSimpleName(rightAnnoMirror);
                if (!leftAnnoName.equals(rightAnnoMirror)) {
                    basicEncoding.append("is" + leftAnnoName + "[v] = false <- is" + rightAnnoName + "[v] = true.\n");
                }
                
            }
        }
        return basicEncoding.toString();
    }

    private String getSimpleName(AnnotationMirror annoMirror) {
        return AnnotationUtils.annotationSimpleName(annoMirror).toString();
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
