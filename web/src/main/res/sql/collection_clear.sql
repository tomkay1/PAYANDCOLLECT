#sql("collectTradeByMerchant")
  select ct.merchantID as mId,(select merchantName from merchant_info where id=ct.merchantID) as mName,
  (select merchantNo from merchant_info where id=ct.merchantID) as mNo,
  count(ct.id) as tradeCount,sum(ct.amount) as amount,sum(ct.merFee) as fee,
  sum(ct.bankFee) as bankFee
   from collection_trade ct WHERE ct.finalCode='0' and ct.tradeTime<?
   AND ct.clearStatus!='0' AND ct.dat is NULL GROUP BY ct.merchantID
#end
#sql("collectAllTrade")
  select
  count(ct.id) as tradeCount,sum(ct.amount) as amount,sum(ct.merFee) as fee,
  sum(ct.bankFee) as bankFee
   from collection_trade ct WHERE ct.finalCode='0' and ct.tradeTime<?
   AND ct.clearStatus!='0' AND ct.dat is NULL
#end

#sql("findByMerchant")
select *
from collection_trade ct WHERE ct.finalCode='0' and ct.tradeTime<? AND ct.merchantID=?
                               AND ct.clearStatus!='0' AND ct.dat is NULL
#end