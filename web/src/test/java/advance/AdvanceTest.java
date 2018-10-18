package advance;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import api.HttpClient;

public class AdvanceTest {

	public static void tradeTest() throws Exception {
		Map<String, String> reqData = new HashMap<String, String>();
		reqData.put("customerNm", "全渠道");
		reqData.put("certifId", "341126197709218366");
		reqData.put("accNo", "6216261000000000018");
		reqData.put("txnAmt", "1000");
		reqData.put("operID", "*$*");

		String host = "http://localhost:8082";
		// String host = "https://pac.mybank.cc";
		String result = HttpClient.send(host + "/advance/trade/initiate", reqData, HttpClient.UTF_8_ENCODING, 50000,
				50000);

		System.out.println(result);
		JSONObject o = JSON.parseObject(result);
		System.out.println(o);
		String unionpayCollection = o.getString("unionpayCollection");
		System.out.println(unionpayCollection);
	}

	public static void main(String[] args) throws Exception {
		tradeTest();
	}
}
