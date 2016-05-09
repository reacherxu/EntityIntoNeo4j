package com.neo4j;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
		ins.recommendLawyer("郭明");
//		System.out.println(ins.getDirectChildrenNum("郭明", Entity.Lawyer , Relation.Civil));
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
	public int createRelationshipTo(int id1,int id2,Relation r) {
		String sql = "start m=node({1}),n=node({2}) CREATE m-[r:" + r + "]->n";
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
		String sql = "start n=node({1}) match n-[]-(m:" + e + ") return ID(m) as id,m.name as name order by ID(m)";
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
		String sql = "start n=node({1}) match n-[*1..]-(m:" + e + ") return ID(m) as id order by ID(m)";
		List<Map<String, Object>> result = neoConn.queryList(sql,id);
		return result;
	}
	
	/**
	 * 返回指定身份人的直接相连案件
	 * @param name
	 * @param e
	 * @param r
	 * @return
	 */
	public int getDirectChildrenNum(String name,Entity e, Relation r) {
		String sql = "match (n:"+e+")-[r:"+r+"]-(m:Case) where n.name={1} return count(m) as count";
		List<Map<String, Object>> result = neoConn.queryList(sql,name);
		return (Integer) result.get(0).get("count");
	}
	
	/**
	 * 返回指定节点的全部子节点个数
	 * @param id
	 * @return
	 */
	public int getChildrenNum(int id,Entity e ) {
		String sql = "start n=node({1}) match n-[*1..]-(m:" + e + ") return ID(m) as id ";
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
		String sql = "start n=node({1}) match (n:Node)-[]-(m:Node) where m.name='" + property + "' return m.id as id";
		List<Map<String, Object>> result = neoConn.queryList(sql,id);
		return result.size() == 0 ? false : true;
	}
	
	/**
	 *  寻找id的叶子节点 ,添加属性 
	 * @param id
	 * @return
	 */
	public String getLeaf(Integer id) {
		String sql = "start n=node({1}) match (n:Node)-[r:Related_to*0..]-(m:Node) return m.name as name";
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
		String sql = "start n=node({1}) match (n:Node)-[r*0..]-(m:Node) where m.name={2} return ID(m) as id order by ID(m)";
		
		List<Map<String, Object>> nameList = neoConn.queryList(sql,id,name);
		
		List<Integer> names = new ArrayList<Integer>();
		for (int i = 0; i < nameList.size(); i++) 
			names.add( (Integer)nameList.get(i).get("id") );
		
		return names;
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	public String getCaseName(Relation r) {
		String ret = null;
		switch (r) {
		/**
		 * 民事案件
		 */
		case Civil_Personal_Right:
			ret = "人格权纠纷";
			break;
		case Civil_Marriage:
			ret = "婚姻家庭纠纷";
			break;
		case Civil_Contract:
			ret = "合同纠纷";
			break;
		case Civil_IPR:
			ret = "知识产权纠纷";
			break;
		case Civil_Maritime:
			ret = "海事海商纠纷";
			break;
			
			
			/**
			 * 刑事案件
			 */
		case Criminal_Public_Safety:
			ret = "危害公共安全";
			break;
		case Criminal_Personal_Right:
			ret = "侵犯公民人身权利";
			break;
		case Criminal_Property:
			ret = "侵犯财产";
			break;
		case Criminal_Social_Management_Order:
			ret = "妨害社会管理秩序";
			break;
		case Criminal_Corruption:
			ret = "贪污贿赂";
			break;
		case Criminal_Malfeasance:
			ret = "渎职";
			break;
			
			/**
			 * 行政案件
			 */
		case Administration:
			ret = "行政";
			break;
		case Indemnity:
			ret = "赔偿";
			break;
		case Carry:
			ret = "执行";
			break;
	
		}
		return ret;
	}
	
	/**
	 * 判别律师处理哪类案件比较多
	 * @param name
	 */
	public void recommendLawyer(String name) {
		Map<Relation,Integer> caseTypes = new TreeMap<Relation, Integer>();
		int max_count = 0;
		Relation max_count_r = null;
		
		for(Relation r : Relation.values()) {
			int count = getDirectChildrenNum(name, Entity.Lawyer, r);
			
			if(max_count < count) {
				max_count = count;
				max_count_r = r;
			}
			
			if(count != 0)
				caseTypes.put(r, count);
		}
		
		//TODO　对map进行处理，则可以进行推荐
		System.out.println("\n\n");
		System.out.println("律师" + name + "经历如下：" );
		for(Map.Entry<Relation,Integer> entry : caseTypes.entrySet()) {
			int cases = entry.getValue();
			if( cases != 0 )
				System.out.println("处理"+getCaseName(entry.getKey())+"案件"+cases+"起");
		}
		System.out.println("在"+getCaseName(max_count_r)+"案件上推荐律师"+name);
	}

	/**
	 * 判别客户处理哪类案件比较多
	 * @param name
	 */
	public void recommendClient(String name) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
