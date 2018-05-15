package com.mybank.pc.core;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.mybank.pc.admin.model.Attachment;
import com.mybank.pc.admin.model.CardBin;
import com.mybank.pc.admin.model.Content;
import com.mybank.pc.admin.model.LogOp;
import com.mybank.pc.admin.model.Mapping;
import com.mybank.pc.admin.model.Param;
import com.mybank.pc.admin.model.Res;
import com.mybank.pc.admin.model.Role;
import com.mybank.pc.admin.model.RoleRes;
import com.mybank.pc.admin.model.Taxonomy;
import com.mybank.pc.admin.model.Ufile;
import com.mybank.pc.admin.model.User;
import com.mybank.pc.admin.model.UserRole;
import com.mybank.pc.collection.model.CollectionClear;
import com.mybank.pc.collection.model.CollectionCleartotle;
import com.mybank.pc.collection.model.CollectionEntrust;
import com.mybank.pc.collection.model.CollectionTrade;
import com.mybank.pc.collection.model.UnionpayBatchCollection;
import com.mybank.pc.collection.model.UnionpayBatchCollectionBatchno;
import com.mybank.pc.collection.model.UnionpayBatchCollectionQuery;
import com.mybank.pc.collection.model.UnionpayCallbackLog;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.collection.model.UnionpayCollectionQuery;
import com.mybank.pc.collection.model.UnionpayEntrust;
import com.mybank.pc.merchant.model.*;

/**
 * Generated by JFinal, do not modify this file.
 * 
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     _MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class _MappingKit {

	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("card_bin", "id", CardBin.class);
		arp.addMapping("collection_clear", "id", CollectionClear.class);
		arp.addMapping("collection_cleartotle", "id", CollectionCleartotle.class);
		arp.addMapping("collection_entrust", "id", CollectionEntrust.class);
		arp.addMapping("collection_trade", "id", CollectionTrade.class);
		arp.addMapping("log_op", "id", LogOp.class);
		arp.addMapping("merchant_cust", "id", MerchantCust.class);
		arp.addMapping("merchant_fee", "id", MerchantFee.class);
		arp.addMapping("merchant_info", "id", MerchantInfo.class);
		arp.addMapping("merchant_user", "id", MerchantUser.class);
		arp.addMapping("s_attachment", "id", Attachment.class);
		arp.addMapping("s_content", "id", Content.class);
		arp.addMapping("s_mapping", "id", Mapping.class);
		arp.addMapping("s_param", "id", Param.class);
		arp.addMapping("s_res", "id", Res.class);
		arp.addMapping("s_role", "id", Role.class);
		arp.addMapping("s_role_res", "id", RoleRes.class);
		arp.addMapping("s_taxonomy", "id", Taxonomy.class);
		arp.addMapping("s_ufile", "id", Ufile.class);
		arp.addMapping("s_user", "id", User.class);
		arp.addMapping("s_user_role", "id", UserRole.class);
		arp.addMapping("unionpay_batch_collection", "id", UnionpayBatchCollection.class);
		arp.addMapping("unionpay_batch_collection_batchno", "id", UnionpayBatchCollectionBatchno.class);
		arp.addMapping("unionpay_batch_collection_query", "id", UnionpayBatchCollectionQuery.class);
		arp.addMapping("unionpay_callback_log", "id", UnionpayCallbackLog.class);
		arp.addMapping("unionpay_collection", "id", UnionpayCollection.class);
		arp.addMapping("unionpay_collection_query", "id", UnionpayCollectionQuery.class);
		arp.addMapping("unionpay_entrust", "id", UnionpayEntrust.class);
		arp.addMapping("merchant_fee_amount_record", "id", MerchantFeeAmountRecord.class);
	}
}
