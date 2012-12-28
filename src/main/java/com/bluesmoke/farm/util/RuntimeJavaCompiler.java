package com.bluesmoke.farm.util;

import java.io.File;
import java.util.*;
import javax.tools.*;
import javax.tools.JavaCompiler.*;
public class RuntimeJavaCompiler {
    public static void compile(JavaObjectFromString code, String packageName, String className) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector diagnosticsCollector =  new DiagnosticCollector();
        StandardJavaFileManager fileManager  =  compiler.getStandardFileManager(diagnosticsCollector, null, null);
        Iterable fileObjects = Arrays.asList(code);
        CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector, null, null, fileObjects);
        Boolean result = task.call();
        List<Diagnostic> diagnostics = diagnosticsCollector.getDiagnostics();
        for(Diagnostic d : diagnostics){
            System.out.println(d.getMessage(null));
        }
        if(result == true){
            System.out.println("Compilation has succeeded");

            packageName = "target/classes/" + packageName.replaceAll("\\.", "/");
            File folder = new File(packageName);
            if(!folder.exists())
            {
                folder.mkdirs();
            }

            File fileToMove = new File(className + ".class");
            fileToMove.renameTo(new File(packageName + "/" + className + ".class"));

        }else{
            System.out.println("Compilation fails.");
        }
    }
}
