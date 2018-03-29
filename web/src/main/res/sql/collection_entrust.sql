#sql("findUnionpayEntrustPage")
	SELECT * FROM unionpay_entrust  WHERE 1=1
	    #if(search)
	        AND (instr(customerNm, #para(search) )>0 OR instr(certifId, #para(search) )>0 OR instr(accNo, #para(search) )>0 OR instr(phoneNo, #para(search) )>0) 
	    #end
	    #if(bTime)
	    	AND cat >= #para(bTime)
	    #end
	    #if(eTime)
	    	AND cat <= #para(eTime)
	    #end
	  ORDER BY mat DESC,cat DESC
#end

