#sql("findByTradeNo")
	SELECT * FROM collection_trade  WHERE 1=1
	    #if(tradeNo)
	        AND tradeNo = #para(tradeNo)
	    #end
#end
#sql("findMerchantFee")
	SELECT * FROM merchant_fee WHERE 1=1
		amountLower < #para(amount)
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

