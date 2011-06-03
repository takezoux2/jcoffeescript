/*
 * Copyright 2011 David Yeung
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

import java.io.*;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: takeshita
 * Date: 11/06/03
 * Time: 12:35
 * To change this template use File | Settings | File Templates.
 */
public class ContinuousCompiler implements FileChangeListener{


    FileObserver observer;
    JCoffeeScriptCompiler compiler;

    public ContinuousCompiler(String path,Collection<Option> options, String[] extensions,boolean recursive){
        observer = new FileObserver(path);
        observer.setExtensions(extensions);
        observer.setRecursive(recursive);
        observer.setListener(this);
        compiler = new JCoffeeScriptCompiler(options);
        System.out.println("Observing " + observer.getObserveDirectory().getAbsolutePath() + " ...");
    }

    public void start(){
        observer.start();
    }
    public void stop(){
        observer.stop();
    }

    String format = "Compile %s";
    @Override
    public void onUpdate(String filePath) {
        System.out.println(String.format(format,filePath));
        compile(filePath);

    }

    @Override
    public void onCreate(String filePath) {
        System.out.println(String.format(format,filePath));
        compile(filePath);
    }

    @Override
    public void onDelete(String filePath) {
        File f = new File(getJsFileName(filePath));

        if(f.exists() && f.isFile()){
            System.out.print("Delete " + f.getAbsolutePath());
            f.delete();
        }
    }

    protected String getJsFileName(String coffee){
        int pos = coffee.lastIndexOf(".");
        String outputFileName;
        if(pos > 0){
            return coffee.substring(0,pos) + ".js";
        }else{
            return coffee + ".js";
        }
    }

    protected void compile(String path){

        String outputFileName = getJsFileName(path);

        try {
            FileInputStream stream = new FileInputStream(path);

            byte[] file = new byte[stream.available()];
            stream.read(file);
            String coffee = new String(file,"utf-8");

            FileOutputStream output = new FileOutputStream(outputFileName);
            String js = compiler.compile(coffee);
            output.write(js.getBytes("utf-8"));


        } catch (FileNotFoundException e) {
            // not occure
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JCoffeeScriptCompileException e) {
            e.printStackTrace();
        }

    }

}
