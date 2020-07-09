## Bean 延迟依赖查找接口

```java
// 一个对象工厂
public interface ObjectFactory<T> {

    // 返回一个对象
    T getObject() throws BeansException;
}

```
  - org.springframework.beans.factory.ObjectFactory
  
> 此接口定义了一个简单工厂，是一个函数式接口，可以在调用时返回一个对象实例(可能是共享的或独立的)。



## org.springframework.beans.factory.ObjectProvider

> ObjectProvider继承自ObjectFactory


```java
// 1.可以看到ObjectProvider本身继承了ObjectFactory接口，所以它本身就是一个ObjectFactory
// 2.从5.1之后，这个接口还多继承了一个Iterable接口，意味着能对它进行迭代以及流式操作
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {

	// 返回用指定参数创建的bean, 如果容器中不存在, 抛出异常
	T getObject(Object... args) throws BeansException;

	// 如果指定类型的bean注册到容器中, 返回 bean 实例, 否则返回 null
	@Nullable
	T getIfAvailable() throws BeansException;

	// 如果返回对象不存在，则用传入的Supplier获取一个Bean并返回，否则直接返回存在的对象
	default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfAvailable();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	 // 消费对象的一个实例（可能是共享的或独立的），如果存在通过Consumer回调消耗目标对象。
     // 如果不存在则直接返回
	default void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfAvailable();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	// 如果不可用或不唯一（没有指定primary）则返回null。否则，返回对象。
	@Nullable
	T getIfUnique() throws BeansException;

	// 如果不存在唯一对象，则调用Supplier的回调函数
	default T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfUnique();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	// 如果存在唯一对象，则消耗掉该对象
	default void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfUnique();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	// 返回符合条件的对象的Iterator，没有特殊顺序保证（一般为注册顺序）
	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	// 返回符合条件对象的连续的Stream，没有特殊顺序保证（一般为注册顺序）
	default Stream<T> stream() {
		throw new UnsupportedOperationException("Multi element access not supported");
	}

	// 返回符合条件对象的连续的Stream。在标注Spring应用上下文中采用@Order注解或实现Order接口的顺序
	default Stream<T> orderedStream() {
		throw new UnsupportedOperationException("Ordered element access not supported");
	}
}

```

- 函数式接口
  - getIfAvailable(Supplier) / 单一查找
  - ifAvailable(Consumer)  / 单一查找
- Stream 扩展 - stream()  / 集合查找


**实体类Rumenz.java**

```java
package com.rumenz;
public class Rumenz{

    private Integer id;
    private String name;

    @Override
    public String toString() {
        return "Rumenz{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
    
   public static Rumenz createRumenz (){
        Rumenz r=new Rumenz();
        r.setId(123);
        r.setName("static创建的Rumenz");
        return r;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

```

**配置类Config.java**

```java
package com.rumenz;
import org.springframework.context.annotation.Bean;
public class Config {
    @Bean
    public Rumenz rumenz1(){
        Rumenz r=new Rumenz();
        r.setId(456);
        r.setName("入门小站");
        return r;
    }
    @Bean
    public Rumenz rumenz(){
        Rumenz r=new Rumenz();
        r.setId(123);
        r.setName("入门小站");
        return r;
    }
}
```

## ObjectProvider 单一查找(必须加@Primary)

```java
package com.rumenz;



import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;


public class DemoApplication {

    public static void main(String[] args) {
         AnnotationConfigApplicationContext ac=new AnnotationConfigApplicationContext();
         ac.register(Config.class); //注册Rumenz
         ac.refresh();
         Map<String, Rumenz> map = ac.getBeansOfType(Rumenz.class);
         map.forEach((k,v)->{
            System.out.println(k+"==="+v.toString());
         });

        lookUpByObjectProvider(ac);

        ac.close();
    }

    private static void lookUpByObjectProvider(AnnotationConfigApplicationContext ac){
        ObjectProvider<Rumenz> beanProvider = ac.getBeanProvider(Rumenz.class);
        System.out.println(beanProvider.getObject());

    }

}

```
**报错**
```
rumenz1===Rumenz{id=456, name='入门小站'} SuperRumenz{key='null'}
rumenz===Rumenz{id=123, name='入门小站'} SuperRumenz{key='null'}
Exception in thread "main" org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'com.rumenz.Rumenz' available: expected single matching bean but found 2: rumenz1,rumenz
```
**原因**

> 发现两个类型一样的Bean

**解决方案**

> 使用@Primary 推选一个主要的Bean

```java
@Bean
@Primary //添加注解
public Rumenz rumenz(){
    Rumenz r=new Rumenz();
    r.setId(123);
    r.setName("入门小站");
    return r;
}
```

**输出**

```
Rumenz{id=123, name='入门小站'}
```

## ObjectProvider / Supplier 单一查找(必须加@Primary)
  
> 如果当前获取的Bean不存在那么就创建一个Bean(达到延迟加载和兜底的优雅效果)

```java
package com.rumenz;



import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;


public class DemoApplication {

    public static void main(String[] args) {
         AnnotationConfigApplicationContext ac=new AnnotationConfigApplicationContext();
         ac.register(DemoApplication.class); //注意:不注册Config
         ac.refresh();
        lookUpAvailable(ac);

        ac.close();
    }



    private static void lookUpByObjectProvider(AnnotationConfigApplicationContext ac){
        ObjectProvider<Rumenz> beanProvider = ac.getBeanProvider(Rumenz.class);
        System.out.println(beanProvider.getObject());

    }

}

```

**输出**

```
Rumenz{id=123, name='static创建的Rumenz'}
```


## Stream 扩展集合查找 (不用加@Primary)

```java
package com.rumenz;



import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;


public class DemoApplication {

    public static void main(String[] args) {
         AnnotationConfigApplicationContext ac=new AnnotationConfigApplicationContext();
         ac.register(Config.class);
         ac.refresh();
        lookUpByStream(ac);

        ac.close();
    }

    private static void lookUpByStream(AnnotationConfigApplicationContext ac) {
        ObjectProvider<Rumenz> beanProvider = ac.getBeanProvider(Rumenz.class);
        beanProvider.stream().forEach(System.out::println);
    }
}

```

**输出**

```
Rumenz{id=456, name='入门小站'}
Rumenz{id=123, name='入门小站'}
```

源码:https://github.com/mifunc/Spring-Bean-Delay-lookUp


原文: [https://rumenz.com/rumenbiji/Spring-Bean-Delay-lookUp.html](https://rumenz.com/rumenbiji/Spring-Bean-Delay-lookUp.html)
