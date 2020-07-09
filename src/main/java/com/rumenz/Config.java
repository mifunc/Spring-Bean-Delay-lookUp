package com.rumenz;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class Config {


    @Bean
    public Rumenz rumenz1(){
        Rumenz r=new Rumenz();
        r.setId(456);
        r.setName("入门小站");
        return r;
    }

    @Bean
    @Primary //单一类型查找Bean需要加上不然会报错
    //@Primary 优先考虑，优先考虑被注解的对象注入
    public Rumenz rumenz(){
        Rumenz r=new Rumenz();
        r.setId(123);
        r.setName("入门小站");
        return r;
    }


}
