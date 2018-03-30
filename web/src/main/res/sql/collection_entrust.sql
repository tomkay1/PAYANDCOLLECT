#sql("findUnionpayEntrustPage")
	SELECT * FROM unionpay_entrust  WHERE 1=1
	    #if(search)
	        AND (instr(customerNm, #para(search))>0 OR instr(certifId, #para(search))>0 OR instr(accNo, #para(search))>0 OR instr(phoneNo, #para(search))>0)
	    #end
	    #if(bTime)
	    	AND DATE(cat) >= #para(bTime)
	    #end
	    #if(eTime)
	    	AND DATE(cat) <= #para(eTime)
	    #end
	    #if(txnType)
	    	AND txnType = #para(txnType)
	    #end
	    #if(txnSubType)
	    	AND txnSubType = #para(txnSubType)
	    #end
	  ORDER BY mat DESC,cat DESC
#end
#sql("findOne")
	SELECT * FROM collection_entrust  WHERE 1=1
	    #if(customerNm)
	        AND customerNm = #para(customerNm)
	    #end
	    #if(certifId)
	        AND certifId = #para(certifId)
	    #end
	    #if(accNo)
	        AND accNo = #para(accNo)
	    #end
	    #if(merId)
	        AND merId = #para(merId)
	    #end
	    #if(merchantID)
	        AND merchantID = #para(merchantID)
	    #end
#end

