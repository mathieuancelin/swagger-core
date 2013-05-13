package main;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Opcode;
import play.Logger;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.exceptions.UnexpectedException;
import play.libs.F.T2;
import play.classloading.enhancers.*;

public class Names extends Enhancer {

    @Override
    public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {
      return;
    }
    
    public static List<String> lookupParameterNames(Method method) {
       try {
           List<String> parameters = new ArrayList<String>();

           ClassPool classPool = newClassPool();
           CtClass ctClass = classPool.get(method.getDeclaringClass().getName());
           CtClass[] cc = new CtClass[method.getParameterTypes().length];
           for (int i = 0; i < method.getParameterTypes().length; i++) {
               cc[i] = classPool.get(method.getParameterTypes()[i].getName());
           }
           CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName(),cc);

           // Signatures names
           CodeAttribute codeAttribute = (CodeAttribute) ctMethod.getMethodInfo().getAttribute("Code");
           if (codeAttribute != null) {
               LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute("LocalVariableTable");
               if (localVariableAttribute != null && localVariableAttribute.tableLength() >= ctMethod.getParameterTypes().length) {
                   for (int i = 0; i < ctMethod.getParameterTypes().length + 1; i++) {
                       try {
                         String name = localVariableAttribute.getConstPool().getUtf8Info(localVariableAttribute.nameIndex(i));
                         if (!name.equals("this")) {
                           parameters.add(name);
                         }
                       } catch (Exception e) {
                          //System.out.println("Error with param " + i + " of method '" + method.toString() + "', " + e.toString());
                          //System.out.println("params are : " + parameters.toString());
                       }
                   }
               }
           }

           return parameters;
       } catch (Exception e) {
           throw new UnexpectedException("Cannot extract parameter names", e);
       }
   }
}
