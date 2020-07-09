package com.rumenz;



import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;


public class DemoApplication {

    public static void main(String[] args) {
         AnnotationConfigApplicationContext ac=new AnnotationConfigApplicationContext();
         ac.register(Config.class);
         ac.refresh();
         Map<String, Rumenz> map = ac.getBeansOfType(Rumenz.class);
         map.forEach((k,v)->{
           System.out.println(k+"==="+v.toString());
         });
        //单一查找  com.rumenz.Config 需要加一个@Primary
        lookUpByObjectProvider(ac);
        //单一查找  com.rumenz.Config 需要加一个@Primary
        lookUpAvailable(ac);
        //集合查找
        lookUpByStream(ac);

        ac.close();
    }

    private static void lookUpByStream(AnnotationConfigApplicationContext ac) {

        ObjectProvider<Rumenz> beanProvider = ac.getBeanProvider(Rumenz.class);
        beanProvider.stream().forEach(System.out::println);
    }

    private static void lookUpAvailable(AnnotationConfigApplicationContext ac) {
        ObjectProvider<Rumenz> beanProvider = ac.getBeanProvider(Rumenz.class);
        Rumenz r = beanProvider.getIfAvailable(Rumenz::createRumenz);
        System.out.println(r);
    }


    private static void lookUpByObjectProvider(AnnotationConfigApplicationContext ac){
        ObjectProvider<Rumenz> beanProvider = ac.getBeanProvider(Rumenz.class);
        System.out.println(beanProvider.getObject());

    }

}
