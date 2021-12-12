package com.example.myapplication;

public class myInfo {
    String writer;
    String writerName;
    String writerPhone;
    String parentName;
    String parentPhone;

    public myInfo(String writer, String writerName, String writerPhone, String parentName, String parentPhone){
        this.writer = writer;
        this.writerName = writerName;
        this.writerPhone = writerPhone;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
    }

    public myInfo(){}

    public String getWriter() { return writer; }
    public String getWriterName() { return writerName; }
    public String getWriterPhone() { return writerPhone; }
    public String getParentName() { return parentName; }
    public String getParentPhone() { return parentPhone; }

    public void setWriter(String writer) { this.writer = writer; }
    public void setWriterName(String writerName) { this.writerName = writerName; }
    public void setWriterPhone(String writerPhone) { this.writerPhone = writerPhone; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
}
