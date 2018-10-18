#sql("findTradeList")
	SELECT * FROM unionpay_advance WHERE 1=1
		#if(search)
	        AND (instr(customerNm, #para(search) )>0 OR instr(accNo, #para(search) )>0 OR instr(certifId, #para(search) )>0) 
	    #end
		#if(customerNm)
	        AND customerNm = #para(customerNm)
	    #end
	    #if(certifTp)
	        AND certifTp = #para(certifTp)
	    #end
	    #if(certifId)
	        AND certifId = #para(certifId)
	    #end
	    #if(accType)
	        AND accType = #para(accType)
	    #end
	    #if(accNo)
	        AND accNo = #para(accNo)
	    #end
	    #if(orderId)
	        AND orderId = #para(orderId)
	    #end
	    #if(txnType)
	        AND txnType = #para(txnType)
	    #end
	    #if(txnSubType)
	        AND txnSubType = #para(txnSubType)
	    #end
	    #if(txnTime)
	        AND txnTime = #para(txnTime)
	    #end
	    #if(merId)
	        AND merId = #para(merId)
	    #end
	    #if(queryId)
	        AND queryId = #para(queryId)
	    #end
	    #if(merchantID)
	        AND merchantID = #para(merchantID)
	    #end
	    #if(finalCode)
	        AND finalCode = #para(finalCode)
	    #end
	    #if(bTime)
	    	AND DATE(cat) >= #para(bTime)
	    #end
	    #if(eTime)
	    	AND DATE(cat) <= #para(eTime)
	    #end
		ORDER BY
			mat DESC,cat DESC
#end
#sql("findUnionpayAdvance")
	SELECT * FROM unionpay_advance  WHERE 1=1
	    #if(customerNm)
	        AND customerNm = #para(customerNm)
	    #end
	    #if(certifTp)
	        AND certifTp = #para(certifTp)
	    #end
	    #if(certifId)
	        AND certifId = #para(certifId)
	    #end
	    #if(accType)
	        AND accType = #para(accType)
	    #end
	    #if(accNo)
	        AND accNo = #para(accNo)
	    #end
	    #if(orderId)
	        AND orderId = #para(orderId)
	    #end
	    #if(txnType)
	        AND txnType = #para(txnType)
	    #end
	    #if(txnSubType)
	        AND txnSubType = #para(txnSubType)
	    #end
	    #if(txnTime)
	        AND txnTime = #para(txnTime)
	    #end
	    #if(merId)
	        AND merId = #para(merId)
	    #end
	    #if(queryId)
	        AND queryId = #para(queryId)
	    #end
	    #if(merchantID)
	        AND merchantID = #para(merchantID)
	    #end
	    #if(finalCode)
	        AND finalCode = #para(finalCode)
	    #end
#end
#sql("findUnionpayAdvanceQuery")
	SELECT * FROM unionpay_advance_query  WHERE 1=1
	    #if(certifId)
	        AND certifId = #para(certifId)
	    #end
	    #if(txnType)
	        AND txnType = #para(txnType)
	    #end
	    #if(txnSubType)
	        AND txnSubType = #para(txnSubType)
	    #end
	    #if(merId)
	        AND merId = #para(merId)
	    #end
	     #if(orderId)
	        AND orderId = #para(orderId)
	    #end
	    #if(txnTime)
	        AND txnTime = #para(txnTime)
	    #end
	     #if(queryId)
	        AND queryId = #para(queryId)
	    #end
	    #if(merchantID)
	        AND merchantID = #para(merchantID)
	    #end
	    ORDER BY
			cat DESC
#end