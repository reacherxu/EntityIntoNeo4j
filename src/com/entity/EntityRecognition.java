package com.entity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.neo4j.BaseDao;
import com.neo4j.Entity;


public class EntityRecognition {
	Logger logger = Logger.getLogger(EntityRecognition.class.getName()); 
	
	BaseDao dao = null;
	
	final static String charsetName = "GBK";
	
	public EntityRecognition(BaseDao dao) {
		this.dao = dao;
	}
	/* 
	 * 原告
	 */
	List<String> accuser = new ArrayList<String>();
	/* 
	 * 被告 
	 */
	List<String> defendant = new ArrayList<String>();
	/* 
	 * 律师
	 */
	List<String> lawyer = new ArrayList<String>();
	
	final static String names[] = {"再审申请人","申请再审人","申诉人","上诉人","原告","委托代理人","被申请人","被申诉人","被上诉人","被告"};
//	static List<String> annoucement = Arrays.asList(names);
	
	public static void main(String[] args) {
		/*EntityRecognition er = new EntityRecognition();
		File file = new File("test/sentence");
		er.recognize(file);
		System.out.println(file.getName() + " processing" );
		System.out.println("accuser:" + er.accuser);
		System.out.println("lawyer:" + er.lawyer);
		System.out.println("defendant:" + er.defendant);*/
		BaseDao dao = new BaseDao();
		
		File dir = new File("D:\\文书上网\\律师为郭明");
		File[] files = dir.listFiles();
		for (File file : files) {
			EntityRecognition er = new EntityRecognition(dao);
			er.recognize(file,charsetName);
			
			//TODO 名字要去掉后缀
			er.create(file.getName().substring(0,file.getName().indexOf(".")));
			/*System.out.println(file.getName() + " processing" );
			System.out.println("accuser:" + er.accuser);
			System.out.println("lawyer:" + er.lawyer);
			System.out.println("defendant:" + er.defendant);
			System.out.println("----------------------------------------------------------");*/
		}
		
		dao.logout();
		
	}

	/**
	 * 根据案件创建数据库
	 * @param cases
	 */
	private void create(String cases) {
		
		
		int case_id = dao.creatNode(cases, Entity.Case);
		
		//创建 法律事件和原告之间的关系
		List<Integer> accuser_ids = new ArrayList<Integer>(accuser.size());
		for (int i = 0; i < accuser.size(); i++) {
			int accuser_id = dao.creatNode(accuser.get(i), Entity.Accuser);
			accuser_ids.add(accuser_id);
			dao.createRelationshipTo(case_id, accuser_id);
		}
		
		//创建 法律事件和被告之间的关系
		List<Integer> defendant_ids = new ArrayList<Integer>(defendant.size());
		for (int i = 0; i < defendant.size(); i++) {
			int defendant_id = dao.creatNode(defendant.get(i), Entity.Defendant);
			defendant_ids.add(defendant_id);
			dao.createRelationshipTo(case_id, defendant_id);
		}
		
		//创建 法律事件和律师之间的关系
		List<Integer> lawyer_ids = new ArrayList<Integer>(lawyer.size());
		for (int i = 0; i < lawyer.size(); i++) {
			int lawyer_id = dao.creatNode(lawyer.get(i), Entity.Lawyer);
			lawyer_ids.add(lawyer_id);
			dao.createRelationshipTo(case_id, lawyer_id);
		}
		
	}

	/**
	 * 根据文件识别实体
	 * @param file
	 * @param charsetName	文件的编码方式
	 */
	private void recognize(File file,String charsetName) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader (new FileInputStream(file),charsetName));
			
			String line = null;
			while (reader.ready() && (! (line = reader.readLine()).contains("：") || (line.indexOf("：")==line.length()-1)) ) 
				continue;
				
			
			Boolean people_in_law = true;
			while (reader.ready() && people_in_law && line.contains("：")) {
				
				/* 空格不被trim  */
				parse(line.trim().replace("　　", ""));
				
				line = reader.readLine();
				if(!line.contains("："))
					people_in_law = false;
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			logger.warning("file: " + file.getName() + " not found!");;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param line
	 */
	private void parse(String line) {
		String id_name[] = line.split("：");
		
		//根据标点分割为法律身份和名字
		//原告被告仅仅是contains
		String ids[] = id_name[0].split("\\pP");
		String name[] = id_name[1].split("\\pP");
		
		if(ids[0].contains(names[0]) || ids[0].contains(names[1]) || 
				ids[0].contains(names[2]) || ids[0].contains(names[3]) ||
				ids[0].contains(names[4]) ) 
		{
			if( !accuser.contains(name[0]) )
				accuser.add(name[0]);
		}
		else if(ids[0].contains(names[5]))
		{
			if( !lawyer.contains(name[0]) )
				lawyer.add(name[0]);
		}
		else if(ids[0].contains(names[6]) || ids[0].contains(names[7]) || 
				ids[0].contains(names[8]) || ids[0].contains(names[9]))
		{
			if( !defendant.contains(name[0]) )
				defendant.add(name[0]);
		}
			
	}

}
