package Beans;

import java.util.ArrayList;
import java.util.List;

public class Type {
    private String name;
    private List<String> members = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(String member) {
        this.members.add(member);
    }

    public Type() {
        this.name = name;
    }

    public Type(String name) {
        this.name = name;
    }
}
