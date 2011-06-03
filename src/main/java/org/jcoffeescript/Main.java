/*
 * Copyright 2010 David Yeung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jcoffeescript;

import com.sun.org.apache.bcel.internal.classfile.FieldOrMethod;

import java.io.*;
import java.text.Normalizer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class Main {
    private static final int BUFFER_SIZE = 262144;
    private static final int BUFFER_OFFSET = 0;

    public static void main(String[] args) {
        new Main().execute(args, System.out, System.in);
    }

    public void execute(String[] args, PrintStream out, InputStream in) {
        final Collection<Option> options = readOptionsFrom(args);

        if(options.contains(Option.CONTINUOUS_COMPILE)){
            String dir = getValue(args,"--dir");
            ContinuousCompiler compiler;
            boolean recursive = !options.contains(Option.NOT_RECURSIVE);
            if(dir == null){
                compiler = new ContinuousCompiler(".",options,new String[]{"coffee"},recursive);
            }else{
                compiler = new ContinuousCompiler(dir,options,new String[]{"coffee"},recursive);
            }

            try{
                compiler.start();
                boolean run = true;
                out.println("Type 'exit' to exit.");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                while(run){
                    String line = reader.readLine();
                    if(line.equals("exit")){
                        run = false;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                compiler.stop();
            }

        }else{
            try {
                out.print(new JCoffeeScriptCompiler(options).compile(readSourceFrom(in)));
            } catch (JCoffeeScriptCompileException e) {
                System.err.println(e.getMessage());
            }
        }

    }


    private String readSourceFrom(InputStream inputStream) {
        final InputStreamReader streamReader = new InputStreamReader(inputStream);
        try {
            try {
                StringBuilder builder = new StringBuilder(BUFFER_SIZE);
                char[] buffer = new char[BUFFER_SIZE];
                int numCharsRead = streamReader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
                while (numCharsRead >= 0) {
                    builder.append(buffer, BUFFER_OFFSET, numCharsRead);
                    numCharsRead = streamReader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
                }
                return builder.toString();
            } finally {
                streamReader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getValue(String[] args , String key){
        for(int i = 0;i < args.length - 1 ; i ++){
            if(args[i].equals(key)){
                return args[i + 1];
            }
        }
        return null;
    }

    private Collection<Option> readOptionsFrom(String[] args) {
        final Collection<Option> options = new LinkedList<Option>();

        for(String arg : args){
            if(arg.equals("--bare")){
                options.add(Option.BARE);
            }else if(arg.equals("--cc")){
                options.add(Option.CONTINUOUS_COMPILE);
            }else if(arg.equals("--nr")){
                options.add(Option.NOT_RECURSIVE);
            }
        }
        return options;
    }
}
