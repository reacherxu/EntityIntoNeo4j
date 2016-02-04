package com.test;

import com.neo4j.Relation;


public class Main {

	public static void main(String[] args) {
		String regex="\\（[\u4E00-\u9FA5]+\\）";
//		String str="地方132（更新至）456 （更至）" ;
//		System.out.println(str);
//		System.out.println(str.replaceAll(regex, ""));
//		Entity e = Entity.Lawyer;
//		System.out.println(e);
//		
		String line = "上诉人（原审被告人）智万军（别名智海军），男，1967年8月11日出生。";
		System.out.println(line.substring(line.indexOf("上诉人") + "上诉人".length()));
		
		
		for (Relation e : Relation.values()) {
            System.out.println(e);
        }
	}
}
