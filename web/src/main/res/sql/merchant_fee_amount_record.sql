#sql("page")

  SELECT * FROM merchant_fee_amount_record where 1=1
    #for(x : cond)
       AND #(x.key) #para(x.value)
    #end
    order by cAt DESC


#end