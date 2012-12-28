package com.bluesmoke.farm.util;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RuntimeJavaFileCompiler {

    public static void compile(String classPath,String className, String code)
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String path = classPath;

        System.out.println(path);

        File file = new File(path + "/" + className + ".java");

        try{
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsolutePath());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(code);
            bw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        String fileToCompile = file.getAbsolutePath();

        int compilationResult = compiler.run(null, null, null, fileToCompile, "-classpath", path, "-s", path);


        if(compilationResult == 0){
            System.out.println("Compilation is successful");
        }else{
            System.out.println("Compilation Failed");
        }
    }
}
