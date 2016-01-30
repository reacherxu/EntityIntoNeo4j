package com.neo4j;


import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.util.Util;


public class BaseDao {
	
	public NeoConnection neoConn ;
	
	//数据的版本信息
	private final String VERSION = "0.1-SNAPSHOT";
	
	/*
	 * 构造函数
	 */
	public BaseDao() {
		neoConn = NeoConnection.getNeoConnection();
	}
	
	public String getVERSION() {
		return VERSION;
	}

	public NeoConnection getNeoConn() {
		return neoConn;
	}


	public void setNeoConn(NeoConnection neoConn) {
		this.neoConn = neoConn;
	}
	
	public static void main(String[] args) {
		//"114.212.83.134:7474"  "172.26.13.122:7474"
		BaseDao ins = new BaseDao();
		
		System.out.println(ins.hasNode("辛州", Entity.Lawyer));
		ins.logout();
	}
	
	/**
	 * 登出
	 */
	public void logout() {
		neoConn.logout();
	}

	/**
	 * 根据节点类型，创建节点
	 * @param name	创建的节点名称
	 * @param e	节点类型
	 * @return	返回创建的节点id
	 */
	public int creatNode(String name,Entity e) {
		int nodeID = hasNode(name,e);
		
		if( nodeID == 0) {
			String sql = "create (n:" + e + 
					" {version:'" + VERSION + "',created_time:'" + Util.getCurrentTime()
					+ "',ip:'" + Util.getIP() + "',name:{1}}) return ID(n)";

			Map<String, Object> rs = neoConn.query(sql,name);
			nodeID = (Integer) rs.get("ID(n)");
		}
				
		return nodeID;
	}
	
	/**
	 * 根据节点id创建关系
	 * @param id1
	 * @param id2
	 * @return	返回受影响的行数
	 */
	public int createRelationshipTo(int id1,int id2) {
		String sql = "start m=node({1}),n=node({2}) CREATE m-[r:Related_to]->n";
		int rows = neoConn.update(sql,id1,id2);
		return rows;
	}
	
	/**
	 * 是否存在类型为e的节点
	 * @param name
	 * @param e
	 * @return 存在，则返回id,否则返回0
	 */
	public Integer hasNode(String name,Entity e) {
		String sql = "match (n:" + e + ") where n.name={1} return ID(n) as id";
		List<Map<String, Object>> result = neoConn.queryList(sql,name);
		
		if(result.size() == 0)
			return 0;
		else
			return (Integer) result.get(0).get("id");
	}

	/*-------------------------删除节点信息--------------------------------------------*/
	
	/**
	 * 级联删除节点，包括自身
	 * @param id
	 * @return	返回受影响的行数
	 */
	public int detachDelete(int id) {
		String sql = "start n=node({1}) match (n)-[*0..]->(m) detach delete m";
		int rows = neoConn.update(sql,id);
		return rows;
	}
	
	/*-------------------------修改节点信息--------------------------------------------*/
	
	/**
	 * 设置节点属性  或   添加节点属性
	 * @param id	节点id
	 * @param name	属性名
	 * @param value	属性值
	 */
	public void setProperty(int id, Object name, Object value) {
		String sql = "start n=node({1}) set n." + name.toString() + "=" + value.toString();
		neoConn.update(sql, id);
		
	}
	
	
	/*-------------------------查询节点信息--------------------------------------------*/
	
	/**
	 * 获取节点位置信息
	 * @param id
	 * @return
	 */
	public Point[] getLocation(int id) {
		String sql = "start n=node({1}) return n.location as location";
		Map<String, Object> result = neoConn.query(sql,id);
		
		String locStr[] = result.get("location").toString().split(";");
		Point[] location = new Point[locStr.length];
		for (int i = 0; i < location.length; i++) {
			String[] pos = locStr[i].split(",",2);
			location[i] = new Point((int)Double.parseDouble(pos[0].toString()),(int)Double.parseDouble(pos[1].toString()));
		}
		
		return location;
	}
	
