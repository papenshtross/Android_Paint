package org.linnaeus.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 10.11.2010
 * Time: 17:44:00
 */

public class ReflectionUtils {

    public static <T> ArrayList<T> getClassInstancesBySuper(Class<T> superClass){

        ArrayList<T> items = new ArrayList<T>();

        try {
            String packageName = superClass.getPackage().getName();
            Class[] classes = ReflectionUtils.getClasses(packageName);

            for(Class _class : classes){
                if(!_class.equals(superClass)){
                    Object instance = _class.newInstance();

                    try{
                        items.add((T)instance);
                    }
                    catch(ClassCastException ex){
                        // Ignore such error. I have no idea how to check.
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

  public static Class[] getClasses(String pckgname) throws ClassNotFoundException {
    ArrayList classes=new ArrayList();
    // Get a File object for the package
    File directory=null;
    try {
      directory=new File(Thread.currentThread().getContextClassLoader().getResource('/'+pckgname.replace('.', '/')).getFile());
    } catch(NullPointerException x) {
      throw new ClassNotFoundException(pckgname+" does not appear to be a valid package");
    }
    if(directory.exists()) {
      // Get the list of the files contained in the package
      String[] files=directory.list();
      for(int i=0; i<files.length; i++) {
        // we are only interested in .class files
        if(files[i].endsWith(".class")) {
          // removes the .class extension
          classes.add(Class.forName(pckgname+'.'+files[i].substring(0, files[i].length()-6)));
        }
      }
    } else {
      throw new ClassNotFoundException(pckgname+" does not appear to be a valid package");
    }
    Class[] classesA=new Class[classes.size()];
    classes.toArray(classesA);
    return classesA;
  } 
}
