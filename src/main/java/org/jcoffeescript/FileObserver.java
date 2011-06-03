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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: takeshita
 * Date: 11/06/03
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
public class FileObserver {


    File observeDirectory = null;
    boolean recursive = true;
    String[] extensions = new String[]{"coffee"};
    int observeInterval = 5;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    Map<String,Long> observings = new HashMap<String,Long>();

    volatile boolean stop = true;

    FileChangeListener listener = null;

    public FileObserver(){
        observeDirectory = new File(".");
    }

    public FileObserver(String path){
        observeDirectory = new File(path);
    }

    public void setInterval(int interval,TimeUnit timeUnit){
        this.observeInterval = interval;
        this.timeUnit = timeUnit;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }


    public FileChangeListener getListener() {
        return listener;
    }

    public void setListener(FileChangeListener listener) {
        this.listener = listener;
    }

    public File getObserveDirectory(){
        return observeDirectory;
    }

    public void start(){
        synchronized (observings){
            if(stop){
                stop = false;
                Checker checker = new Checker();
                Thread thread = new Thread(checker);
                thread.setDaemon(true);
                thread.start();
            }
        }
    }
    public void stop(){
        stop = true;
    }


    public void check(){
        List<File> files = listUpFiles();
        checkDelete(files);
        checkUpdate(files);
        checkCreate(files);
    }

    protected void checkDelete(List<File> files){
        HashSet<String> fSet = new HashSet<String>();
        for(File f : files){
            fSet.add(f.getAbsolutePath());
        }
        List<String> deleted = new ArrayList<String>();
        for(Map.Entry<String,Long> e : observings.entrySet()){
            if(!fSet.contains(e.getKey())){
                deleted.add(e.getKey());
            }
        }

        for(String del : deleted){
            observings.remove(del);
            try{
                listener.onDelete(del);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    protected void checkCreate(List<File> files){
        for(File f : files){
            String path = f.getAbsolutePath();
            if(!observings.containsKey(path)){
                observings.put(path,f.lastModified());
                try{
                    listener.onCreate(path);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

    }
    protected void checkUpdate(List<File> files){
        for(File f: files){
            String path = f.getAbsolutePath();
            if(observings.containsKey(path) &&
                    !observings.get(path).equals(f.lastModified())){
                observings.put(path,f.lastModified());
                try{
                    listener.onUpdate(path);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }



    private List<File> listUpFiles(){
        List<File> files = new ArrayList<File>();
        return listUpFiles(observeDirectory);
    }

    private List<File> listUpFiles(File dir){
        File[] files = dir.listFiles(
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    for(String ext : extensions){
                        if(name.endsWith("." + ext)){
                            return true;
                        }
                    }
                    return false;
                }
            });

        List<File> result;
        if(recursive){
            result = new ArrayList<File>();
        }else{
            result = new ArrayList<File>(files.length);
        }
        Collections.addAll(result,files);

        if(recursive){
            File[] dirs = dir.listFiles(new FileFilter(){
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() && !pathname.getName().startsWith(".");
                }
            });
            for(File d : dirs){
                result.addAll(listUpFiles(d));
            }
        }

        return result;

    }


    static class FileRecord{
        public String File;
        public String LastUpdated;
    }

    class Checker implements Runnable{
        @Override
        public void run() {
            check();
            while(!stop){
                try{
                    Thread.sleep(timeUnit.toMillis(observeInterval));
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                check();

            }
        }
    }

}
