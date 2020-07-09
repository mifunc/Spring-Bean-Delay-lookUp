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
