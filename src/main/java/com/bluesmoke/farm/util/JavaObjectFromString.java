package com.bluesmoke.farm.util;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public class JavaObjectFromString extends SimpleJavaFileObject {
    private String contents = null;
    public JavaObjectFromString(String className, String contents) throws Exception{
        super(new URI(className), JavaFileObject.Kind.SOURCE);
        this.contents = contents;
    }
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return contents;
    }
}