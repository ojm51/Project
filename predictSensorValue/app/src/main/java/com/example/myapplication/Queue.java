package com.example.myapplication;

import java.util.ArrayList;
import java.util.Collections;

public class Queue {
    private ArrayList<Float> arrayQueue = new ArrayList<Float>();
    public void enqueue(Float data) { arrayQueue.add(data); }
    public Float dequeue()
    { if(arrayQueue.size()==0)
    { System.out.println("데이터가 존재 하지 않습니다.");
        return null; }
        return arrayQueue.remove(0);
    }

    public int size(){
        return arrayQueue.size();
    }

    public float max(){
        return Collections.max(arrayQueue);
    }

    public float min(){
        return Collections.min(arrayQueue);
    }

    public float get(int index) {return arrayQueue.get(index); }
}
