package com.test;

import java.io.File;

import com.neo4j.Entity;

public class Main {

	public static void main(String[] args) {
		Entity e = Entity.Lawyer;
		System.out.println(e);
		
		File file = new File("D:\\temp\\corpus_mini\\train\\法律-劳动争议\\吴小军、深圳市宝安区观澜日广电子厂、日广有限公司劳动合同纠纷....txt");
		System.out.println(file.getName().substring(0,file.getName().indexOf(".")));
	}
}
