#sql("findTradeListPage")
	SELECT * FROM collection_trade WHERE 1=1
		#if(search)
	        AND (instr(custName, #para(search) )>0 OR instr(cardID, #para(search) )>0 OR instr(mobileBank, #para(search) )>0 OR instr(bankcardNo, #para(search) )>0) 
	    #end
		#if(merchantID)
	        AND merchantID = #para(merchantID)
	    #end
		#if(tradeNo)
	        AND tradeNo = #para(tradeNo)
	    #end
		#if(tradeType)
	        AND tradeType = #para(tradeType)
	    #end
		#if(bussType)
	        AND bussType = #para(bussType)
	    #end
		#if(custName)
	        AND custName = #para(custName)
	    #end
		#if(cardID)
	        AND cardID = #para(cardID)
	    #end
		#if(mobileBank)
	        AND mobileBank = #para(mobileBank)
	    #end
		#if(bankcardNo)
	        AND bankcardNo = #para(bankcardNo)
	    #end
		#if(finalCode)
	        AND finalCode = #para(finalCode)
	    #end
		#if(clearStatus)
	        AND clearStatus = #para(clearStatus)
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
#sql("findMerchantCustListPage")
	SELECT * FROM merchant_cust WHERE 1=1
		#if(search)
	        AND (instr(custName, #para(search) )>0 OR instr(cardID, #para(search) )>0 OR instr(mobileBank, #para(search) )>0 OR instr(bankcardNo, #para(search) )>0) 
	    #end
		#if(merchantID)
	        AND merID = #para(merchantID)
	    #end
		ORDER BY
			mat DESC,cat DESC
#end
#sql("findByTradeNo")
	SELECT * FROM collection_trade  WHERE 1=1
	    #if(tradeNo)
	        AND tradeNo = #para(tradeNo)
	    #end
#end
#sql("findUnionpayCollection")
	SELECT * FROM unionpay_collection  WHERE 1=1
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
	    #if(phoneNo)
	        AND phoneNo = #para(phoneNo)
	    #end
	    #if(tradeNo)
	        AND tradeNo = #para(tradeNo)
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
#sql("findMerchantFee")
	SELECT * FROM merchant_fee WHERE 1=1
		AND amountLower < #para(amount)
		AND (
			(amountUpper != 0 AND amountUpper >= #para(amount))
			OR (amountUpper = 0)
		)
		#if(merID)
	        AND merID = #para(merID)
	    #end
		#if(tradeType)
	        AND tradeType = #para(tradeType)
	    #end
		ORDER BY
			amountLower
#end
#sql("findCustByBankcardNo")
	SELECT * FROM merchant_cust  WHERE 1=1
	    #if(bankcardNo)
	        AND bankcardNo = #para(bankcardNo)
	    #end
#end

