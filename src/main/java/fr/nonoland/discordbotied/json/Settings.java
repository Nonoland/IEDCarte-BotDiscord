package fr.nonoland.discordbotied.json;

import java.util.ArrayList;

public class Settings {

    private long idChannel;
    private long idMessage;

    private ArrayList<Student> students;

    public Settings() {
        students = new ArrayList<>();
    }

    public void setIdChannel(long idChannel) {
        this.idChannel = idChannel;
    }

    public long getIdChannel() {
        return this.idChannel;
    }

    public void setIdMessage(long idMessage) {
        this.idMessage = idMessage;
    }

    public long getIdMessage() {
        return this.idMessage;
    }

    public void setStudents(ArrayList<Student> students) {
        this.students = students;
    }

    public ArrayList<Student> getStudents() {
        return students;
    }
}
