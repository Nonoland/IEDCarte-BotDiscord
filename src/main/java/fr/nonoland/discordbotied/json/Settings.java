package fr.nonoland.discordbotied.json;

import java.util.ArrayList;

public class Settings {

    private long idChannel;
    private long idMessageTable;
    private long idMessageInfo;

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

    public void setIdMessageTable(long idMessage) {
        this.idMessageTable = idMessage;
    }

    public long getIdMessageTable() {
        return this.idMessageTable;
    }

    public void setIdMessageInfo(long idMessageInfo) {
        this.idMessageInfo = idMessageInfo;
    }

    public long getIdMessageInfo() {
        return idMessageInfo;
    }

    public void setStudents(ArrayList<Student> students) {
        this.students = students;
    }

    public ArrayList<Student> getStudents() {
        return students;
    }
}
