package com.example.czojtest.unsafe;

import java.util.ArrayList;
import java.util.List;

/**
 * 无限占用时间 (浪费系统内存)
 */
public class MemoryError {
    public static void main(String[] args) throws InterruptedException{
        List<byte[]> bytes = new ArrayList<>();
        while(true){

        }
    }
}
