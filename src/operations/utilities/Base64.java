package operations.utilities;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;
import pelarsServer.MultimediaContent;
import pelarsServer.OpDetail;
import servlets.Util;
import authorization.PasswordService;
import operations.OperationSingleValue;

public class Base64 extends OperationSingleValue{

	public boolean decode;

	public Base64(JSONObject content) throws JSONException {
		super(content);

		try{
			decode = content.getBoolean("decode");
		}catch(JSONException e){decode = true;}
	}

	@Override
	public void run(List<? extends Data> objs) throws Exception {

		//SELECT ALL Images from Database
		String query = "SELECT M FROM MultimediaContent AS M WHERE M.type = 'text' AND M.session = :ses";	
		List<MultimediaContent> all_images = servlets.Util.doQuery(my_session,query,"ses",cur_session);
		byte[] b;

		for(MultimediaContent m : all_images){

			if(decode){
				//get the Base64 representation of the content data
				String s = new String(m.getData());
				//decode it as an array of bytes
				b = PasswordService.base64ToByte(s);
			}
			else{
				String s = PasswordService.byteToBase64(m.getData());
				b = s.getBytes();
			}
			m.setData(b);
		}

		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 

		//put the URL of the video as response of the operation
		p.result = new String("done");

		Util.update(my_session,p);	 
	}

	@Override
	public List<? extends Data> extract() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeResult() throws Exception {

	}
}