	/**
	 * 查询id节点的name
	 * @param id
	 * @return
	 */
	public String getName(int id) {
		String sql = "start n=node({1}) return n.name as name";
		Map<String, Object> rs = neoConn.query(sql,id);
		return (String) rs.get("name");
	}
	
	/**
	 * 查询图中根节点的id
	 * @return
	 */
	public Integer getRoot() {
		String sql = "start n=node(*) match (n:Node) where n.name='syntax' return ID(n) as id";
		Map<String, Object> rs = neoConn.query(sql);
		return (Integer) rs.get("id");
	}
	
	/**
	 * 返回指定节点的直接子节点的id和name
	 * @param id
	 * @return
	 */
	public List<Map<String, Object>> getDirectChildren(int id,Entity e ) {
		String sql = "start n=node({1}) match n-[]->(m:" + e + ") return ID(m) as id,m.name as name order by ID(m)";
		List<Map<String, Object>> result = neoConn.queryList(sql,id);
		return result;
	}
	
	/**
	 * 返回指定节点的全部子节点
	 * order by ID(m)  --->指定是否要排序
	 * @param id
	 * @return
	 */
	public List<Map<String, Object>> getChildren(int id,Entity e ) {
		String sql = "start n=node({1}) match n-[*1..]->(m:" + e + ") return ID(m) as id order by ID(m)";
		List<Map<String, Object>> result = neoConn.queryList(sql,id);
		return result;
	}
	
	
	/**
	 * 返回指定节点的直接子节点个数
	 * @param id
	 * @return
	 */
	public int getDirectChildrenNum(int id,Entity e ) {
		String sql = "start n=node({1}) match n-[]->(m:" + e + ") return ID(m) as id";
		List<Map<String, Object>> result = neoConn.queryList(sql,id);
		return result.size();
	}
	
	/**
	 * 返回指定节点的全部子节点个数
	 * @param id
	 * @return
	 */
	public int getChildrenNum(int id,Entity e ) {
		String sql = "start n=node({1}) match n-[*1..]->(m:" + e + ") return ID(m) as id ";
		List<Map<String, Object>> result = neoConn.queryList(sql,id);
		return result.size();
	}
	
	/**
	 * 判断id节点是否有名为property的直接子节点
	 * @param id
	 * @param property
	 * @return
	 */
	public boolean hasDirectChild(Integer id,String property) {
		String sql = "start n=node({1}) match (n:Node)-[]->(m:Node) where m.name='" + property + "' return m.id as id";
		List<Map<String, Object>> result = neoConn.queryList(sql,id);
		return result.size() == 0 ? false : true;
	}
	
	/**
	 *  寻找id的叶子节点 ,添加属性 
	 * @param id
	 * @return
	 */
	public String getLeaf(Integer id) {
		String sql = "start n=node({1}) match (n:Node)-[r:Related_to*0..]->(m:Node) return m.name as name";
		List<Map<String, Object>> nodes = this.getNeoConn().queryList(sql,id);
		return nodes.get(nodes.size()-1).get("name").toString();
	}
	
	/**
	 * 返回某节点 名字为name的节点id(可以是多个节点)
	 * @param id
	 * @param name
	 * @return
	 */
	public List<Integer> getIdByName(int id,String name) {
		/* order by 确保得到的子节点是最近的 */
		String sql = "start n=node({1}) match (n:Node)-[r*0..]->(m:Node) where m.name={2} return ID(m) as id order by ID(m)";
		
		List<Map<String, Object>> nameList = neoConn.queryList(sql,id,name);
		
		List<Integer> names = new ArrayList<Integer>();
		for (int i = 0; i < nameList.size(); i++) 
			names.add( (Integer)nameList.get(i).get("id") );
		
		return names;
	}
	
	
	
}
