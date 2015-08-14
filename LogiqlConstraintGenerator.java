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
 * 
 *
 * @author Jianchu Li
 *
 */

public class LogiqlConstraintGenerator {

    Map<String, String> subtype = new HashMap<String, String>();
    Map<String, String> supertype = new HashMap<String, String>();
    Map<String, String> notComparable = new HashMap<String, String>();
    Set<? extends AnnotationMirror> allTypes;
    String top = "";
    String bottom = "";
    final String isAnnotated = "isAnnotated";
    final String mayBeAnnotated = "mayBeAnnotated";
    final String cannotBeAnnotated = "cannotBeAnnotated";
    QualifierHierarchy qualHierarchy;
    private final String currentPath = new File("").getAbsolutePath();

    public LogiqlConstraintGenerator(QualifierHierarchy qualHierarchy) {
        this.qualHierarchy = qualHierarchy;
    }

    public void GenerateLogiqlEncoding() throws IOException {
        allTypes = qualHierarchy.getTypeQualifiers();
        Set<String> allTypesInString = new HashSet<String>();
        String encodingForEqualityConModifier = "";
        String encodingForInequalityConModifier = "";
        String encodingForEqualityConstraint = "";
        String encodingForInequalityConstraint = "";
        String encodingForComparableConstraint = "";
        String encodingForSubtypeConTopBottom = "";
        String encodingForSubtypeConstraint = "";
        String encodingForAdaptationConstraint = "";
        String basicEncoding = "";
        for (AnnotationMirror i : allTypes) {
            allTypesInString.add(i.toString().replaceAll("[.@]", "_"));
        }
        getTopBottomQualifier(qualHierarchy);
        getSubSupertype(qualHierarchy);
        getNotComparable();
        basicEncoding = getBasicString(allTypesInString, basicEncoding);
        encodingForEqualityConModifier = getEncodingForEqualityConModifier(
                allTypesInString, encodingForEqualityConModifier);
        encodingForInequalityConModifier = getEncodingForInequalityConModifier(
                allTypesInString, encodingForInequalityConModifier);
        encodingForEqualityConstraint = getEncodingForEualityConstraint(
                allTypesInString, encodingForEqualityConstraint);
        encodingForInequalityConstraint = getEncodingForIneualityConstraint(
                allTypesInString, encodingForInequalityConstraint);
        encodingForComparableConstraint = getEncodingForComparableConstraint(
                allTypesInString, encodingForComparableConstraint);
        encodingForSubtypeConTopBottom = getEncodingForSubtypeConTopBottom(
                allTypesInString, encodingForSubtypeConTopBottom);
        encodingForSubtypeConstraint = getEncodingForSubtypeConstraint(
                allTypesInString, encodingForSubtypeConstraint);
        encodingForAdaptationConstraint = getEncodingForAdaptationConstraint(encodingForAdaptationConstraint);

        writeFile(basicEncoding + encodingForEqualityConModifier
                + encodingForInequalityConModifier
                + encodingForEqualityConstraint
                + encodingForInequalityConstraint
                + encodingForComparableConstraint
                + encodingForSubtypeConTopBottom + encodingForSubtypeConstraint
                + encodingForAdaptationConstraint);
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
   
    public void getTopBottomQualifier(QualifierHierarchy hierarchy) {
        for (AnnotationMirror i : hierarchy.getTopAnnotations()) {
            top = i.toString().replaceAll("[.@]", "_");
        }
        for (AnnotationMirror j : hierarchy.getBottomAnnotations()) {
            bottom = j.toString().replaceAll("[.@]", "_");
        }
    }

    public void getSubSupertype(QualifierHierarchy hierarchy) {
        for (AnnotationMirror i : allTypes) {
            String subtypeFori = "";
            String supertypeFori = "";
            for (AnnotationMirror j : allTypes) {
                if (hierarchy.isSubtype(j, i)) {
                    subtypeFori = subtypeFori + " "
                            + j.toString().replaceAll("[.@]", "_");
                }
                if (hierarchy.isSubtype(i, j)) {
                    supertypeFori = supertypeFori + " "
                            + j.toString().replaceAll("[.@]", "_");
                }
            }
            supertype.put(i.toString().replaceAll("[.@]", "_"), supertypeFori);
            subtype.put(i.toString().replaceAll("[.@]", "_"), subtypeFori);
        }
    }

    public void getNotComparable() {
        for (AnnotationMirror i : allTypes) {
            String notComparableFori = "";
            for (AnnotationMirror j : allTypes) {
                if (!subtype.get(i.toString().replaceAll("[.@]", "_"))
                        .contains(j.toString().replaceAll("[.@]", "_"))
                        && !subtype.get(j.toString().replaceAll("[.@]", "_"))
                                .contains(i.toString().replaceAll("[.@]", "_"))) {
                    notComparableFori = notComparableFori + " "
                            + j.toString().replaceAll("[.@]", "_");
                }
            }
            if (!notComparableFori.equals("")) {
                notComparable.put(i.toString().replaceAll("[.@]", "_"),
                        notComparableFori);
            }

        }
    }

    public String getEncodingForEualityConstraint(Set<String> allTypesInString,
            String encodingForEqualityConstraint) {
        for (String s : allTypesInString) {
            encodingForEqualityConstraint = encodingForEqualityConstraint
                    + isAnnotated + s + "(v1) <- equalityConstraint(v1,v2), "
                    + isAnnotated + s + "(v2).\n";
            encodingForEqualityConstraint = encodingForEqualityConstraint
                    + isAnnotated + s + "(v2)<- equalityConstraint(v1,v2), "
                    + isAnnotated + s + "(v1).\n";

        }
        return encodingForEqualityConstraint;
    }

    public String getEncodingForSubtypeConstraint(Set<String> allTypesInString,
            String encodingForSubtypeConTopBottom) {
        String[] subtypeFors;
        String[] supertypeFors;
        for (String subkey : allTypesInString) {
            subtypeFors = subtype.get(subkey).split(" ");
            supertypeFors = supertype.get(subkey).split(" ");
            for (int i = 1; i < subtypeFors.length; i++) {
                if (!subtypeFors[i].equals(subkey)
                        && !subtypeFors[i].equals(" ")) {
                    encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                            + cannotBeAnnotated
                            + subkey
                            + "(v1) <- subtypeConstraint(v1,v2), "
                            + isAnnotated + subtypeFors[i] + "(v2).\n";
                }
                if (!subtypeFors[i].equals(" ")
                        && !(subkey.equals(top) && subtypeFors[i].equals(top))) {
                    encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                            + mayBeAnnotated
                            + subkey
                            + "(v2) <- subtypeConstraint(v1,v2), "
                            + isAnnotated
                            + subtypeFors[i]
                            + "(v1), "
                            + "!"
                            + cannotBeAnnotated + subkey + "(v2).\n";
                }
            }

            for (int j = 1; j < supertypeFors.length; j++) {
                if (!supertypeFors[j].equals(subkey)
                        && !supertypeFors[j].equals(" ")) {
                    encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                            + cannotBeAnnotated
                            + subkey
                            + "(v2) <- subtypeConstraint(v1,v2), "
                            + isAnnotated + supertypeFors[j] + "(v1).\n";
                }
                if (!supertypeFors[j].equals(" ")
                        && !(subkey.equals(bottom) && supertypeFors[j]
                                .equals(bottom))) {
                    encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                            + mayBeAnnotated
                            + subkey
                            + "(v1) <- subtypeConstraint(v1,v2), "
                            + isAnnotated
                            + supertypeFors[j]
                            + "(v2), "
                            + "!"
                            + cannotBeAnnotated + subkey + "(v1).\n";
                }

            }
        }

        return encodingForSubtypeConTopBottom;

    }

    public String getEncodingForSubtypeConTopBottom(
            Set<String> allTypesInString, String encodingForSubtypeConTopBottom) {
        String[] subtypeFors;
        for (String subkey : subtype.keySet()) {
            subtypeFors = subtype.get(subkey).split(" ");
            if (subtypeFors.length == allTypesInString.size() + 1) {
                encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                        + isAnnotated + subkey
                        + "(v2) <- subtypeConstraint(v1,v2), " + isAnnotated
                        + subkey + "(v1).\n";
            }
        }
        for (String superkey : supertype.keySet()) {
            subtypeFors = supertype.get(superkey).split(" ");
            if (subtypeFors.length == allTypesInString.size() + 1) {
                encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                        + isAnnotated + superkey
                        + "(v1) <- subtypeConstraint(v1,v2), " + isAnnotated
                        + superkey + "(v2).\n";
            }
        }
        return encodingForSubtypeConTopBottom;
    }

    public String getEncodingForComparableConstraint(
            Set<String> allTypesInString, String encodingForComparableConstraint) {
        String variableMaybeAnnotated = "";
        if (notComparable.isEmpty() != true) {
            String[] notComparableForkey;
            for (String key : notComparable.keySet()) {
                notComparableForkey = notComparable.get(key).split(" ");
                for (String s : notComparableForkey) {
                    if (!s.equals("")) {
                        encodingForComparableConstraint = encodingForComparableConstraint
                                + cannotBeAnnotated
                                + key
                                + "(v1)<- comparableConstraint(v1,v2), "
                                + isAnnotated + s + "(v2).\n";
                        encodingForComparableConstraint = encodingForComparableConstraint
                                + cannotBeAnnotated
                                + key
                                + "(v2)<- comparableConstraint(v1,v2), "
                                + isAnnotated + s + "(v1).\n";
                        for (String ss : allTypesInString) {
                            if (!ss.equals(s)) {
                                variableMaybeAnnotated = variableMaybeAnnotated
                                        + mayBeAnnotated
                                        + ss
                                        + "(v1) <- comparableConstraint(v1,v2), "
                                        + isAnnotated + key + "(v2), !"
                                        + cannotBeAnnotated + ss + "(v1).\n";
                                variableMaybeAnnotated = variableMaybeAnnotated
                                        + mayBeAnnotated
                                        + ss
                                        + "(v2) <- comparableConstraint(v1,v2), "
                                        + isAnnotated + key + "(v1), !"
                                        + cannotBeAnnotated + ss + "(v2).\n";
                            }
                        }
                    }
                }
            }
        }
        encodingForComparableConstraint = encodingForComparableConstraint
                + variableMaybeAnnotated;
        return encodingForComparableConstraint;
    }

    public String getEncodingForIneualityConstraint(
            Set<String> allTypesInString, String encodingForInequalityConstraint) {
        String variableMaybeAnnotated = "";
        for (String s : allTypesInString) {
            encodingForInequalityConstraint = encodingForInequalityConstraint
                    + cannotBeAnnotated + s
                    + "(v1) <- inequalityConstraint(v1,v2), " + isAnnotated + s
                    + "(v2).\n";
            encodingForInequalityConstraint = encodingForInequalityConstraint
                    + cannotBeAnnotated + s
                    + "(v2) <- inequalityConstraint(v1,v2), " + isAnnotated + s
                    + "(v1).\n";
            for (String ss : allTypesInString) {
                if (s != ss) {
                    variableMaybeAnnotated = variableMaybeAnnotated
                            + mayBeAnnotated + ss
                            + "(v1) <- inequalityConstraint(v1,v2), "
                            + isAnnotated + s + "(v2), !" + cannotBeAnnotated
                            + ss + "(v1).\n";
                    variableMaybeAnnotated = variableMaybeAnnotated
                            + mayBeAnnotated + ss
                            + "(v2) <- inequalityConstraint(v1,v2), "
                            + isAnnotated + s + "(v1), !" + cannotBeAnnotated
                            + ss + "(v2).\n";
                }
            }
        }
        encodingForInequalityConstraint = encodingForInequalityConstraint
                + variableMaybeAnnotated;
        return encodingForInequalityConstraint;

    }

    public String getEncodingForInequalityConModifier(
            Set<String> allTypesInString,
            String encodingForInequalityConModifier) {
        for (String s : allTypesInString) {
            encodingForInequalityConModifier = encodingForInequalityConModifier
                    + cannotBeAnnotated
                    + s
                    + "(v1) <- inequalityConstraintContainsModifier(v1,v2), v2 = \""
                    + s + "\".\n";
        }

        return encodingForInequalityConModifier;
    }

    public String getEncodingForEqualityConModifier(
            Set<String> allTypesInString, String encodingForEqualityConModifier) {
        for (String s : allTypesInString) {
            encodingForEqualityConModifier = encodingForEqualityConModifier
                    + isAnnotated
                    + s
                    + "(v2) <- equalityConstraintContainsModifier(v1,v2), v1 = \""
                    + s + "\".\n";
        }
        return encodingForEqualityConModifier;
    }

    public String getBasicString(Set<String> allTypesInString,
            String basicEncoding) {
        basicEncoding = "variable(v), hasvariableName(v:i) -> int(i)."
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
                + "\nsubtypeConstraint(v1,v2) -> variable(v1), variable(v2).\n";
        for (String s : allTypesInString) {
            basicEncoding = basicEncoding + isAnnotated + s
                    + "(v) ->variable(v).\n" + mayBeAnnotated + s
                    + "(v) ->variable(v).\n" + cannotBeAnnotated + s
                    + "(v) ->variable(v).\n" + "AnnotationOf[v] = \"" + s
                    + "\" <-isAnnotated" + s + "(v).\n";
        }
        return basicEncoding;

    }

    public String getEncodingForAdaptationConstraint(
            String encodingForAdaptationConstraint) {
         InferenceChecker IC = new GUTIChecker();
         GUTIChecker GC = (GUTIChecker) IC;
         if (IC instanceof AdaptationInference ){
         encodingForAdaptationConstraint= GC.viewpointEncodingFor();
         }
        return encodingForAdaptationConstraint;
    }
    
    private void writeFile(String output){
        File file = new File(currentPath);
        String Base = file.getParent().toString();
        String Path = Base + "/src/checkers/inference/solver/LogiqlDebugSolver";
        try {
            String writePath = Path + "/LogiqlEncoding.logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(output);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
