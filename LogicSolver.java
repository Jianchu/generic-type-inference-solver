package checkers.inference.solver.LogiqlDebugSolver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import javax.swing.text.html.HTMLDocument.Iterator;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.TypeHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;

import checkers.inference.InferenceSolver;
import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.util.InferenceUtil;
import ostrusted.quals.OsTrusted;
import ostrusted.quals.OsUntrusted;

import org.checkerframework.javacutil.AnnotationProvider;
import org.checkerframework.javacutil.AnnotationUtils;

/**
 * 
 *
 * @author Jianchu Li
 *
 */

public class LogicSolver implements InferenceSolver {

    Map<String, String> subtype = new HashMap<String, String>();
    Map<String, String> supertype = new HashMap<String, String>();
    Map<String, String> notComparable = new HashMap<String, String>();
    Set<? extends AnnotationMirror> allTypes;
    String top ="";
    String bottom = "";
    final String isAnnotated = "isAnnotated";
    final String mayBeAnnotated = "mayBeAnnotated";
    final String cannotBeAnnotated = "cannotBeAnnotated";
    
    @Override
    public Map<Integer, AnnotationMirror> solve(
            Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
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

        // print();
        System.out.println(basicEncoding);
        System.out.println(encodingForEqualityConModifier);
        System.out.println(encodingForInequalityConModifier);
        System.out.println(encodingForEqualityConstraint);
        System.out.println(encodingForInequalityConstraint);
        System.out.println(encodingForComparableConstraint);
        System.out.println(encodingForSubtypeConTopBottom);
        System.out.println(encodingForSubtypeConstraint);
        System.out.println(encodingForAdaptationConstraint);
        return null;

    }

    public void getTopBottomQualifier(QualifierHierarchy hierarchy){
        for (AnnotationMirror i :hierarchy.getTopAnnotations()){
            top = i.toString().replaceAll("[.@]", "_");
        }
        for (AnnotationMirror j :hierarchy.getBottomAnnotations()){
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
                    + isAnnotated + s
                    + "[v1] = true <- equalityConstraint[v1,v2] = true, "
                    + isAnnotated + s + "[v2] = true.\n";
            encodingForEqualityConstraint = encodingForEqualityConstraint
                    + isAnnotated + s
                    + "[v2] = true <- equalityConstraint[v1,v2] = true, "
                    + isAnnotated + s + "[v1] = true.\n";

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
                            + "[v1] = true <- subtypeConstraint[v1,v2] = true, "
                            + isAnnotated + subtypeFors[i] + "[v2] = true.\n";
                } if (!subtypeFors[i].equals(" ") && !(subkey.equals(top) && subtypeFors[i].equals(top))) {
                    encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                            + mayBeAnnotated
                            + subkey
                            + "[v2] = true <- subtypeConstraint[v1,v2] = true, "
                            + isAnnotated
                            + subtypeFors[i]
                            + "[v1] = true, "
                            + "!"
                            + cannotBeAnnotated
                            + subkey
                            + "[v2] = true.\n";
                }
            }

