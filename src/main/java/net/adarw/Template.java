package net.adarw;

import java.util.ArrayList;

public class Template {
    static class Component{
        enum ComponentType{
            BOOLEAN,
            STRING,
            INT,
            DOUBLE
        }
        public ComponentType type;
        public String name;

        public Component(ComponentType type, String name){
            this.type = type;
            this.name = name;
        }

    }

    public ArrayList<Component> components = new ArrayList<>();
}
