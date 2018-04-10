package com.mybank.pc.task;

import java.util.List;

import com.jfinal.aop.Duang;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.collection.model.UnionpayBatchCollection;
import com.mybank.pc.collection.trade.CBatchQuerySrv;

public class CollectionBatchQueryTask implements Runnable {

	private CBatchQuerySrv cBatchQuerySrv = Duang.duang(CBatchQuerySrv.class);

	@Override
	public void run() {
		LogKit.info("开始调用批量代收查询接口...");
		SqlPara sqlPara = Db.getSqlPara("collection_trade.findNeedQueryBatchCollection");
		List<UnionpayBatchCollection> ubcList = UnionpayBatchCollection.dao.find(sqlPara);
		for (UnionpayBatchCollection unionpayBatchCollection : ubcList) {
			cBatchQuerySrv.batchQuery(unionpayBatchCollection);
		}
	}
}