            for (int j = 1; j < supertypeFors.length; j++) {
                if (!supertypeFors[j].equals(subkey)
                        && !supertypeFors[j].equals(" ")) {
                    encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                            + cannotBeAnnotated
                            + subkey
                            + "[v2] = true <- subtypeConstraint[v1,v2] = true, "
                            + isAnnotated + supertypeFors[j] + "[v1] = true.\n";
                } if (!supertypeFors[j].equals(" ") && !(subkey.equals(bottom) && supertypeFors[j].equals(bottom))) {
                    encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                            + mayBeAnnotated
                            + subkey
                            + "[v1] = true <- subtypeConstraint[v1,v2] = true, "
                            + isAnnotated
                            + supertypeFors[j]
                            + "[v2] = true, "
                            + "!"
                            + cannotBeAnnotated
                            + subkey
                            + "[v1] = true.\n";
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
                        + "[v2] = true <- subtypeConstraint[v1,v2] = true, "
                        + isAnnotated + subkey + "[v1] = true.\n";
            }
        }
        for (String superkey : supertype.keySet()) {
            subtypeFors = supertype.get(superkey).split(" ");
            if (subtypeFors.length == allTypesInString.size() + 1) {
                encodingForSubtypeConTopBottom = encodingForSubtypeConTopBottom
                        + isAnnotated + superkey
                        + "[v1] = true <- subtypeConstraint[v1,v2] = true, "
                        + isAnnotated + superkey + "[v2] = true.\n";
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
                                + "[v1] = true <- comparableConstraint[v1,v2] = true, "
                                + isAnnotated + s + "[v2] = true.\n";
                        encodingForComparableConstraint = encodingForComparableConstraint
                                + cannotBeAnnotated
                                + key
                                + "[v2] = true <- comparableConstraint[v1,v2] = true, "
                                + isAnnotated + s + "[v1] = true.\n";
                        for (String ss : allTypesInString) {
                            if (!ss.equals(s)) {
                                variableMaybeAnnotated = variableMaybeAnnotated
                                        + mayBeAnnotated
                                        + ss
                                        + "[v1] = true <- comparableConstraint[v1,v2] = true, "
                                        + isAnnotated + key + "[v2] = true, !"
                                        + cannotBeAnnotated + ss
                                        + "[v1] = true.\n";
                                variableMaybeAnnotated = variableMaybeAnnotated
                                        + mayBeAnnotated
                                        + ss
                                        + "[v2] = true <- comparableConstraint[v1,v2] = true, "
                                        + isAnnotated + key + "[v1] = true, !"
                                        + cannotBeAnnotated + ss
                                        + "[v2] = true.\n";
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
                    + "[v1] = true <- inequalityConstraint[v1,v2] = true, "
                    + isAnnotated + s + "[v2] = true.\n";
            encodingForInequalityConstraint = encodingForInequalityConstraint
                    + cannotBeAnnotated + s
                    + "[v2] = true <- inequalityConstraint[v1,v2] = true, "
                    + isAnnotated + s + "[v1] = true.\n";
            for (String ss : allTypesInString) {
                if (s != ss) {
                    variableMaybeAnnotated = variableMaybeAnnotated
                            + mayBeAnnotated
                            + ss
                            + "[v1] = true <- inequalityConstraint[v1,v2] = true, "
                            + isAnnotated + s + "[v2] = true, !"
                            + cannotBeAnnotated + ss + "[v1] = true.\n";
                    variableMaybeAnnotated = variableMaybeAnnotated
                            + mayBeAnnotated
                            + ss
                            + "[v2] = true <- inequalityConstraint[v1,v2] = true, "
                            + isAnnotated + s + "[v1] = true, !"
                            + cannotBeAnnotated + ss + "[v2] = true.\n";
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
                    + "[v1] = true <- inequalityConstraintContainsModifier[v1,v2] = true, v2 = \""
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
                    + "[v2] = true <- equalityConstraintContainsModifier[v1,v2] = true, v1 = \""
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
                + "\nadaptationConstraint[v1,v2,v3] = a -> variable(v1), variable(v2), variable(v3), boolean(a)."
                + "\nequalityConstraint[v1,v2] = e -> variable(v1), variable(v2), boolean(e)."
                + "\nequalityConstraintContainsModifier[v1,v2] = e -> modifier(v1), variable(v2), boolean(e)."
                + "\ninequalityConstraint[v1,v2] = e -> variable(v1), variable(v2), boolean(e)."
                + "\ninequalityConstraintContainsModifier[v1,v2] = e -> modifier(v2), variable(v1), boolean(e)."
                + "\ncomparableConstraint[v1,v2] = e -> variable(v1), variable(v2), boolean(e)."
                + "\nsubtypeConstraint[v1,v2] = s -> variable(v1), variable(v2), boolean(s).\n";
        for (String s : allTypesInString) {
            basicEncoding = basicEncoding + isAnnotated + s
                    + "[v]=i ->variable(v),boolean(i).\n" + mayBeAnnotated + s
                    + "[v]=i ->variable(v),boolean(i).\n" + cannotBeAnnotated
                    + s + "[v]=i ->variable(v),boolean(i).\n"
                    + "AnnotationOf[v] = \"" + s + "\" <-isAnnotated" + s
                    + "[v] = true.\n";
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

    public void print() {

        Set<String> Keys = subtype.keySet();
        System.out.println("subtype relation:");
        for (String a : Keys) {
            System.out.println("supertype: " + a + ",subtype: "
                    + subtype.get(a));
        }
        System.out.println("supertype relation");
        for (String b : Keys) {
            System.out.println("subtype: " + b + ",supertype: "
                    + supertype.get(b));
        }
        System.out.println("notComparableFori");
        Set<String> Keys1 = notComparable.keySet();
        for (String c : Keys1) {
            System.out.println("for: " + c + ", not Comparable with: "
                    + notComparable.get(c));
        }
    }
}

