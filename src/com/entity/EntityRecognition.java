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
import com.neo4j.Relation;


public class EntityRecognition {
	Logger logger = Logger.getLogger(EntityRecognition.class.getName()); 

	BaseDao dao = null;

	final static String charsetName = "GBK";

	public EntityRecognition(BaseDao dao) {
		this.dao = dao;
	}

	public EntityRecognition() {
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
	/*
	 * 法律文书中的声明
	 */
	final static String accuser_names[] = {"再审申请人","申请再审人","申诉人","上诉人","原告","原审第三人","申请人","申请复议人","申请执行人"};
	final static String defendant_names[] = {"再审被申请人","被申请人","被申诉人","被上诉人","被告人","被告",
		"原审被告人","原审被告","被执行人","罪犯"};
	final static String lawyer_names[] = {"委托代理人","诉讼代理人","代理人","辩护人","委托辩护人"};
	final static String others[] = {"法定代表人","负责人","代理权限"};

	public static void main(String[] args) {
		/*EntityRecognition er = new EntityRecognition();
		File file = new File("test/sentence");
		er.recognize(file,charsetName);
		System.out.println(file.getName() + " processing" );
		System.out.println("accuser:" + er.accuser);
		System.out.println("lawyer:" + er.lawyer);
		System.out.println("defendant:" + er.defendant);*/
		
		BaseDao dao = new BaseDao();
		
		/*File baseDir = new File("D:\\data\\law_book_guo");
		File[] catDirs = baseDir.listFiles();
		for (File dir : catDirs) {
			File[] files = dir.listFiles();
			System.out.println(dir.getName()+"下处理文件："+files.length);
			for (File file : files) {
				//要注意构造函数使用
				EntityRecognition er = new EntityRecognition(dao);
				System.out.println(file.getName() + " processing" ); 

				er.recognize(file,charsetName);

				er.create(dir.getName(),file.getName().substring(0,file.getName().indexOf(".")));

//				System.out.println("accuser:" + er.accuser);
//				System.out.println("lawyer:" + er.lawyer);
//				System.out.println("defendant:" + er.defendant);
//				System.out.println("----------------------------------------------------------");
			}
		}*/

		EntityRecognition er = new EntityRecognition(dao);
		er.recommend("郭明", Entity.Lawyer);
		dao.logout();

	}
	
	public void recommend(String name, Entity id) {
		switch (id) {
		case Lawyer:	
			dao.recommendLawyer(name);
			break;

		case Accuser:			
			dao.recommendClient(name);
			break;
			
		default:
			dao.recommendClient(name);
			break;
		}
	}

	/**
	 * 根据案件创建数据库
	 * @param relation
	 * @param cases
	 */
	private void create(String relation,String cases) {

		int case_id = dao.creatNode(cases, Entity.Case);
		Relation r = judgeRelation(relation);
		//创建 法律事件和原告之间的关系
		List<Integer> accuser_ids = new ArrayList<Integer>(accuser.size());
		for (int i = 0; i < accuser.size(); i++) {
			int accuser_id = dao.creatNode(accuser.get(i), Entity.Accuser);
			accuser_ids.add(accuser_id);
			dao.createRelationshipTo(case_id, accuser_id,r);
		}

		//创建 法律事件和被告之间的关系
		List<Integer> defendant_ids = new ArrayList<Integer>(defendant.size());
		for (int i = 0; i < defendant.size(); i++) {
			int defendant_id = dao.creatNode(defendant.get(i), Entity.Defendant);
			defendant_ids.add(defendant_id);
			dao.createRelationshipTo(case_id, defendant_id,r);
		}

		//创建 法律事件和律师之间的关系
		List<Integer> lawyer_ids = new ArrayList<Integer>(lawyer.size());
		for (int i = 0; i < lawyer.size(); i++) {
			int lawyer_id = dao.creatNode(lawyer.get(i), Entity.Lawyer);
			lawyer_ids.add(lawyer_id);
			dao.createRelationshipTo(case_id, lawyer_id,r);
		}

	}

	private Relation judgeRelation(String relation) {
		/**
		 * 民事案件
		 */
		if( relation.contains("人格权"))
			return Relation.Civil_Personal_Right;
		else if( relation.contains("婚姻家庭"))
			return Relation.Civil_Marriage;
		else if( relation.contains("合同"))
			return Relation.Civil_Contract;
		else if( relation.contains("知识产权"))
			return Relation.Civil_IPR;
		else if( relation.contains("海事海商"))
			return Relation.Civil_Maritime;
		
		/**
		 * 刑事案件
		 */
		else if( relation.contains("公共安全"))
			return Relation.Criminal_Public_Safety;
		else if( relation.contains("侵犯公民人身权利"))
			return Relation.Criminal_Personal_Right;
		else if( relation.contains("侵犯财产"))
			return Relation.Criminal_Property;
		else if( relation.contains("社会管理秩序"))
			return Relation.Criminal_Social_Management_Order;
		else if( relation.contains("贪污贿赂"))
			return Relation.Criminal_Corruption;
		else if( relation.contains("渎职"))
			return Relation.Criminal_Malfeasance;
		
		/**
		 * 行政案件
		 */
		else if( relation.contains("行政案件"))
			return Relation.Administration;
		
		/**
		 * 赔偿案件
		 */
		else if( relation.contains("赔偿案件"))
			return Relation.Indemnity;
		
		/**
		 * 执行案件
		 */
		else 
			return Relation.Carry;
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
			while (reader.ready() && !isAnnouncement((line = reader.readLine())) ) 
				continue;


			Boolean people_in_law = true;
			while (reader.ready() && people_in_law ) {

				/* 1.空格不被trim; 2.处理掉括号里的辅助内容 */
				line = line.trim().replace("　　", "").replaceAll("\\（[\u4E00-\u9FA5]+\\）", "");

				/*
				 * 两种处理模式
				 */
				if(line.contains("：") || line.contains(":")) { 
					parseToken(line);

				} else {
					parseWithoutToken(line);
				}

				
				if( !isAnnouncement((line = reader.readLine())) )
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
	 * 是声明的时候返回声明的位置
	 * 以行开始判断是否是声明
	 * 问题是不能完全判定，故加了长度限制
	 * @param line
	 * @return
	 */
	private boolean isAnnouncement(String line) {
		line = line.trim().replace("　　", "").replaceAll("\\（[\u4E00-\u9FA5]+\\）", "");
		
		String removeToken[] = line.split("\\pP");
		if( removeToken.length > 0 && removeToken[0].length() >= 25)
			return false;
		if(line.contains("一案") && line.contains("本院"))
			return false;
		
		for (int i = 0; i < accuser_names.length; i++) {
			if( line.startsWith(accuser_names[i]))
				return true;
		}
		for (int i = 0; i < defendant_names.length; i++) {
			if( line.startsWith(defendant_names[i]))
				return true;
		}
		for (int i = 0; i < lawyer_names.length; i++) {
			if( line.startsWith(lawyer_names[i]))
				return true;
		}
		for (int i = 0; i < others.length; i++) {
			if( line.startsWith(others[i]))
				return true;
		}
		return false;
	}

	/**
	 * 冒号分隔的声明
	 * @param line
	 */
	private void parseToken(String line) {
		String id_name[] =  null;
		
		if(line.contains(":"))
			id_name = line.split(":");
		if(line.contains("："))
			id_name = line.split("：");

		//根据标点分割为法律身份和名字
		//原告被告仅仅是contains
		if(id_name.length < 2)
			return ;
		String ids[] = id_name[0].split("\\pP");
		String name[] = id_name[1].split("\\pP");

		for (int i = 0; i < accuser_names.length; i++) {
			if( ids[0].equals(accuser_names[i])) {
				if( !accuser.contains(name[0]) && name[0].length()<25) {
					accuser.add(name[0]);
					return ;
				}
			}
		}
		for (int i = 0; i < lawyer_names.length; i++) {
			if( ids[0].equals(lawyer_names[i])) {
				if( !lawyer.contains(name[0]) && name[0].length()<25) {
					lawyer.add(name[0]);
					return ;
				}
			}
		}
		for (int i = 0; i < defendant_names.length; i++) {
			if( ids[0].equals(defendant_names[i])) {
				if( !defendant.contains(name[0]) && name[0].length()<25) {
					defendant.add(name[0]);
					return ;
				}
			}
		}
		/*if(ids[0].contains(accuser_names[0]) || ids[0].contains(accuser_names[1]) || 
				ids[0].equals(accuser_names[2]) || ids[0].equals(accuser_names[3]) ||
				ids[0].contains(accuser_names[4]) || ids[0].contains(accuser_names[5]) ||
				ids[0].equals(accuser_names[6]) || ids[0].equals(accuser_names[7])) 
		{
			if( !accuser.contains(name[0]) && name[0].length()<25)
				accuser.add(name[0]);
		}
		else if(ids[0].contains(lawyer_names[0]) || ids[0].contains(lawyer_names[1]) ||
				ids[0].contains(lawyer_names[2]) || ids[0].contains(lawyer_names[3])  ||
				ids[0].contains(lawyer_names[4]) )
		{
			if( !lawyer.contains(name[0]) && name[0].length()<25)
				lawyer.add(name[0]);
		}
		else if(ids[0].contains(defendant_names[0]) || ids[0].contains(defendant_names[1]) || 
				ids[0].contains(defendant_names[2]) || ids[0].equals(defendant_names[3]) || 
				ids[0].equals(defendant_names[4]) || ids[0].equals(defendant_names[5]) ||
				ids[0].equals(defendant_names[6]) || ids[0].equals(defendant_names[7]))
		{
			if( !defendant.contains(name[0]) && name[0].length()<25)
				defendant.add(name[0]);
		}*/

	}

	/**
	 * 无冒号分隔的声明
	 * @param line
	 */
	private void parseWithoutToken(String line) {
		String id_name = line.split("\\pP")[0];
		
		for (int i = 0; i < accuser_names.length; i++) {
			if( id_name.startsWith(accuser_names[i])) {
				String tmp = id_name.substring(id_name.indexOf(accuser_names[i]) + accuser_names[i].length());
				if( !accuser.contains(tmp) && tmp.length()<25)
					accuser.add(tmp);
				return ;
			}
		}
		for (int i = 0; i < defendant_names.length; i++) {
			if( id_name.startsWith(defendant_names[i])) {
				String tmp = id_name.substring(id_name.indexOf(defendant_names[i]) + defendant_names[i].length());
				if( !defendant.contains(tmp)  && tmp.length()<25)
					defendant.add(tmp);
				return ;
			}
		}
		for (int i = 0; i < lawyer_names.length; i++) {
			if( id_name.startsWith(lawyer_names[i])) {
				String tmp = id_name.substring(id_name.indexOf(lawyer_names[i]) + lawyer_names[i].length());
				if( !lawyer.contains(tmp)&& tmp.length()<25)
					lawyer.add(tmp);
				return ;
			}
		}
	}

}
