package com.neo4j;


//TODO 关系进行扩充
public enum Relation {
	/**
	 * 民事案件
	 */
	/**
	 * 人格权
	 */
	Civil_Personal_Right,
	/**
	 * 婚姻家庭
	 */
	Civil_Marriage,
	/**
	 * 合同纠纷
	 */
	Civil_Contract,
	/**
	 * 知识产权
	 */
	Civil_IPR,
	/**
	 * 海事海商
	 */
	Civil_Maritime,
	
	
	/**
	 * 刑事案件
	 */
	/**
	 * 危害公共安全
	 */
	Criminal_Public_Safety,
	/**
	 * 侵犯公民人身权利
	 */
	Criminal_Personal_Right,
	/**
	 * 侵犯财产
	 */
	Criminal_Property,
	/**
	 * 妨害社会管理秩序
	 */
	Criminal_Social_Management_Order,
	/**
	 * 贪污贿赂
	 */
	Criminal_Corruption,
	/**
	 * 渎职
	 */
	Criminal_Malfeasance,
	
	
	
	/**
	 * 行政案件
	 */
	Administration,
	/**
	 * 赔偿案件
	 */
	Indemnity,
	/**
	 * 执行案件
	 */
	Carry
}
