package util;

import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Abort;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;

import static com.sun.tools.javac.util.List.from;
import static com.sun.tools.javac.util.List.of;

/*
    Copied from openJDK9: http://hg.openjdk.java.net/jdk9/jdk9/langtools/file/e18190929198/test/tools/lib/types/TypeHarness.java#l374
    
 */

public class TypeHarness {

    protected Types types;
    protected ReusableJavaCompiler tool;

    public TypeHarness() {
        Context ctx = new Context();
        JavacFileManager.preRegister(ctx);
        MyAttr.preRegister(ctx);
        tool = new ReusableJavaCompiler(ctx);
        types = Types.instance(ctx);
    }
    
    public static void main(String[] args) {

        ArrayList<String> imports = new ArrayList<>();
        ArrayList<String> typeVars = new ArrayList<>();
        TypeHarness typeHarness = new TypeHarness();
        StrToTypeFactory strToTypeFactory = typeHarness.new StrToTypeFactory(null, imports, typeVars);
        Type aType = strToTypeFactory.getType("java.lang.Object[]");
        Type aType1 = strToTypeFactory.getType("int");
    }

    /**
     * An example is reported below:
     * List<String> imports = new ArrayList<>();
     * imports.add("java.util.*");
     * List<String> typeVars = new ArrayList<>();
     * typeVars.add("T");
     * strToTypeFactory = new StrToTypeFactory(null, imports, typeVars);
     * Type freeType = strToTypeFactory.getType("List<? extends T>");
     * Type aType = strToTypeFactory.getType("List<? extends String>");
     */

    public class StrToTypeFactory {

        int id = 0;
        String pkg;
        java.util.List<String> imports;
        public java.util.List<String> typeVarDecls;
        public List<Type> typeVariables;

        public StrToTypeFactory(String pkg, java.util.List<String> imports,
                java.util.List<String> typeVarDecls) {

            this.pkg = pkg;
            this.imports = imports;
            this.typeVarDecls = typeVarDecls;
            this.typeVariables = from(typeVarDecls.stream()
            .map(this::typeVarName)
            .map(this::getType)
            .collect(Collectors.toList())
            );
        }

        TypeVar getTypeVarFromStr(String name) {

            if (typeVarDecls == null) {
                return null;
            }
            int index = typeVarDecls.indexOf(name);
            if (index != -1) {
                return (TypeVar) typeVariables.get(index);
            }
            return null;
        }

        List<Type> getTypeVars() {
            return typeVariables;
        }

        String typeVarName(String typeVarDecl) {
            String[] ss = typeVarDecl.split(" ");
            return ss[0];
        }

        public final Type getType(String type) {

            JavaSource source = new JavaSource(type);
            MyAttr.theType = null;
            MyAttr.typeParameters = List.nil();
            tool.clear();
            List<JavaFileObject> inputs = of(source);

            try {
                tool.compile(inputs);
            } catch (Throwable ex) {
                throw new Abort(ex);
            }

            if (typeVariables != null) {
                return types.subst(MyAttr.theType, MyAttr.typeParameters, typeVariables);
            }

            return MyAttr.theType;
        }

        class JavaSource extends SimpleJavaFileObject {

            String id;
            String type;
            String template = "#Package;\n" +
            "#Imports\n" +
            "class G#Id#TypeVars {\n" +
            "   #FieldType var;" +
            "}";

            JavaSource(String type) {
                super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE);
                this.id = String.valueOf(StrToTypeFactory.this.id++);
                this.type = type;

            }

            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                String impStmts = imports.size() > 0 ?
                imports.stream().map(i -> "import " + i + ";").collect(Collectors.joining("\n")) : "";
                String tvars = typeVarDecls.size() > 0 ?
                typeVarDecls.stream().collect(Collectors.joining(",", "<", ">")) : "";
                return template
                .replace("#Package", (pkg == null) ? "" : "package " + pkg + ";")
                .replace("#Imports", impStmts)
                .replace("#Id", id)
                .replace("#TypeVars", tvars)
                .replace("#FieldType", type);
            }
        }
    }

    static class MyAttr extends Attr {

        private static Type theType;
        private static List<Type> typeParameters = List.nil();

        static void preRegister(Context context) {
            context.put(attrKey, (com.sun.tools.javac.util.Context.Factory<Attr>) c -> new MyAttr(c));
        }

        MyAttr(Context context) {
            super(context);
        }

        @Override
        public void visitVarDef(JCVariableDecl tree) {
            super.visitVarDef(tree);
            theType = tree.type;
        }

        @Override
        public void attribClass(DiagnosticPosition pos, ClassSymbol c) {
            super.attribClass(pos, c);
            ClassType ct = (ClassType) c.type;
            typeParameters = ct.typarams_field;
        }
    }

    static class ReusableJavaCompiler extends JavaCompiler {

        ReusableJavaCompiler(Context context) {
            super(context);
        }

        @Override
        public void close() {
            // do nothing
        }

        protected void checkReusable() {
            // do nothing
        }

        void clear() {
            newRound();
        }

        public void newRound() {
            inputFiles.clear();
            todo.clear();
        }
    }
}

