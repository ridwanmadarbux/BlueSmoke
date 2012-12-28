package com.bluesmoke.farm;

import org.junit.Test;

import java.util.TreeMap;

import static junit.framework.Assert.assertTrue;

public class MapToStringTest {

    @Test
    public void testMapToString()
    {
        TreeMap<Integer,TreeMap<Integer, String>> map = new TreeMap<Integer, TreeMap<Integer, String>>();
        map.put(0, new TreeMap<Integer, String>());
        map.put(1, new TreeMap<Integer, String>());
        map.put(2, new TreeMap<Integer, String>());
        map.put(3, new TreeMap<Integer, String>());

        map.get(1).put(-1, "a");
        map.get(2).put(-2, "b");
        map.get(3).put(1, "c");
        map.get(0).put(3, "d");
        map.get(2).put(2, "e");
        map.get(1).put(0, "f");

        String dist = map.toString().replaceAll("\\}\\}|[ ]", "");
        dist = dist.replaceAll("=\\{", ">");
        dist = dist.replaceAll("\\},", "<");
        dist = dist.replaceAll("=", "@");
        dist = dist.substring(1);

        System.out.println(dist);
        assertTrue(map.size() == 4);
    }
}
